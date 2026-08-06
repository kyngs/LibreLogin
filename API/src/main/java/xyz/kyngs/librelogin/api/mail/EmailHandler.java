/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.mail;

/**
 * This interface is used to send emails to the users.
 *
 * @author kyngs
 */
public interface EmailHandler {

    /**
     * This method sends an email to the user.
     *
     * @param email   The email to send the email to.
     * @param subject The subject of the email.
     * @param content The content of the email.
     */
    void sendEmail(String email, String subject, String content);

    /**
     * This method sends a test email to the user.
     *
     * @param email The email to send the email to.
     */
    void sendTestMail(String email);

    /**
     * This method sends a password reset email to the user.
     *
     * @param email    The email to send the email to.
     * @param token    The token to reset the password.
     * @param ip       The ip of the user.
     * @param username The username of the user.
     */
    void sendPasswordResetMail(String email, String token, String username, String ip);

    /**
     * This method sends a verification email to the user.
     *
     * @param email The email to send the email to.
     * @param token The token to verify the email.
     * @param username The username of the user.
     */
    void sendVerificationMail(String email, String token, String username);
}
