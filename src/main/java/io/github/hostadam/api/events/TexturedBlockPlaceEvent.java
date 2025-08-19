package io.github.hostadam.api.events;

import io.github.hostadam.canvas.block.CanvasBlock;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;

@Getter
public class TexturedBlockPlaceEvent extends BlockEvent {

    private final CanvasBlock type;

    public TexturedBlockPlaceEvent(@NotNull CanvasBlock type, @NotNull Block block) {
        super(block);
        this.type = type;
    }

    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
