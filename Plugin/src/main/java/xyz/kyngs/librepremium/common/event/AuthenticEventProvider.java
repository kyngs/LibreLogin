package xyz.kyngs.librepremium.common.event;

import xyz.kyngs.librepremium.api.event.Event;
import xyz.kyngs.librepremium.api.event.EventProvider;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class AuthenticEventProvider implements EventProvider {

    private final Map<Class<? extends Event>, Set<Consumer<Event>>> listeners;
    private final AuthenticLibrePremium plugin;

    public AuthenticEventProvider(AuthenticLibrePremium plugin) {
        this.plugin = plugin;
        listeners = new HashMap<>();
    }

    @Override
    public <E extends Event> void subscribe(Class<E> clazz, Consumer<E> handler) {
        if (!clazz.isInterface())
            throw new IllegalArgumentException("You must subscribe to the event, not its implementation");
        listeners.computeIfAbsent(clazz, x -> new HashSet<>()).add((Consumer<Event>) handler);
    }

    @Override
    public <C extends Event, E extends C> void fire(Class<C> clazz, E event) {
        var set = listeners.get(clazz);

        if (set == null || set.isEmpty()) return;

        for (Consumer<Event> consumer : set) {
            consumer.accept(event);
        }
    }


}
