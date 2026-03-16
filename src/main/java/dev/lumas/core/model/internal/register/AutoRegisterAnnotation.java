package dev.lumas.core.model.internal.register;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.lumacore.manager.modules.AutoRegister;
import dev.lumas.lumacore.manager.modules.RegisterType;

import java.util.Arrays;

public record AutoRegisterAnnotation(AutoRegister handle) implements RegisterAnnotation {
    @Override
    public Autowire[] value() {
        return Arrays.stream(handle.value()).map(RegisterType::toNewHandle)
                .toList().toArray(new Autowire[0]);
    }

    @Override
    public String requires() {
        return handle.requires();
    }
}
