package com.engineersbox.testflow.provisioning.context

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.config.AWSConfig

class ProvisioningContext {
    // Testing platform box
    String terraformFile
    Map<String, ?> tfVars;
    AWSConfig awsConfig;

    // Target cluster
    String clusterConfig;
    APIConfig apiConfig;
}
