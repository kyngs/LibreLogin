/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.log;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A simple implementation of the LogFilter for Java's built-in logging system.
 * <p>
 * This class extends {@link LogFilter} and implements {@link Filter} interface to provide filtering capabilities for the standard Java logging framework.
 * <p>
 * This implementation preserves any existing filter that was already set on the logger by chaining it with this filter's functionality.</p>
 */
public class SimpleLogFilter extends LogFilter implements Filter {

    /**
     * The original filter that was set on the logger before this filter was applied.
     * <p>
     * This is preserved to maintain the existing filtering chain.
     */
    private final Filter filter;
    /**
     * The logger instance that this filter is attached to.
     * <p>
     * Used for filter injection and management.
     */
    private final Logger logger;

    public SimpleLogFilter(Logger logger) {
        filter = logger.getFilter();
        this.logger = logger;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (filter != null && !filter.isLoggable(record)) return false;

        return checkMessage(record.getMessage(), record.getParameters());
    }

    @Override
    public void inject() {
        logger.setFilter(this);
    }
}
