package com.engineersbox.testflow.provisioning.context

import com.engineersbox.testflow.config.APIConfig

class DeprovisioningContext {
    def context;
    APIConfig apiConfig;
    UUID clusterId;
    UUID tfResourceId;
}
