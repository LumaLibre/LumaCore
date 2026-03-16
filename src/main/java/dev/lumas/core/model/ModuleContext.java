package dev.lumas.core.model;

import org.bukkit.plugin.Plugin;

public record ModuleContext(Plugin plugin, String fallbackPrefix) {

}