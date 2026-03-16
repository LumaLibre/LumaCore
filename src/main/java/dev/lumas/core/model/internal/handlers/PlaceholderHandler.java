package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.LumaCore;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.model.placeholder.AbstractPlaceholder;
import dev.lumas.core.model.placeholder.AbstractPlaceholderManager;
import dev.lumas.core.model.placeholder.SoloAbstractPlaceholder;
import dev.lumas.core.util.Logging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderHandler implements RegisterHandler<Object> {

    private final Map<AbstractPlaceholderManager<?, ?>, List<AbstractPlaceholder<?>>> managers = new LinkedHashMap<>();
    private final List<AbstractPlaceholder<?>> queuedPlaceholders = new ArrayList<>();

    @Override
    public void register(Object instance, ModuleContext ctx) {
        if (!LumaCore.isWithPlaceholderAPI()) return;

        if (instance instanceof AbstractPlaceholderManager<?, ?> manager) {
            manager.register();
            managers.put(manager, new ArrayList<>());
        } else if (instance instanceof SoloAbstractPlaceholder solo) {
            solo.register(); // register and forget
        } else if (instance instanceof AbstractPlaceholder<?> placeholder) {
            queuedPlaceholders.add(placeholder);
        }
    }

    @Override
    public void unregister(Object instance, ModuleContext ctx) {
        if (!LumaCore.isWithPlaceholderAPI()) return;

        if (instance instanceof SoloAbstractPlaceholder solo) {
            solo.unregister();
        }
    }

    @Override
    public void postProcess(ModuleContext context) {
        for (AbstractPlaceholder<?> placeholder : queuedPlaceholders) {
            AbstractPlaceholderManager<?, ?> parent = managers.keySet().stream()
                    .filter(m -> placeholder.parent().isInstance(m))
                    .findFirst()
                    .orElse(null);

            if (parent == null) {
                Logging.warningLog("No parent PlaceholderManager for: " + placeholder.getClass().getSimpleName());
                continue;
            }
            parent.addPlaceholder(placeholder);
        }
        queuedPlaceholders.clear();
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof SoloAbstractPlaceholder || instance instanceof AbstractPlaceholder<?>;
    }
}
