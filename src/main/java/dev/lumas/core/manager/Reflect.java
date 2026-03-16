package dev.lumas.core.manager;

import com.google.common.reflect.ClassPath;
import dev.lumas.core.annotation.ReflectIgnore;
import dev.lumas.core.util.Logging;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * A utility manager for scanning packages and reflectively loading classes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Reflect {

    private Class<?> base;
    private final Set<String> packages = new HashSet<>();
    private boolean inverted = false;
    private boolean appendToBase = true;

    /**
     * Creates a new {@link Reflect} instance.
     * @param base The base class to scan for classes.
     * @return A new {@link Reflect} instance.
     */
    public static Reflect from(Class<?> base) {
        Reflect reflect = new Reflect();
        reflect.base = base;
        return reflect;
    }

    /**
     * Adds packages to the scan. If empty, all packages will be scanned.
     * @param packages The packages to scan.
     * @return The current Reflect instance.
     */
    public Reflect packages(String... packages) {
        for (String pack : packages) {
            if (appendToBase) {
                this.packages.add(base.getPackage().getName() + "." + pack);
            } else {
                this.packages.add(pack);
            }
        }
        return this;
    }

    /**
     * Scans for classes in the specified packages, ignoring the base package.
     * @return The current Reflect instance.
     */
    public Reflect absolute() {
        this.appendToBase = false;
        return this;
    }

    /**
     * Inverts the filter logic. When set, only classes that do NOT match the filters will be returned.
     * @return The current Reflect instance.
     */
    public Reflect inverted() {
        this.inverted = true;
        return this;
    }

    /**
     * Scans for classes in the specified packages that are assignable from the given filters.
     * If no filters are provided, all classes will be returned.
     * @param filters The filters to apply.
     * @return A set of classes that match the filters.
     */
    public Set<Class<?>> scan(Class<?>... filters) {
        String basePackage = base.getPackage().getName();
        Collection<String> targetPackages;

        if (!packages.isEmpty()) {
            targetPackages = packages;
        } else {
            try {
                targetPackages = getAllPackages(basePackage);
            } catch (IOException e) {
                Logging.errorLog("An error occurred while scanning for classes!", e);
                return Set.of();
            }
        }

        Set<Class<?>> found = new HashSet<>();
        for (String pack : targetPackages) {
            try {
                found.addAll(loadClasses(pack));
            } catch (IOException e) {
                Logging.errorLog("Error while scanning for classes", e);
            }
        }

        if (filters.length == 0) return found;

        return found.stream()
                .filter(clazz -> {
                    boolean matches = Arrays.stream(filters)
                            .anyMatch(filter -> filter.isAssignableFrom(clazz));
                    return inverted != matches;
                })
                .collect(Collectors.toSet());
    }

    private Set<Class<?>> loadClasses(String packageName) throws IOException {
        ClassLoader classLoader = base.getClassLoader();

        return ClassPath.from(classLoader)
                .getTopLevelClasses(packageName)
                .stream()
                .map(info -> {
                    try {
                        return Class.forName(info.getName(), false, classLoader);
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(clazz -> {
                    try {
                        return !clazz.isAnnotationPresent(ReflectIgnore.class) && !clazz.isAnnotationPresent(dev.lumas.lumacore.reflect.ReflectIgnore.class);
                    } catch (NoClassDefFoundError | TypeNotPresentException e) {
                        return false;
                    }
                })
                .collect(Collectors.toSet());
    }

    public List<String> getAllPackages(String basePackage) throws IOException {
        List<String> packages = new ArrayList<>();
        try (JarFile jarFile = new JarFile(base.getProtectionDomain().getCodeSource().getLocation().getPath())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    if (className.startsWith(basePackage)) {
                        String packageName = className.substring(0, className.lastIndexOf('.'));
                        if (!packages.contains(packageName)) {
                            packages.add(packageName);
                        }
                    }
                }
            }
        }
        return packages;
    }

    /**
     * Checks if a class exists.
     * @param className The canonical name of the class to check.
     * @return True if the class exists, false otherwise.
     */
    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
