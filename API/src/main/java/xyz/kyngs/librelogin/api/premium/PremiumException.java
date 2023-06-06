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

    private final Issue issue;

    public PremiumException(Issue issue, Exception exception) {
        super(exception);
        this.issue = issue;
    }

    public PremiumException(Issue issue, String message) {
        super(message);
        this.issue = issue;
    }

    public Issue getIssue() {
        return issue;
    }

    public enum Issue {
        THROTTLED, SERVER_EXCEPTION, UNDEFINED
    }

}
