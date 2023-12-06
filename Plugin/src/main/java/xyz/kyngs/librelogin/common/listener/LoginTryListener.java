/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package xyz.kyngs.librelogin.common.listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent.AuthenticationSource;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;

public class LoginTryListener<P, S> {

    private final AuthenticLibreLogin<P, S> plugin;
    private final Map<P, Integer> loginTries = new ConcurrentHashMap<>();

    public LoginTryListener(AuthenticLibreLogin<P, S> libreLogin) {
        this.plugin = libreLogin;
        libreLogin.getEventProvider().subscribe(libreLogin.getEventTypes().wrongPassword, this::onWrongPassword);
        libreLogin.getEventProvider().subscribe(libreLogin.getEventTypes().authenticated, this::onAuthenticated);
    }

    private void onWrongPassword(WrongPasswordEvent<P, S> wrongPasswordEvent) {
        if (wrongPasswordEvent.getSource() != AuthenticationSource.LOGIN && wrongPasswordEvent.getSource() != AuthenticationSource.TOTP)
            return;
        // if key do not exists, put 1 as value
        // otherwise sum 1 to the value linked to key
        int currentLoginTry = loginTries.merge(wrongPasswordEvent.getPlayer(), 1, Integer::sum);
        if (currentLoginTry >= plugin.getConfiguration().get(ConfigurationKeys.KICK_ON_WRONG_PASSWORD)) {
            plugin.getPlatformHandle().kick(wrongPasswordEvent.getPlayer(), plugin.getMessages().getMessage("kick-error-password-wrong"));
        }
    }

    private void onAuthenticated(AuthenticatedEvent<P, S> authenticatedEvent) {
        loginTries.remove(authenticatedEvent.getPlayer());
    }

}
