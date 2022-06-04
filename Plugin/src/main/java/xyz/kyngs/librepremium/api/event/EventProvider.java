package xyz.kyngs.librepremium.api.event;

import java.util.function.Consumer;

public interface EventProvider<P, S> {

    <E extends Event<P, S>> void subscribe(Class<E> clazz, Consumer<E> handler);

    <C extends Event<?, ?>, E extends C> void fire(Class<C> clazz, E event);

}
