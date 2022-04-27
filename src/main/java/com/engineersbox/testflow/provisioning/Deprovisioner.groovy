package com.engineersbox.testflow.provisioning

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.config.TerraformVars
import com.engineersbox.testflow.provisioning.context.DeprovisioningContext
import com.engineersbox.testflow.provisioning.service.APIServiceActivity
import com.engineersbox.testflow.provisioning.service.ExternalJobServiceActivity
import com.engineersbox.testflow.provisioning.service.TerraformAction
import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.Promise
import com.uber.cadence.workflow.Workflow

class Deprovisioner implements DeprovisioningWorkflow<DeprovisioningContext, Boolean> {

    private static final HTTP_DELETE_METHOD = "DELETE";
    private static final EXTENDED_PROVISIONING_ENDPOINT = "/provisioning/v1/extended";
    private static final TERRAFORM_JENKINS_JOB_NAME = "terraform-pipeline";

    private final APIServiceActivity<Boolean> apiService;
    private final ExternalJobServiceActivity<Boolean> cliService;

    Deprovisioner() {
        this.apiService = Workflow.newActivityStub(APIServiceActivity<Boolean>.class);
        this.cliService = Workflow.newActivityStub(ExternalJobServiceActivity<Boolean>.class);
    }

    private Promise<Boolean> deprovisionTargetCluster(final APIConfig config, final UUID clusterId) {
        return Async.function(
                this.apiService.&invokeRequest,
                config,
                HTTP_DELETE_METHOD,
                EXTENDED_PROVISIONING_ENDPOINT,
                [clusterId] as Object[]
        );
    }

    private Promise<Boolean> deprovisionTestPlatformBox(final def context, final UUID resourceId) {
        return Async.function(
                this.cliService.&invokeAction,
                context,
                TERRAFORM_JENKINS_JOB_NAME,
                [TerraformAction.DESTROY, resourceId] as Object[]
        );
    }

    @Override
    Boolean deprovision(final DeprovisioningContext deprovisioningContext) {
        try {
            final List<Boolean> results = Promise.allOf([
                    deprovisionTargetCluster(deprovisioningContext.getApiConfig(), deprovisioningContext.clusterId),
                    deprovisionTestPlatformBox(deprovisioningContext.getContext(), deprovisioningContext.tfResourceId),
            ]).get();
            return results.every(Boolean.TRUE.&equals);
        } catch (final Exception ignored) {
            return false;
        }
    }

}
