package io.github.hostadam.utilities;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class ServerUtils {

    public static Optional<Location> generateRandomLocation(World world, int minX, int minZ, int maxX, int maxZ, int maxAttempts, Predicate<Location> condition) {
        for(int attempt = 0; attempt < maxAttempts; attempt++) {
            int x = ThreadLocalRandom.current().nextInt(minX, maxX);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ);

            Location highest = world.getHighestBlockAt(x, z).getLocation();
            if(condition.test(highest)) {
                return Optional.of(highest.clone().add(0.0, 1.0, 0.0));
            }
        }

        return Optional.empty();
    }

    public static void transferPlayer(JavaPlugin originPlugin, Player player, String server) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("Connect");
        output.writeUTF(server);
        player.sendPluginMessage(originPlugin, "BungeeCord", output.toByteArray());
    }

    public static void kickPlayer(JavaPlugin originPlugin, OfflinePlayer target, String reason) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("KickPlayer");
        output.writeUTF(Objects.requireNonNull(target.getName()));
        output.writeUTF(reason);
        Bukkit.getServer().sendPluginMessage(originPlugin, "BungeeCord", output.toByteArray());
    }
}
