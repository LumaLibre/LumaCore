package dev.lumas.lumacore.reflect;

import dev.lumas.core.manager.Reflect;
import lombok.Getter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @deprecated Use {@link dev.lumas.core.manager.Reflect}
 */
@Getter
@Deprecated
public final class ReflectionUtil {

    private final String basePackage;
    private final List<String> whitelistedPackages;
    private final Class<?> base;
    private final Reflect reflect;

    public ReflectionUtil(Class<?> base) {
        this.base = base;
        this.basePackage = base.getPackage().getName();
        this.whitelistedPackages = new java.util.ArrayList<>();
        this.reflect = Reflect.from(base);
    }

    public static ReflectionUtil of(Class<?> base) {
        return new ReflectionUtil(base);
    }

    public void whitelistPackages(String... packages) {
        whitelistPackages(true, packages);
    }

    public void whitelistPackages(boolean appendToBase, String... packages) {
        reflect.packages(packages);
        if (!appendToBase) reflect.absolute();
        for (String pack : packages) {
            if (appendToBase) {
                whitelistedPackages.add(basePackage + "." + pack);
            } else {
                whitelistedPackages.add(pack);
            }
        }
    }

    public Set<Class<?>> getAllClassesFor(Class<?>... classes) {
        return reflect.scan(classes);
    }

    public Set<Class<?>> findClasses(String packageName, List<Class<?>> classes) throws IOException {
        return Reflect.from(base)
                .absolute()
                .packages(packageName)
                .scan(classes.toArray(new Class<?>[0]));
    }

    public List<String> getAllPackages() throws IOException {
        return Reflect.from(base).getAllPackages(basePackage);
    }

    public static boolean classExists(String className) {
        return Reflect.classExists(className);
    }
}