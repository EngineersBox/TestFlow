package com.engineersbox.testflow.provisioning

import com.engineersbox.testflow.provisioning.context.DeprovisioningContext

class Deprovisioner implements DeprovisioningWorkflow<DeprovisioningContext, Void> {
    @Override
    Void deprovision(final DeprovisioningContext deprovisioningContext) {
        return null;
    }
}
