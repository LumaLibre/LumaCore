package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.internal.RegisterHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ListenerHandler implements RegisterHandler<Listener> {

    public void register(Listener instance, ModuleContext ctx) {
        ctx.plugin().getServer().getPluginManager().registerEvents(instance, ctx.plugin());
    }

    public void unregister(Listener instance, ModuleContext ctx) {
        HandlerList.unregisterAll(instance);
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof Listener;
    }
}