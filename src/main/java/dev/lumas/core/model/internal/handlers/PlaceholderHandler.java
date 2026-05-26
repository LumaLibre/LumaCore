package dev.lumas.core.model.internal.handlers;

import dev.lumas.core.LumaCore;
import dev.lumas.core.model.ModuleContext;
import dev.lumas.core.model.internal.RegisterHandler;
import dev.lumas.core.model.placeholder.AbstractPlaceholder;
import dev.lumas.core.model.placeholder.AbstractPlaceholderManager;
import dev.lumas.core.model.placeholder.SoloAbstractPlaceholder;
import dev.lumas.core.util.Logging;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@NullMarked
public class PlaceholderHandler implements RegisterHandler<Object> {

    private final Map<AbstractPlaceholderManager<?, ?>, List<AbstractPlaceholder<?>>> managers = new LinkedHashMap<>();
    private final List<AbstractPlaceholder<?>> queuedPlaceholders = new ArrayList<>();

    @Override
    public void register(Object instance, ModuleContext ctx) {
        if (!LumaCore.isPlaceholderAPI()) return;

        switch (instance) {
            case AbstractPlaceholderManager<?, ?> manager -> {
                manager.register();
                managers.put(manager, new ArrayList<>());
            }
            case SoloAbstractPlaceholder solo -> solo.register(); // register and forget
            case AbstractPlaceholder<?> placeholder -> queuedPlaceholders.add(placeholder);
            default -> {}
        }
    }

    @Override
    public void unregister(Object instance, ModuleContext ctx) {
        if (!LumaCore.isPlaceholderAPI()) return;

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
            parent.addUntyped(placeholder);
        }
        queuedPlaceholders.clear();
    }

    @Override
    public boolean accepts(Object instance) {
        return instance instanceof SoloAbstractPlaceholder || instance instanceof AbstractPlaceholder<?>;
    }
}
