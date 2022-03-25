package xyz.kyngs.librepremium.common;

import xyz.kyngs.librepremium.api.Logger;

public class SL4JLogger implements Logger {

    private final org.slf4j.Logger slf4j;

    public SL4JLogger(org.slf4j.Logger slf4j) {
        this.slf4j = slf4j;
    }

    @Override
    public void info(String message) {
        slf4j.info(message);
    }

    @Override
    public void warn(String message) {
        slf4j.warn(message);
    }

    @Override
    public void error(String message) {
        slf4j.error(message);
    }
}
