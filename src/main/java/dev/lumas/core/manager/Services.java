package dev.lumas.core.manager;

import dev.lumas.core.model.Service;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@NullMarked
public final class Services {

    private static final Set<Service> tracked = new HashSet<>();

    public static void addTracked(Service service) {
        tracked.add(service);
    }

    public static void removeTracked(Service service) {
        tracked.remove(service);
    }

    public static Set<Service> getTracked() {
        return Set.copyOf(tracked);
    }

    @Nullable
    public static Service getTracked(Class<?> clazz) {
        return tracked.stream().filter(service -> service.getClass().equals(clazz)).findFirst().orElse(null);
    }

    public static Optional<Service> optionalTracked(Class<?> clazz) {
        return tracked.stream().filter(service -> service.getClass().equals(clazz)).findFirst();
    }

    public static boolean isTracked(Class<?> clazz) {
        return tracked.stream().anyMatch(service -> service.getClass().equals(clazz));
    }
}
