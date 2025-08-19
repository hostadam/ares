package io.github.hostadam.api.compatibility;

import io.github.hostadam.utilities.world.SafeLocation;
import io.papermc.paper.threadedregions.scheduler.*;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

@AllArgsConstructor
public class FoliaHelper {

    private final JavaPlugin owningPlugin;

    private final RegionScheduler regionScheduler;
    private final GlobalRegionScheduler globalScheduler;
    private final AsyncScheduler asyncScheduler;

    public FoliaHelper(JavaPlugin owningPlugin) {
        this.owningPlugin = owningPlugin;

        final Server server = Bukkit.getServer();
        this.regionScheduler = server.getRegionScheduler();
        this.asyncScheduler = server.getAsyncScheduler();
        this.globalScheduler = server.getGlobalRegionScheduler();
    }

    private EntityScheduler getSchedulerFor(Entity entity) {
        return entity.getScheduler();
    }

    public void executeRegion(Location location, Runnable runnable) {
        this.regionScheduler.execute(this.owningPlugin, location, runnable);
    }

    public void executeRegion(SafeLocation location, Runnable runnable) {
        this.regionScheduler.execute(this.owningPlugin, location.getWorld(), location.blockX(), location.blockZ(), runnable);
    }

    public void executeRegion(Location location, Consumer<Location> consumer) {
        this.executeRegion(location, (() -> consumer.accept(location)));
    }

    public void executeRegion(Entity entity, Runnable runnable) {
        this.executeRegion(entity.getLocation(), runnable);
    }

    public <T extends Entity> boolean executeEntity(T entity, Runnable runnable, Runnable retired) {
        return getSchedulerFor(entity).execute(this.owningPlugin, runnable, retired, 1);
    }

    public <T extends Entity> boolean executeEntity(T entity, Runnable runnable) {
        return getSchedulerFor(entity).execute(this.owningPlugin, runnable, null, 1);
    }

    public void executeGlobal(Runnable runnable) {
        this.executeGlobal(runnable, false);
    }

    public void executeGlobal(Runnable runnable, boolean async) {
        if(async) {
            this.asyncScheduler.runNow(this.owningPlugin, scheduledTask -> runnable.run());
        } else {
            this.globalScheduler.execute(this.owningPlugin, runnable);
        }
    }
}
