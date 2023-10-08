package br.com.syrxmc.bot.core.listeners.events;

@FunctionalInterface
public interface IDynamicHandler<T> {
    void onEvent(T event);
}
