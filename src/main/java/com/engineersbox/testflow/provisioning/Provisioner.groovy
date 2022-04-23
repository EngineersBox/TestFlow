package com.engineersbox.testflow.provisioning

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.config.AWSConfig
import com.engineersbox.testflow.provisioning.context.ProvisioningContext
import com.engineersbox.testflow.provisioning.service.APIServiceActivity
import com.engineersbox.testflow.provisioning.service.CLIServiceActivity
import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.Promise
import com.uber.cadence.workflow.Workflow

class Provisioner implements ProvisioningWorkflow<ProvisioningContext, Tuple2<UUID, UUID>> {

    private final APIServiceActivity<UUID> apiService;
    private final CLIServiceActivity<UUID> cliService;

    Provisioner() {
        this.apiService = Workflow.newActivityStub(APIServiceActivity<UUID>.class);
        this.cliService = Workflow.newActivityStub(CLIServiceActivity<UUID>.class);
    }

    private Promise<UUID> provisionTargetCluster(final APIConfig config) {
        return Async.function(
                this.apiService.&invokeRequest,
                config,
                null,
                null,
                [] as Object[]
        );
    }

    private Promise<UUID> provisionTestPlatformBox(final def context, final String params) {
        return Async.function(
                this.cliService.&invokeAction,
                context,
                params,
        );
    }

    @Override
    Tuple2<UUID, UUID> provision(final ProvisioningContext provisioningContext) {
        try {
            final List<UUID> results = Promise.allOf([
                    provisionTargetCluster(null),
                    provisionTestPlatformBox(null, null),
            ]).get();
            return new Tuple2<>(results[0], results[1]);
        } catch (final Exception ignored) {
            return null;
        }
    }
}
