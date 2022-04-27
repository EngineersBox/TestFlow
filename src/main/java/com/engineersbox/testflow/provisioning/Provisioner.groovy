package com.engineersbox.testflow.provisioning

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.config.TerraformVars
import com.engineersbox.testflow.provisioning.context.ProvisioningContext
import com.engineersbox.testflow.provisioning.service.APIServiceActivity
import com.engineersbox.testflow.provisioning.service.ExternalJobServiceActivity
import com.engineersbox.testflow.provisioning.service.TerraformAction
import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.Promise
import com.uber.cadence.workflow.Workflow

class Provisioner implements ProvisioningWorkflow<ProvisioningContext, Tuple2<UUID, UUID>> {

    private static final HTTP_POST_METHOD = "POST";
    private static final EXTENDED_PROVISIONING_ENDPOINT = "/provisioning/v1/extended";
    private static final TERRAFORM_JENKINS_JOB_NAME = "terraform-pipeline";

    private final APIServiceActivity<UUID> apiService;
    private final ExternalJobServiceActivity<UUID> terraformService;

    Provisioner() {
        this.apiService = Workflow.newActivityStub(APIServiceActivity<UUID>.class);
        this.terraformService = Workflow.newActivityStub(ExternalJobServiceActivity<UUID>.class);
    }

    private Promise<UUID> provisionTargetCluster(final APIConfig config) {
        return Async.function(
                this.apiService.&invokeRequest,
                config,
                HTTP_POST_METHOD,
                EXTENDED_PROVISIONING_ENDPOINT,
                [] as Object[]
        );
    }

    private Promise<UUID> provisionTestPlatformBox(final def context, final TerraformVars tfVars) {
        return Async.function(
                this.terraformService.&invokeAction,
                context,
                TERRAFORM_JENKINS_JOB_NAME,
                [TerraformAction.APPLY, tfVars] as Object[],
        );
    }

    @Override
    Tuple2<UUID, UUID> provision(final ProvisioningContext provisioningContext) {
        try {
            final List<UUID> results = Promise.allOf([
                    provisionTargetCluster(provisioningContext.getApiConfig()),
                    provisionTestPlatformBox(provisioningContext.getContext(), provisioningContext.tfVars),
            ]).get();
            return new Tuple2<>(results[0], results[1]);
        } catch (final Exception ignored) {
            return null;
        }
    }
}
