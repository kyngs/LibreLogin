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
        if (wrongPasswordEvent.getSource() != AuthenticationSource.LOGIN)
            return;
        // if key do not exists, put 1 as value
        // otherwise sum 1 to the value linked to key
        loginTries.merge(wrongPasswordEvent.getPlayer(), 1, Integer::sum);
        if (loginTries.getOrDefault(wrongPasswordEvent.getPlayer(), 0) >= plugin.getConfiguration().get(ConfigurationKeys.MAX_LOGIN_TRIES)) {
            plugin.getPlatformHandle().kick(wrongPasswordEvent.getPlayer(), plugin.getMessages().getMessage("kick-too-many-login-tries"));
        }
    }

    private void onAuthenticated(AuthenticatedEvent<P, S> authenticatedEvent) {
        loginTries.remove(authenticatedEvent.getPlayer());
    }

}
