package dev.lumas.core.manager;

import dev.lumas.core.model.Service;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Manager for tracking registered services.
 * @see Service
 */
@NullMarked
public final class Services {

    private static final Set<Service> tracked = new HashSet<>();

    /**
     * Adds a service to the tracked services.
     * @param service The service to add.
     */
    @ApiStatus.Internal
    public static void addTracked(Service service) {
        tracked.add(service);
    }

    /**
     * Removes a service from the tracked services.
     * @param service The service to remove.
     */
    @ApiStatus.Internal
    public static void removeTracked(Service service) {
        tracked.remove(service);
    }

    /**
     * Gets a copy of the tracked services.
     * @return A copy of the tracked services.
     */
    public static Set<Service> getTracked() {
        return Set.copyOf(tracked);
    }

    /**
     * Gets a tracked service by class.
     * @param clazz The class of the service to get.
     * @return The tracked service, or null if not found.
     */
    @Nullable
    public static Service getTracked(Class<?> clazz) {
        return tracked.stream().filter(service -> service.getClass().equals(clazz)).findFirst().orElse(null);
    }

    /**
     * Gets an optional tracked service by class.
     * @param clazz The class of the service to get.
     * @return The tracked service, or empty if not found.
     */
    public static Optional<Service> optionalTracked(Class<?> clazz) {
        return tracked.stream().filter(service -> service.getClass().equals(clazz)).findFirst();
    }

    /**
     * Checks if a service is tracked.
     * @param clazz The class of the service to check.
     * @return True if the service is tracked, false otherwise.
     */
    public static boolean isTracked(Class<?> clazz) {
        return tracked.stream().anyMatch(service -> service.getClass().equals(clazz));
    }
}
