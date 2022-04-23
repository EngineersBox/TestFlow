package com.engineersbox.testflow.stage

import com.engineersbox.testflow.stage.context.TestStageContext
import com.uber.cadence.workflow.WorkflowMethod

interface TestStageWorkflow {
    @WorkflowMethod
    TestResult execute(final TestStageContext context);
}