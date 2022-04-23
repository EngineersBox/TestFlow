package com.engineersbox.testflow.provisioning

import com.uber.cadence.workflow.WorkflowMethod

interface DeprovisioningWorkflow<T,R> {
    @WorkflowMethod
    R deprovision(final T deprovisioningContext);
}