package io.github.hostadam.api.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

public class AsyncBridge {

    private final JavaPlugin owningPlugin;
    private FoliaHelper foliaHelper;

    public AsyncBridge(JavaPlugin owningPlugin) {
        this.owningPlugin = owningPlugin;
        this.initFoliaHelper();
    }

    private void initFoliaHelper() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            this.foliaHelper = new FoliaHelper(this.owningPlugin);
        } catch (ClassNotFoundException ignored) { }
    }

    public void execute(Runnable runnable) {
        if(this.foliaHelper != null) {
            this.foliaHelper.executeGlobal(runnable);
            return;
        }

        if(!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(this.owningPlugin, runnable);
        } else runnable.run();
    }

    public <T> void execute(T object, Runnable runnable) {
        if(this.foliaHelper == null) {
            this.execute(runnable);
            return;
        }

        if(object instanceof Entity entity) {
            this.foliaHelper.executeEntity(entity, runnable);
        } else if(object instanceof Location location) {
            this.foliaHelper.executeRegion(location, runnable);
        } else {
            this.foliaHelper.executeGlobal(runnable);
        }
    }

    public <T> void execute(T object, Consumer<T> consumer) {
        this.execute(object, (() -> consumer.accept(object)));
    }
}
