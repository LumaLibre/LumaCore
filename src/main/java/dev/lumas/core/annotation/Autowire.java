package dev.lumas.core.annotation;

import dev.lumas.core.model.Service;
import dev.lumas.core.model.command.AbstractCommand;
import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.core.model.placeholder.AbstractPlaceholder;
import dev.lumas.core.model.placeholder.SoloAbstractPlaceholder;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;

/**
 * Defines the categories of components that can be registered
 * through the LumaCore framework.
 */
@Getter
@NullMarked
public enum Autowire {
    /**
     * Auto registration of classes implementing
     * {@link Listener}.
     */
    LISTENER(Listener.class),
    /**
     * Auto registration of classes extending
     * {@link AbstractCommand}.
     */
    COMMAND(AbstractCommand.class),
    /**
     * Auto registration of classes extending
     * {@link AbstractSubCommand}
     */
    SUBCOMMAND(AbstractSubCommand.class),
    /**
     * Auto registration of classes extending
     * {@link SoloAbstractPlaceholder}
     * or {@link AbstractPlaceholder}.
     */
    PLACEHOLDER(SoloAbstractPlaceholder.class, AbstractPlaceholder.class),
    /**
     * Auto calling of {@link Service#register()}
     * and {@link Service#unregister()} methods of
     * classes implementing {@link Service}.
     */
    SERVICE(Service.class);

    private final Class<?>[] handleTypes;

    Autowire(Class<?>... handleTypes) {
        this.handleTypes = handleTypes;
    }

    public boolean isHandleType(Class<?> aClass) {
        for (Class<?> handleType : handleTypes) {
            if (handleType.isAssignableFrom(aClass)) {
                return true;
            }
        }
        return false;
    }
}
