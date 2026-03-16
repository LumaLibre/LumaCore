package dev.lumas.core.annotation;

import dev.lumas.core.model.Service;
import dev.lumas.core.model.command.AbstractCommand;
import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.core.model.placeholder.AbstractPlaceholder;
import dev.lumas.core.model.placeholder.SoloAbstractPlaceholder;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Defines the categories of components that can be registered
 * through the LumaCore framework.
 *
 * @see Register
 */
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
    PLACEHOLDER(() -> resolveClasses(
            "dev.lumas.core.model.placeholder.SoloAbstractPlaceholder",
            "dev.lumas.core.model.placeholder.AbstractPlaceholder"
    )),
    /**
     * Auto calling of {@link Service#register()}
     * and {@link Service#unregister()} methods of
     * classes implementing {@link Service}.
     */
    SERVICE(Service.class);

    private Class<?> @Nullable [] handleTypes;
    private @Nullable Supplier<Class<?>[]> supplier;

    Autowire(Class<?>... handleTypes) {
        this.handleTypes = handleTypes;
    }

    Autowire(Supplier<Class<?>[]> supplier) {
        this.supplier = supplier;
    }


    public Class<?>[] getHandleTypes() {
        if (handleTypes == null) {
            if (supplier == null) return new Class<?>[0];
            handleTypes = supplier.get();
            supplier = null;
        }
        return handleTypes;
    }


    public boolean isHandleType(Class<?> aClass) {
        for (Class<?> handleType : getHandleTypes()) {
            if (handleType.isAssignableFrom(aClass)) {
                return true;
            }
        }
        return false;
    }

    private static Class<?>[] resolveClasses(String... classNames) {
        return Arrays.stream(classNames)
                .map(name -> {
                    try {
                        return Class.forName(name);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Class<?>[]::new);
    }
}
