package com.engineersbox.testflow.provisioning.service

import com.engineersbox.testflow.config.TerraformVars
import com.uber.cadence.workflow.Async
import com.uber.cadence.workflow.Promise

class TerraformJobService implements ExternalJobServiceActivity<UUID> {

    private static Promise<UUID> invokeJob(final def context,
                                           final String jobName,
                                           final TerraformAction action,
                                           final Object params) {
        return Async.function(context.&callPipeline, jobName, action, params);
    }

    @Override
    UUID invokeAction(final def context,
                        final String jobName,
                        final Object ...args) {
        if (args.length < 1) {
            throw new RuntimeException("Requires arguments for TF action and params");
        }
        final TerraformAction action;
        if (args[0] instanceof String) {
            action = TerraformAction.valueOf(args[1] as String);
        } else if (args[0] instanceof TerraformAction) {
            action = args[0] as TerraformAction;
        } else {
            throw new RuntimeException("Expected String or TerraformAction value");
        }
        return invokeJob(
                context,
                jobName,
                action,
                args.length > 1 ? args[1..args.length] : [],
        ).get();
    }
}
