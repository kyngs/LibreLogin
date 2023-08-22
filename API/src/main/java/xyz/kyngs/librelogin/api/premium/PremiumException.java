/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.premium;

/**
 * An exception that is thrown when fetching premium data fails.
 *
 * @author kyngs
 */
public class PremiumException extends Exception {

    /**
     * The issue that caused this exception.
     */
    private final Issue issue;

    /**
     * Construct a new PremiumException with the given Issue and Exception.
     *
     * @param issue     The issue related to the exception.
     * @param exception The exception that caused the issue.
     */
    public PremiumException(Issue issue, Exception exception) {
        super(exception);
        this.issue = issue;
    }

    /**
     * Construct a new PremiumException with the given Issue and message.
     *
     * @param issue The issue related to the exception.
     * @param message The message describing the exception.
     */
    public PremiumException(Issue issue, String message) {
        super(message);
        this.issue = issue;
    }

    /**
     * Gets the issue that caused this exception.
     *
     * @return the issue that caused this exception
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * Possible issues that can cause this exception.
     */
    public enum Issue {
        /**
         * The API throttled the request.
         */
        THROTTLED,
        /**
         * The API returned an invalid response.
         */
        SERVER_EXCEPTION,
        /**
         * Other issues.
         */
        UNDEFINED
    }

}
