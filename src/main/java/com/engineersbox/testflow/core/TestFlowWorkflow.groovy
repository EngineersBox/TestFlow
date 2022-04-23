package com.engineersbox.testflow.core

import com.uber.cadence.workflow.QueryMethod
import com.uber.cadence.workflow.SignalMethod
import com.uber.cadence.workflow.WorkflowMethod

interface TestFlowWorkflow {
    @WorkflowMethod
    FlowState invokeTestFlow();

    @QueryMethod(name = "status")
    FlowState getStatus();

    @SignalMethod
    void retryStage();

    @SignalMethod
    void abort();
}