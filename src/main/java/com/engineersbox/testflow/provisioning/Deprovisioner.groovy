package com.engineersbox.testflow.provisioning

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.provisioning.context.DeprovisioningContext
import com.engineersbox.testflow.provisioning.service.APIServiceActivity
import com.engineersbox.testflow.provisioning.service.CLIServiceActivity
import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.Promise
import com.uber.cadence.workflow.Workflow

class Deprovisioner implements DeprovisioningWorkflow<DeprovisioningContext, Boolean> {

    private final APIServiceActivity<Boolean> apiService;
    private final CLIServiceActivity<Boolean> cliService;

    Deprovisioner() {
        this.apiService = Workflow.newActivityStub(APIServiceActivity<Boolean>.class);
        this.cliService = Workflow.newActivityStub(CLIServiceActivity<Boolean>.class);
    }

    private Promise<Boolean> deprovisionTargetCluster(final APIConfig config) {
        return Async.function(
                this.apiService.&invokeRequest,
                config,
                null,
                null,
                [] as Object[]
        );
    }

    private Promise<Boolean> deprovisionTestPlatformBox(final def context, final String params) {
        return Async.function(
                this.cliService.&invokeAction,
                context,
                params,
        );
    }

    @Override
    Boolean deprovision(final DeprovisioningContext deprovisioningContext) {
        try {
            final List<Boolean> results = Promise.allOf([
                    deprovisionTargetCluster(deprovisioningContext.getApiConfig()),
                    deprovisionTestPlatformBox(deprovisioningContext.getContext(), null),
            ]).get();
            return results.every(Boolean.TRUE.&equals);
        } catch (final Exception ignored) {
            return false;
        }
    }

}
