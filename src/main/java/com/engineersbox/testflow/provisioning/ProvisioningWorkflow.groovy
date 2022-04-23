package com.engineersbox.testflow.provisioning

import com.uber.cadence.workflow.WorkflowMethod

interface ProvisioningWorkflow<T,R> {
    @WorkflowMethod
    R provision(final T provisioningContext);
}