package com.engineersbox.testflow.provisioning.service

import com.engineersbox.testflow.config.APIConfig
import com.uber.cadence.activity.ActivityMethod

interface APIServiceActivity<T> {
    @ActivityMethod
    T invokeRequest(final APIConfig config, final String method, final String endpoint, final Object ...requestArgs);
}