package io.github.hostadam.canvas.block.types;

import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.configuration.ConfigurationSection;

public record NoteBlockType(Instrument instrument, Note note, boolean powered) implements BlockType {

    @Override
    public Material getMaterial() {
        return Material.NOTE_BLOCK;
    }

    @Override
    public void apply(Block block) {
        NoteBlock noteBlock = (NoteBlock) this.getMaterial().createBlockData();
        noteBlock.setInstrument(instrument);
        noteBlock.setNote(note);
        noteBlock.setPowered(powered);
        block.setBlockData(noteBlock);
    }

    @Override
    public boolean matches(Block other) {
        if(other.getType() != this.getMaterial()) return false;
        NoteBlock noteBlock = (NoteBlock) other.getBlockData();
        return noteBlock.getInstrument() == this.instrument && noteBlock.getNote().equals(this.note) && noteBlock.isPowered() == this.powered;
    }

    public static NoteBlockType from(ConfigurationSection section) {
        if(!section.contains("instrument") || !section.contains("note") || !section.contains("powered")) {
            throw new IllegalArgumentException("Missing argument in section: " + section.getName() + " (instrument, note, powered)");
        }

        String instrumentName = section.getString("instrument");
        Instrument instrument;
        try {
            instrument = Instrument.valueOf(instrumentName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid instrument: " + instrumentName, e);
        }

        int note = section.getInt("note");
        boolean powered = section.getBoolean("powered");
        return new NoteBlockType(instrument, new Note(note), powered);
    }
}
