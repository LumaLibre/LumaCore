package dev.lumas.lumacore.reflect;

import com.google.common.reflect.ClassPath;
import dev.lumas.lumacore.utility.Logging;
import lombok.Getter;

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

@Getter
public final class ReflectionUtil {

    private final String basePackage;
    private final List<String> whitelistedPackages;
    private final Class<?> base;

    public ReflectionUtil(Class<?> base) {
        this.base = base;
        this.basePackage = base.getPackage().getName();
        this.whitelistedPackages = new ArrayList<>();
    }

    public static ReflectionUtil of(Class<?> base) {
        return new ReflectionUtil(base);
    }

    public void whitelistPackages(String... packages) {
        whitelistPackages(true, packages);
    }

    public void whitelistPackages(boolean appendToBase, String... packages) {
        for (String pack : packages) {
            if (appendToBase) {
                whitelistedPackages.add(basePackage + "." + pack);
            } else {
                whitelistedPackages.add(pack);
            }
        }
    }

    public Set<Class<?>> getAllClassesFor(Class<?>... classes) {
        List<String> packages;

        if (!whitelistedPackages.isEmpty()) {
            packages = whitelistedPackages;
        } else {
            try {
                packages = getAllPackages();
            } catch (IOException e) {
                Logging.errorLog("An error occurred while searching for classes!", e);
                return Set.of();
            }
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

    public Set<Class<?>> findClasses(String packageName, List<Class<?>> classes) throws IOException {
        ClassLoader classLoader = base.getClassLoader();

        Set<Class<?>> foundClasses = ClassPath.from(classLoader)
                .getTopLevelClasses(packageName)
                .stream()
                .map(ClassPath.ClassInfo::getName)
                .map(name -> {
                    try {
                        return Class.forName(name, false, classLoader);
                    } catch (ClassNotFoundException e) {
                        Logging.errorLog("Error while loading class", e);
                        return null;
                    } catch (NoClassDefFoundError e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(clazz -> {
                    try {
                        return !clazz.isAnnotationPresent(ReflectIgnore.class);
                    } catch (NoClassDefFoundError | TypeNotPresentException e) {
                        return false;
                    }
                })
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

    public List<String> getAllPackages() throws IOException {
        List<String> packages = new ArrayList<>();
        try (JarFile jarFile = new JarFile(base.getProtectionDomain().getCodeSource().getLocation().getPath())) {
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').replace(".class", "");
                    if (!className.startsWith(basePackage)) {
                        continue;
                    }

                    String packageName = className.substring(0, className.lastIndexOf('.'));
                    if (!packages.contains(packageName)) {
                        packages.add(packageName);
                    }
                }
            }

        }
        return packages;
    }


}