/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.listener;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent.AuthenticationSource;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.config.ConfigurationKeys;

public class LoginTryListener<P, S> {

    private final AuthenticLibreLogin<P, S> plugin;
    private final Cache<P, Integer> loginTries;

    public LoginTryListener(AuthenticLibreLogin<P, S> libreLogin) {
        this.plugin = libreLogin;
        this.loginTries = Caffeine.newBuilder()
                .expireAfterAccess(plugin.getConfiguration().get(ConfigurationKeys.MILLISECONDS_TO_EXPIRE_LOGIN_ATTEMPTS), TimeUnit.MILLISECONDS)
                .build();
        libreLogin.getEventProvider().subscribe(libreLogin.getEventTypes().wrongPassword, this::onWrongPassword);
        libreLogin.getEventProvider().subscribe(libreLogin.getEventTypes().authenticated, this::onAuthenticated);
    }

    private void onWrongPassword(WrongPasswordEvent<P, S> wrongPasswordEvent) {
        AuthenticationSource source = wrongPasswordEvent.getSource();
        if (source != AuthenticationSource.LOGIN && source != AuthenticationSource.TOTP)
            return;
        if (plugin.getConfiguration().get(ConfigurationKeys.MAX_LOGIN_ATTEMPTS) == -1)
            return;
        // if key do not exists, put 1 as value
        // otherwise sum 1 to the value linked to key
        int currentLoginTry = loginTries.asMap().merge(wrongPasswordEvent.getPlayer(), 1, Integer::sum);
        if (currentLoginTry >= plugin.getConfiguration().get(ConfigurationKeys.MAX_LOGIN_ATTEMPTS)) {
            plugin.getPlatformHandle().kick(wrongPasswordEvent.getPlayer(), plugin.getMessages().getMessage("kick-error-password-wrong"));
        }
    }

    private void onAuthenticated(AuthenticatedEvent<P, S> authenticatedEvent) {
        loginTries.invalidate(authenticatedEvent.getPlayer());
    }

}
