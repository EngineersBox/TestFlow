package com.engineersbox.testflow.config

class TerraformVars {
    String name
    String region
    String size
    String ami
    Map<String, String> tags

    Map<String, ?> intoMap() {
        return [
                name: this.name,
                region: this.region,
                size: this.size,
                ami: this.ami,
                tags: this.tags,
        ];
    }
}
