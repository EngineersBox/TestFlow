package com.engineersbox.testflow.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper

class Config {
    @JsonProperty("api")
    APIConfig api;

    @JsonProperty("aws")
    AWSConfig aws;

    @JsonProperty("testingPlatform")
    TestingPlatformBoxConfig testingPlatform;

    @JsonProperty("cluster")
    TargetClusterConfig cluster;

    static Config fromResources(final String path) {
        final Properties properties = new Properties()
        final  File propertiesFile = new File(path)
        propertiesFile.withInputStream {
            properties.load(it)
        };
        final JavaPropsMapper mapper = new JavaPropsMapper();
        return mapper.readerFor(Config.class).readValue(propertiesFile);
    }
}
