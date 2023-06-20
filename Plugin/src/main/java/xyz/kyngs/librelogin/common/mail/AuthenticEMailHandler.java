/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.mail;

import org.apache.commons.mail.EmailConstants;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import xyz.kyngs.librelogin.api.mail.EmailHandler;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;

public class AuthenticEMailHandler implements EmailHandler {

    private final AuthenticLibreLogin<?, ?> plugin;

    public AuthenticEMailHandler(AuthenticLibreLogin<?, ?> plugin) {
        this.plugin = plugin;
    }

    @Override
    public void sendEmail(String email, String subject, String content) {
        try {
            var config = plugin.getConfiguration();
            var port = config.get(ConfigurationKeys.MAIL_PORT);

            var mail = new HtmlEmail();

            mail.setCharset(EmailConstants.UTF_8);
            mail.setHostName(config.get(ConfigurationKeys.MAIL_HOST));
            mail.setSmtpPort(port);
            mail.setSubject(subject);
            mail.setAuthentication(config.get(ConfigurationKeys.MAIL_USERNAME), config.get(ConfigurationKeys.MAIL_PASSWORD));
            mail.addTo(email);
            mail.setFrom(config.get(ConfigurationKeys.MAIL_USERNAME), config.get(ConfigurationKeys.MAIL_SENDER));

            switch (port) {
                case 465 -> {
                    mail.setSslSmtpPort(String.valueOf(port));
                    mail.setSSLOnConnect(false);
                }
                case 587 -> {
                    mail.setStartTLSEnabled(true);
                    mail.setStartTLSRequired(true);
                }
                default -> {
                    mail.setStartTLSEnabled(true);
                    mail.setSSLOnConnect(true);
                    mail.setSSLCheckServerIdentity(true);
                }
            }

            mail.setHtmlMsg(content);
            mail.send();
        } catch (EmailException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void sendTestMail(String email) {
        sendEmail(email, "LibreLogin test mail", """
                Congratulations! You have successfully configured email sending in LibreLogin!<br>
                Now, your users can reset their passwords.<br>
                <i>If you have no idea what this means, block the sender.</i>
                """);
    }

    @Override
    public void sendPasswordResetMail(String email, String token, String ip) {

    }

    @Override
    public void sendVerificationMail(String email, String token, String ip) {

    }
}
