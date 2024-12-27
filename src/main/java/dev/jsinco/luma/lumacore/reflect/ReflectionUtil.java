package dev.jsinco.luma.lumacore.reflect;

import com.google.common.reflect.ClassPath;
import dev.jsinco.luma.lumacore.utility.Logging;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public final class ReflectionUtil {

    public final String basePackage;
    private final Class<?> base;

    public ReflectionUtil(Class<?> base) {
        this.base = base;
        this.basePackage = base.getPackage().getName();
    }

    public static ReflectionUtil of(Class<?> base) {
        return new ReflectionUtil(base);
    }

    public Set<Class<?>> getAllClassesFor(@Nullable Class<?>... classes) {
        List<String> packages;
        try {
            packages = getAllPackages();
        } catch (IOException e) {
            Logging.errorLog("An error occurred while searching for classes!", e);
            return Set.of();
        }

        Set<Class<?>> allClasses = new HashSet<>();

        for (String pack : packages) {
            try {
                allClasses.addAll(findClasses(pack, List.of(classes)));
            } catch (IOException e) {
                Logging.errorLog("Error while Looking for classes", e);
            }
        }
        return allClasses;
    }

    private Set<Class<?>> findClasses(String packageName, List<Class<?>> classes) throws IOException {
        ClassLoader classLoader = base.getClassLoader();

        Set<Class<?>> foundClasses = ClassPath.from(classLoader)
                .getTopLevelClasses(packageName)
                .stream()
                .map(ClassPath.ClassInfo::getName)
                .map(name -> {
                    try {
                        return Class.forName(name, false, base.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        Logging.errorLog("Error while loading class", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(clazz -> !clazz.isAnnotationPresent(ReflectIgnore.class))
                .collect(Collectors.toSet());

        if (classes.isEmpty()) {
            return foundClasses;
        }

        Set<Class<?>> returnableClasses = new HashSet<>();

        for (Class<?> clazz : foundClasses) {
            for (Class<?> aClass : classes) {
                if (aClass.isAssignableFrom(clazz)) {
                    returnableClasses.add(clazz);
                    break;
                }
            }
        }

        return returnableClasses;
    }

    private List<String> getAllPackages() throws IOException {
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


}
