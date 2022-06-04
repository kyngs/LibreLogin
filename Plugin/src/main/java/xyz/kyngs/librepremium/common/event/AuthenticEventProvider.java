package xyz.kyngs.librepremium.common.event;

import xyz.kyngs.librepremium.api.event.Event;
import xyz.kyngs.librepremium.api.event.EventProvider;
import xyz.kyngs.librepremium.common.AuthenticHandler;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AuthenticEventProvider<P, S> extends AuthenticHandler<P, S> implements EventProvider<P, S> {

    private final Map<Class<? extends Event<P, S>>, Set<Consumer<Event<P, S>>>> listeners;

    public AuthenticEventProvider(AuthenticLibrePremium<P, S> plugin) {
        super(plugin);
        this.listeners = new HashMap<>();
    }

    @Override
    public <E extends Event<P, S>> void subscribe(Class<E> clazz, Consumer<E> handler) {
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
