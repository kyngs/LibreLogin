package xyz.kyngs.librelogin.common.event;

import xyz.kyngs.librelogin.api.event.Event;
import xyz.kyngs.librelogin.api.event.EventProvider;
import xyz.kyngs.librelogin.common.AuthenticHandler;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AuthenticEventProvider<P, S> extends AuthenticHandler<P, S> implements EventProvider<P, S> {

    private final Map<Class<? extends Event<P, S>>, Set<Consumer<Event<P, S>>>> listeners;

    public AuthenticEventProvider(AuthenticLibreLogin<P, S> plugin) {
        super(plugin);
        this.listeners = new HashMap<>();
    }

    @Override
    public <E extends Event<P, S>> void subscribe(Class<? extends E> clazz, Consumer<E> handler) {
        if (!clazz.isInterface())
            throw new IllegalArgumentException("You must subscribe to the event, not its implementation");
        listeners.computeIfAbsent(clazz, x -> new HashSet<>()).add((Consumer<Event<P, S>>) handler);
    }

    @Override
    public <C extends Event<?, ?>, E extends C> void fire(Class<C> clazz, E event) {
        var set = listeners.get(clazz);

        if (set == null || set.isEmpty()) return;

        for (Consumer<Event<P, S>> consumer : set) {
            consumer.accept((Event<P, S>) event);
        }
    }
}
