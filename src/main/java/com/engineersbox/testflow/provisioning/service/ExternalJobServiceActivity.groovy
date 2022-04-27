package com.engineersbox.testflow.provisioning.service

import com.uber.cadence.activity.ActivityMethod

interface ExternalJobServiceActivity<T> {
    @ActivityMethod
    T invokeAction(final def context, final String jobName, final Object ...args);
}