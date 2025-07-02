package com.github.hostadam.ares.command.example;

import com.github.hostadam.ares.command.AresCommand;
import com.github.hostadam.ares.command.tabcompletion.TabCompletionMapper;
import com.github.hostadam.ares.command.context.CommandContext;
import com.github.hostadam.ares.command.tabcompletion.TabCompletions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Optional;

public class Test {

    @AresCommand(
            labels = "sethealth",
            description = "Set the health of a player",
            usage = "<player> [health]",
            permission = "command.sethealth"
    )
    @TabCompletionMapper(key = "player", mappedClass = Player.class)
    public void test(CommandContext ctx) {
        Player player = ctx.getArgument("player", Player.class, Component.text("Invalid player."));
        double health = ctx.getArgument("health", 20.0d);
    }

    @AresCommand(
            labels = "heal",
            description = "Heal a player",
            usage = "[player]",
            permission = "command.heal"
    )
    @TabCompletionMapper(key = "player", mappedClass = Player.class)
    public void heal(CommandContext ctx) {
        Optional<Player> optional = ctx.getArgument("player", Player.class);
        optional.ifPresentOrElse(target -> {
            target.setHealth(20.0d);
            target.sendMessage(Component.text("You have been healed!", NamedTextColor.GREEN));
        }, () -> {
            Player sender = ctx.sender(Player.class, Component.text("Only players may do this", NamedTextColor.RED));
            sender.setHealth(20.0d);
            sender.sendMessage(Component.text("You have been healed!", NamedTextColor.GREEN));
        });
    }
}
