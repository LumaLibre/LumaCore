package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.manager.Services;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.Service;
import dev.lumas.core.model.internal.RegisterHandler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ServiceHandler implements RegisterHandler<Service> {

    @Override
    public void register(Service instance, ModuleContext context) {
        instance.register();
        Services.addTracked(instance);
    }

    @Override
    public void unregister(Service instance, ModuleContext context) {
        instance.unregister();
        Services.removeTracked(instance);
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof Service;
    }
}
