package com.engineersbox.testflow.core

enum FlowState {
    IDLE,
    RUNNING,
    PROVISIONING,
    DEPROVISIONING,
    EXECUTING_TEST_STAGES,
    TEST_FAILURE,
    CONFIGURING_TEST_PLATFORM,
    WAITING_FOR_SIGNAL,
    FAILED,
    SUCCEEDED,
}