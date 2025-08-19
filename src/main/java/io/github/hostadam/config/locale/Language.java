package io.github.hostadam.config.locale;

import io.github.hostadam.Ares;
import io.github.hostadam.AresImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class Language {

    private final Ares ares;
    @NonNull
    private final LanguageFile languageFile;

    public void reload() {
        this.languageFile.load();
    }

    public boolean contains(String path) {
        return this.languageFile.contains(path);
    }

    public void send(String path, CommandSender sender, PlaceholderProvider provider) {
        resolve(path, provider, sender::sendMessage);
    }

    public void broadcast(String path, String permission, PlaceholderProvider provider) {
        broadcast(path, player -> player.hasPermission(permission) || player.isOp(), provider);
    }

    public void broadcast(String path, Predicate<Player> predicate, PlaceholderProvider provider) {
        resolve(path, provider, message -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(predicate.test(player)) {
                    player.sendMessage(message);
                }
            }
        });
    }

    public void broadcast(String path, Iterable<Player> players, PlaceholderProvider provider) {
        resolve(path, provider, message -> players.forEach(player -> player.sendMessage(message)));
    }

    public void broadcast(String path, PlaceholderProvider provider) {
        resolve(path, provider, Bukkit::broadcast);
    }

    public void send(String path, CommandSender sender) {
        resolve(path, sender::sendMessage);
    }

    public void broadcast(String path, Predicate<Player> predicate) {
        resolve(path, message -> {
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(predicate.test(player)) {
                    player.sendMessage(message);
                }
            }
        });
    }

    public void broadcast(String path, Iterable<Player> players) {
        resolve(path, message -> players.forEach(player -> player.sendMessage(message)));
    }

    public void broadcast(String path, String permission) {
        resolve(path, message -> Bukkit.broadcast(message, permission));
    }

    private void resolve(String path, Consumer<Component> consumer) {
        CompletableFuture<Component> future = this.fetchAsync(path);
        future.whenComplete((component, throwable) -> this.ares.async().execute(() -> consumer.accept(component)));
    }

    private void resolve(String path, PlaceholderProvider provider, Consumer<Component> consumer) {
        CompletableFuture<Component> future = this.fetchAsync(path, provider);
        future.whenComplete((component, throwable) -> this.ares.async().execute(() -> consumer.accept(component)));
    }

    public Component fetch(String key) {
        return this.fetch(key, PlaceholderProvider.empty());
    }

    public Component fetch(String key, PlaceholderProvider provider) {
        if(!this.languageFile.contains(key)) {
            return Component.text("Language path '" + key + "' is not valid.", NamedTextColor.RED);
        }

        return this.languageFile.resolve(key, provider);
    }

    public CompletableFuture<Component> fetchAsync(String key) {
        return this.fetchAsync(key, PlaceholderProvider.empty());
    }

    public CompletableFuture<Component> fetchAsync(String key, PlaceholderProvider provider) {
        if(!this.languageFile.contains(key)) {
            return CompletableFuture.completedFuture(Component.text("Language path '" + key + "' is not valid.", NamedTextColor.RED));
        }

        return CompletableFuture.supplyAsync(() -> this.languageFile.resolve(key, provider));
    }
}
