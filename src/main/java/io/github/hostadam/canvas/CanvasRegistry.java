package io.github.hostadam.canvas;

import io.github.hostadam.canvas.block.CanvasBlock;
import io.github.hostadam.canvas.block.types.BlockType;
import io.github.hostadam.canvas.block.types.MultiFacingType;
import io.github.hostadam.canvas.block.types.NoteBlockType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CanvasRegistry {

    @Getter
    private final Set<Material> registeredBlockTypes = new HashSet<>();
    private final List<CanvasBlock> blocks = new ArrayList<>();

    public void registerBlock(CanvasBlock block) {
        this.blocks.add(block);

        BlockType type = block.getType();
        this.registeredBlockTypes.add(type.getMaterial());
    }

    public void switchTexture(Block block, CanvasBlock other) {
        other.apply(block);
    }

    public boolean shouldCancelPlacement(Material material) {
        return this.registeredBlockTypes.contains(material);
    }

    public Optional<CanvasBlock> getByBlock(Block block) {
        if(!this.registeredBlockTypes.contains(block.getType())) {
            return Optional.empty();
        }

        return this.blocks.stream().filter(texturedBlock -> texturedBlock.getType().matches(block)).findAny();
    }

    public Optional<CanvasBlock> getByItem(ItemStack bukkitItem) {
        if(bukkitItem == null || bukkitItem.getType() == Material.AIR) return Optional.empty();
        return this.blocks.stream().filter(canvasBlock -> canvasBlock.getItemStack().isSimilar(bukkitItem)).findAny();
    }
}
