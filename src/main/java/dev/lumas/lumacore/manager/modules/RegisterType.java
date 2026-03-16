package dev.lumas.lumacore.manager.modules;

import dev.lumas.core.annotation.Autowire;

/**
 * @deprecated Use {@link dev.lumas.core.annotation.Autowire}
 */
@Deprecated
public enum RegisterType {
    LISTENER,
    COMMAND,
    SUBCOMMAND,
    PLACEHOLDER,
    SERVICE;


    public Autowire toNewHandle() {
        return Autowire.valueOf(name());
    }
}
