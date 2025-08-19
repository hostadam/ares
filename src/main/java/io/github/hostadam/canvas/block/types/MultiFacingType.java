package io.github.hostadam.canvas.block.types;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;

public record MultiFacingType(String type, boolean[] blockFaceValues) implements BlockType {

    @Override
    public Material getMaterial() {
        return switch (type.toLowerCase()) {
            case "stem" -> Material.MUSHROOM_STEM;
            case "red" -> Material.RED_MUSHROOM_BLOCK;
            case "brown" -> Material.BROWN_MUSHROOM_BLOCK;
            case "transparent" -> Material.CHORUS_PLANT;
            default -> null;
        };
    }

    @Override
    public void apply(Block block) {
        Material material = this.getMaterial();
        if(material == null) return;

        BlockFace[] faces = BlockFace.values();
        MultipleFacing facing = (MultipleFacing) Bukkit.createBlockData(material);

        for(int index = 0; index < blockFaceValues.length; index++) {
            facing.setFace(faces[index], blockFaceValues[index]);
        }

        block.setBlockData(facing);
    }

    @Override
    public boolean matches(Block other) {
        if(other.getType() != this.getMaterial()) return false;
        BlockFace[] faces = BlockFace.values();
        MultipleFacing facing = (MultipleFacing) other.getBlockData();
        Set<BlockFace> allowedFaces = facing.getAllowedFaces();

        for(int index = 0; index < 6; index++) {
            BlockFace blockFace = faces[index];
            boolean value = this.blockFaceValues[index];
            if(allowedFaces.contains(blockFace) && value != facing.hasFace(blockFace)) {
                return false;
            }
        }

        return true;
    }

    public static MultiFacingType from(ConfigurationSection section) {
        String facesString = section.getString("faces");
        if(facesString == null) {
            throw new IllegalArgumentException("Missing 'faces' for block: " + section.getName());
        }

        String[] split = facesString.split(",");
        if(split.length != 6) {
            throw new IllegalArgumentException("Expected 6 faces, got " + split.length + " in: " + section.getName());
        }

        boolean[] blockFaces = new boolean[6];
        for(int i = 0; i < 6; i++) {
            blockFaces[i] = Boolean.parseBoolean(split[i].trim());
        }

        String type = section.getString("variant");
        if(type == null) {
            throw new IllegalArgumentException("Missing 'variant' (red/brown/stem/transparent) for block: " + section.getName());
        }

        return new MultiFacingType(type, blockFaces);
    }
}
