package com.engineersbox.testflow.stage.context

import com.engineersbox.testflow.stage.TestResult

class TestStageContext {
    private final Map<String, Object> contextElements;
    private TestResult previousStageResult = null;
    private FailureBehaviour failureBehaviour;
    private boolean retry;

    TestStageContext() {
        this.contextElements = new HashMap<>();
        this.failureBehaviour = FailureBehaviour.SKIP;
        this.retry = false;
    }

    Object getElement(final String key) {
        return this.contextElements.get(key);
    }

    void setElement(final String key, final Object value) {
        this.contextElements.put(key, value);
    }

    void setPreviousStageResult(final TestResult previousStageResult) {
        this.previousStageResult = previousStageResult;
    }

    TestResult getPreviousStageResult() {
        return this.previousStageResult;
    }

    void setFailureBehaviour(final FailureBehaviour failureBehaviour) {
        this.failureBehaviour = failureBehaviour;
    }

    FailureBehaviour getFailureBehaviour() {
        return this.failureBehaviour;
    }

    void setRetry(final boolean retry) {
        this.retry = retry;
    }

    boolean shouldRetry() {
        return this.retry;
    }
}
