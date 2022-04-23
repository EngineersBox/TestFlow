package com.engineersbox.testflow.provisioning.service

import com.uber.cadence.activity.ActivityMethod

interface CLIServiceActivity<T> {
    @ActivityMethod
    T invokeAction(final def context, final String params);
}