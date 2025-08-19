package io.github.hostadam.canvas.block;

import io.github.hostadam.canvas.block.types.BlockType;
import io.github.hostadam.utilities.item.ItemParser;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
public class CanvasBlock {

    @Getter
    private final BlockType type;
    private final ItemStack itemStack;
    private final boolean dropItem;

    public CanvasBlock(ItemStack itemStack, ConfigurationSection section) {
        this.itemStack = itemStack;
        this.type = BlockType.deserialize(section);
        this.dropItem = section.getBoolean("drop-item", true);
    }

    public ItemStack getItemStack() {
        return this.itemStack.clone();
    }

    public void apply(Block block) {
        this.type.apply(block);
    }
}
