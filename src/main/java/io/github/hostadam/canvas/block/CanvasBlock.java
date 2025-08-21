package io.github.hostadam.canvas.block;

import io.github.hostadam.canvas.block.types.BlockType;
import io.github.hostadam.utilities.item.ItemParser;
import io.papermc.paper.datacomponent.DataComponentType;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

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
        ItemStack cloned = this.itemStack.clone();
        cloned.copyDataFrom(this.itemStack, dataComponentType -> true);
        return cloned;
    }

    public void apply(Block block) {
        this.type.apply(block);
    }
}
