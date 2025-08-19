package io.github.hostadam.canvas.events;

import io.github.hostadam.AresImpl;
import io.github.hostadam.api.events.TexturedBlockBreakEvent;
import io.github.hostadam.api.events.TexturedBlockPlaceEvent;
import io.github.hostadam.canvas.CanvasRegistry;
import io.github.hostadam.canvas.block.CanvasBlock;
import lombok.AllArgsConstructor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.world.GenericGameEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@AllArgsConstructor
public class BlockEvents implements Listener {

    private final AresImpl ares;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if(event.isCancelled()) return;
        Block block = event.getBlockPlaced();
        ItemStack itemStack = event.getItemInHand();

        Optional<CanvasBlock> optional = this.ares.canvas().getByItem(itemStack);
        optional.ifPresentOrElse(canvasBlock -> {
            canvasBlock.apply(block);
            new TexturedBlockBreakEvent(canvasBlock, block).callEvent();
        }, () -> {
            CanvasRegistry canvas = this.ares.canvas();
            if(canvas.shouldCancelPlacement(itemStack.getType())) {
                event.setCancelled(true);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if(event.isCancelled()) return;
        Block block = event.getBlock();
        Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(block);
        optional.ifPresent(canvasBlock -> new TexturedBlockPlaceEvent(canvasBlock, block).callEvent());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDrop(BlockDropItemEvent event) {
        if(event.isCancelled()) return;
        Block block = event.getBlock();
        Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(block);
        optional.ifPresent(canvasBlock -> {
            event.setCancelled(true);

            World world = block.getWorld();
            Location location = block.getLocation();

            if(canvasBlock.isDropItem()) {
                world.dropItemNaturally(location, canvasBlock.getItemStack());
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysics(BlockPhysicsEvent event) {
        if(event.isCancelled()) return;
        Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(event.getBlock());
        optional.ifPresent(instance -> event.setCancelled(true));

        Optional<CanvasBlock> sourceOptional = this.ares.canvas().getByBlock(event.getSourceBlock());
        sourceOptional.ifPresent(instance -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysics(BlockReceiveGameEvent event) {
        if(event.isCancelled() || !event.getEvent().getKey().equals(GameEvent.NOTE_BLOCK_PLAY.getKey())) return;
        Block source = event.getBlock();
        Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(source);
        optional.ifPresent(instance -> event.setCancelled(true));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for(Block block : event.getBlocks()) {
            Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(block);
            if(optional.isPresent()) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for(Block block : event.getBlocks()) {
            Optional<CanvasBlock> optional = this.ares.canvas().getByBlock(block);
            if(optional.isPresent()) {
                event.setCancelled(true);
                break;
            }
        }
    }
}
