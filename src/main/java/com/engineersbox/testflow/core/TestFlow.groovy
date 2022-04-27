package com.engineersbox.testflow.core

import com.engineersbox.testflow.config.Config
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

abstract class TestFlow implements TestFlowWorkflow {

    private static final int MAX_RETRIES = 10;
    private static final Duration SIGNAL_WAIT_TIMEOUT = Duration.ofSeconds(10L);

    private FlowState status;
    private Boolean retry;
    private TestStageContext testStageContext;

    private ProvisioningWorkflow<ProvisioningContext, Tuple2<UUID, UUID>> provisioningWorkflow;
    private DeprovisioningWorkflow<DeprovisioningContext, Boolean> deprovisioningWorkflow;
    private final ProvisioningContext provisioningContext;
    private final DeprovisioningContext deprovisioningContext;
    
    private final Config config;

    TestFlow() {
        this.provisioningWorkflow = Workflow.newChildWorkflowStub(ProvisioningWorkflow<ProvisioningContext, Tuple2<UUID, UUID>>.class);
        this.deprovisioningWorkflow = Workflow.newChildWorkflowStub(DeprovisioningWorkflow<DeprovisioningContext, Boolean>.class);
        this.testStageContext = new TestStageContext();
        this.provisioningContext = new ProvisioningContext();
        this.deprovisioningContext = new DeprovisioningContext();
        this.config = Config.fromResources();
    }

    abstract List<TestStageWorkflow> provideStages();

    private void provision() {
        this.status = FlowState.PROVISIONING;
        final Tuple2<UUID, UUID> result = this.provisioningWorkflow.provision(this.provisioningContext);
        if (result == null) {
            this.status = FlowState.FAILED;
        }
    }

    private void deprovision() {
        this.status = FlowState.DEPROVISIONING;
        final Boolean result = this.deprovisioningWorkflow.deprovision(this.deprovisioningContext);
        if (Boolean.TRUE != result) {
            this.status = FlowState.FAILED;
        }
    }

    private void configureTestBox() {
        this.status = FlowState.CONFIGURING_TEST_PLATFORM;
    }

    private boolean shouldRetry() {
        final FlowState previousState = this.status;
        this.status = FlowState.WAITING_FOR_SIGNAL;
        final boolean result = Workflow.await(SIGNAL_WAIT_TIMEOUT, { -> this.retry })
        this.retry = null;
        this.status = previousState;
        return result;
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
            if (this.testStageContext.shouldRetry() && retryAttempts < MAX_RETRIES && shouldRetry()) {
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

    @Override
    final FlowState invokeTestFlow() {
        this.status = FlowState.RUNNING;

        provision();
        if (this.status == FlowState.FAILED) {
            return this.status;
        }

        configureTestBox();
        if (this.status == FlowState.FAILED) {
            return this.status;
        }

        final TestResult testResult = executeTests();

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
