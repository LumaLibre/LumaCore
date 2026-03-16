package dev.lumas.core.util;

import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.PlaceholderMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.internal.command.CommandAnnotation;
import dev.lumas.core.model.internal.command.CommandInfoHolder;
import dev.lumas.core.model.internal.command.CommandMetaHolder;
import dev.lumas.core.model.internal.placeholder.PlaceholderAnnotation;
import dev.lumas.core.model.internal.placeholder.PlaceholderInfoHolder;
import dev.lumas.core.model.internal.placeholder.PlaceholderMetaHolder;
import dev.lumas.core.model.internal.register.AutoRegisterAnnotation;
import dev.lumas.core.model.internal.register.RegisterAnnotation;
import dev.lumas.core.model.internal.register.RegisterHolder;
import dev.lumas.lumacore.manager.commands.CommandInfo;
import dev.lumas.lumacore.manager.modules.AutoRegister;
import dev.lumas.lumacore.manager.placeholder.PlaceholderInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Annotations {

    @Nullable
    public static CommandAnnotation getCommandMeta(Object object) {
        return getCommandMeta(object.getClass());
    }

    @Nullable
    public static CommandAnnotation getCommandMeta(Class<?> clazz) {
        CommandMeta meta = clazz.getAnnotation(dev.lumas.core.annotation.CommandMeta.class);
        if (meta != null) {
            return new CommandMetaHolder(meta);
        }

        CommandInfo info = clazz.getAnnotation(dev.lumas.lumacore.manager.commands.CommandInfo.class);
        if (info != null) {
            return new CommandInfoHolder(info);
        }
        return null;
    }

    @Nullable
    public static PlaceholderAnnotation getPlaceholderMeta(Object object) {
        return getPlaceholderMeta(object.getClass());
    }

    @Nullable
    public static PlaceholderAnnotation getPlaceholderMeta(Class<?> clazz) {
        PlaceholderMeta meta = clazz.getAnnotation(PlaceholderMeta.class);
        if (meta != null) {
            return new PlaceholderMetaHolder(meta);
        }

        PlaceholderInfo info = clazz.getAnnotation(PlaceholderInfo.class);
        if (info != null) {
            return new PlaceholderInfoHolder(info);
        }
        return null;
    }

    @Nullable
    public static RegisterAnnotation getRegisterAnnotation(Object object) {
        return getRegisterAnnotation(object.getClass());
    }

    @Nullable
    public static RegisterAnnotation getRegisterAnnotation(Class<?> clazz) {
        Register register = clazz.getAnnotation(Register.class);
        if (register != null) {
            return new RegisterHolder(register);
        }

        AutoRegister autoRegister = clazz.getAnnotation(AutoRegister.class);
        if (autoRegister != null) {
            return new AutoRegisterAnnotation(autoRegister);
        }
        return null;
    }
}
