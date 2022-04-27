package com.engineersbox.testflow.provisioning.context

import com.engineersbox.testflow.config.APIConfig
import com.engineersbox.testflow.config.AWSConfig
import com.engineersbox.testflow.config.TerraformVars

class ProvisioningContext {
    // Testing platform box
    String terraformFile
    TerraformVars tfVars;
    AWSConfig awsConfig;
    def context;

    // Target cluster
    String clusterConfig;
    APIConfig apiConfig;
}
