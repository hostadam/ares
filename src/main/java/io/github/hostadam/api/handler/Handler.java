package io.github.hostadam.api.handler;

import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

@Getter
public abstract class Handler<T extends JavaPlugin> {

    private static final AtomicInteger READY = new AtomicInteger(0);
    protected final T reference;

    public Handler(@NotNull T reference) {
        this.reference = reference;
    }

    public void markReady() {
        READY.incrementAndGet();
    }

    public void cleanup() {
        // Override if necessary
    }

    public void register(Listener listener) {
        this.reference.getServer().getPluginManager().registerEvents(listener, this.reference);
    }

    public void execute(long delay, long repeat, Consumer<BukkitTask> consumer) {
        this.reference.getServer().getScheduler().runTaskTimer(this.reference, consumer, delay, repeat);
    }

    public void executeLater(long delay, Consumer<BukkitTask> consumer) {
        this.reference.getServer().getScheduler().runTaskLater(this.reference, consumer, delay);
    }

    public void log(String logMessage) {
        this.reference.getLogger().log(Level.INFO, logMessage);
    }

    public void error(String logMessage, Throwable throwable) {
        this.reference.getLogger().log(Level.INFO, logMessage, throwable);
    }

    public abstract void enable();
    public abstract void disable();

    public static boolean allHandlersReady(int expectedCount) {
        return READY.get() >= expectedCount;
    }
}
