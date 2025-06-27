/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

/**
 * A Log4J implementation of the {@link LogFilter} that filters out sensitive authentication commands.
 * <p>
 * This class extends {@link LogFilter} and implements Log4J's {@link Filter} interface to provide filtering capabilities for Log4J logging system.
 * <p>
 * The filter is designed to prevent logging of sensitive information such as passwords and authentication tokens that might appear in certain commands. It integrates with Log4J's filtering chain and can be easily added to the root logger.
 * <p>
 * This implementation handles various Log4J filter method overloads to ensure comprehensive coverage of all possible logging scenarios, including parameterized messages and raw objects.
 */
public class Log4JFilter extends LogFilter implements Filter {

    @Override
    public void inject() {
        ((Logger) LogManager.getRootLogger()).addFilter(new Log4JFilter());
    }

    /**
     * Converts the result of message checking into a Log4J {@link Result}.
     *
     * @param message The message pattern to be checked
     * @param parameters The parameters associated with the message.
     * @return {@link Result#NEUTRAL} if the message should be logged, {@link Result#DENY} if it should be filtered out
     * @see LogFilter#checkMessage
     */
    private Result checkMessageResult(String message, Object[] parameters) {
        return checkMessage(message, parameters) ? Result.NEUTRAL : Result.DENY;
    }

    @Override
    public Result getOnMatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result getOnMismatch() {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object... params) {
        return checkMessageResult(message, params);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0) {
        return checkMessageResult(message, new Object[]{p0});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1) {
        return checkMessageResult(message, new Object[]{p0, p1});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return checkMessageResult(message, new Object[]{p0, p1, p2});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4, p5});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4, p5, p6});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7, p8});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return checkMessageResult(message, new Object[]{p0, p1, p2, p3, p4, p5, p6, p7, p8, p9});
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return checkMessageResult(msg.toString(), null);
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return checkMessageResult(msg.getFormat(), msg.getParameters());
    }

    @Override
    public Result filter(LogEvent event) {
        var message = event.getMessage();
        return checkMessageResult(message.getFormat(), message.getParameters());
    }

    @Override
    public State getState() {
        return State.STARTED;
    }

    public void initialize() {
    }

    public boolean isStarted() {
        return true;
    }

    public boolean isStopped() {
        return false;
    }

    public void start() {
    }

    public void stop() {
    }
}
