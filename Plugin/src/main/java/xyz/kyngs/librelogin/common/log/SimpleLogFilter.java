/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.log;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SimpleLogFilter extends LogFilter implements Filter {

    private final Filter filter;
    private final Logger logger;

    public SimpleLogFilter(Logger logger) {
        filter = logger.getFilter();
        this.logger = logger;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (!filter.isLoggable(record)) return false;

        return checkMessage(record.getMessage());
    }

    @Override
    public void inject() {
        logger.setFilter(this);
    }
}
