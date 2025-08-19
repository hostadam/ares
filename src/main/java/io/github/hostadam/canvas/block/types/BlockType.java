package io.github.hostadam.canvas.block.types;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

public interface BlockType {

    Material getMaterial();
    void apply(Block block);
    boolean matches(Block other);

    static BlockType deserialize(ConfigurationSection section) {
        String id = section.getString("type");
        if(id == null) {
            throw new IllegalArgumentException("Missing 'tye' field in section: " + section.getName());
        }

        return switch(id.toUpperCase()) {
            case "MULTI_FACING" -> MultiFacingType.from(section);
            case "NOTE", "NOTE_BLOCK" -> NoteBlockType.from(section);
            default -> throw new IllegalArgumentException("Unknown block type: " + id);
        };
    }
}
