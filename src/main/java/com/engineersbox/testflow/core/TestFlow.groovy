package com.engineersbox.testflow.core

import com.engineersbox.testflow.provisioning.DeprovisioningWorkflow
import com.engineersbox.testflow.provisioning.ProvisioningWorkflow
import com.engineersbox.testflow.provisioning.context.DeprovisioningContext
import com.engineersbox.testflow.provisioning.context.ProvisioningContext
import com.engineersbox.testflow.stage.TestResult
import com.engineersbox.testflow.stage.TestStageWorkflow
import com.engineersbox.testflow.stage.context.FailureBehaviour
import com.engineersbox.testflow.stage.context.TestStageContext
import com.engineersbox.testflow.util.SeekableCollection
import com.uber.cadence.workflow.Workflow

import java.time.Duration

abstract  class TestFlow implements TestFlowWorkflow {

    private static final int MAX_RETRIES = 10;
    private static final Duration SIGNAL_WAIT_TIMEOUT = Duration.ofSeconds(10L);

    private FlowState status;
    private Boolean retry;
    private TestStageContext testStageContext;

    private ProvisioningWorkflow provisioningWorkflow;
    private DeprovisioningWorkflow deprovisioningWorkflow;

    TestFlow() {
        this.provisioningWorkflow = Workflow.newChildWorkflowStub(ProvisioningWorkflow<ProvisioningContext, Void>.class);
        this.deprovisioningWorkflow = Workflow.newChildWorkflowStub(DeprovisioningWorkflow<DeprovisioningContext, Void>.class);
        this.testStageContext = new TestStageContext();
    }

    abstract List<TestStageWorkflow> provideStages();

    private void provision() {
        this.status = FlowState.PROVISIONING;
        this.provisioningWorkflow.provision();
    }

    private void deprovision() {
        this.status = FlowState.DEPROVISIONING;
    }

    private void configureTestBox() {
        this.status = FlowState.CONFIGURING_TEST_PLATFORM;
    }

    private TestResult executeTests() {
        this.status = FlowState.EXECUTING_TEST_STAGES;
        TestResult testResult = TestResult.SUCCESS;
        final SeekableCollection<TestStageWorkflow> seekableStages = new SeekableCollection(provideStages());
        int retryAttempts = 0;
        TestStageWorkflow stage;
        testStageIterator: while ((stage = (TestStageWorkflow) seekableStages.next()) != null) {
            final FailureBehaviour failureBehaviour = this.testStageContext.getFailureBehaviour();
            final TestResult currentResult = stage.execute(this.testStageContext);
            if (currentResult == TestResult.SUCCESS) {
                retryAttempts = 0;
                continue;
            }
            this.status = FlowState.TEST_FAILURE;
            if (this.testStageContext.shouldRetry() && retryAttempts < MAX_RETRIES) {
                retryAttempts++;
                seekableStages.seekRelative(-1);
                continue;
            }
            switch (failureBehaviour) {
                case FailureBehaviour.EXIT:
                    testResult = currentResult;
                    break testStageIterator;
                case FailureBehaviour.SKIP:
                    retryAttempts = 0;
                    break;
            }
        }
        return testResult;
    }

    private boolean shouldRetry() {
        final FlowState previousState = this.status;
        this.status = FlowState.WAITING_FOR_SIGNAL;
        final boolean result = Workflow.await(SIGNAL_WAIT_TIMEOUT, { -> this.retry })
        this.retry = null;
        this.status = previousState;
        return result;
    }

    @Override
    final FlowState invokeTestFlow() {
        this.status = FlowState.RUNNING;

        // TODO: Provision
        provision();

        // TODO: Configure testing box
        configureTestBox();

        // TODO: Run test stages
        final TestResult testResult = executeTests();

        // TODO: Deprovision
        deprovision();
        return this.status;
    }

    @Override
    final FlowState getStatus() {
        return this.status
    }

    @Override
    final void retryStage() {
        this.retry = true;
    }

    @Override
    final void abort() {

    }
}
