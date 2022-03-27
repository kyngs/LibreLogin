package xyz.kyngs.librepremium.api.event;

import java.util.function.Consumer;

public interface EventProvider {

    <E extends Event> void subscribe(Class<E> clazz, Consumer<E> handler);

    <C extends Event, E extends C> void fire(Class<C> clazz, E event);

}
