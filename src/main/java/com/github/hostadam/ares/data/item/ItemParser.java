package com.github.hostadam.ares.data.item;

import com.github.hostadam.ares.utils.PaperUtils;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage"})
public class ItemParser {

    public static ItemBuilder parse(ConfigurationSection section) {
        if(section == null) return null;
        Material material = Material.getMaterial(section.getString("material").toUpperCase());
        int amount = section.getInt("amount", 1);
        ItemBuilder builder = new ItemBuilder(material)
                .amount(amount)
                .glow(section.contains("glow") && section.getBoolean("glow"))
                .unbreakable(section.contains("unbreakable") && section.getBoolean("unbreakable"));

        if(section.contains("durability")) {
            builder.durability(section.getInt("durability"));
        }

        if(section.contains("max-stack-size")) {
            builder.maxStackSize(section.getInt("max-stack-size"));
        }

        if(section.contains("customModelData")) {
            builder.customModelData(section.getInt("customModelData"));
        }

        if(section.contains("playerSkull")) {
            builder.skull(section.getString("playerSkull"));
        }

        if(section.contains("dyeColor")) {
            builder.dyeColor(DyeColor.valueOf(section.getString("dyeColor").toUpperCase()));
        }

        if(section.contains("itemFlags")) {
            section.getStringList("itemFlags").stream().map(string -> ItemFlag.valueOf(string.toUpperCase())).forEach(builder::itemFlag);
        }

        if(section.contains("rarity")) {
            builder.rarity(ItemRarity.valueOf(section.getString("rarity").toUpperCase()));
        }

        if(section.contains("tooltip")) {
            builder.tooltip(section.getBoolean("tooltip"));
        }

        if(section.contains("potionColor")) {
            builder.potionColor(Color.fromRGB(section.getInt("potionColor")));
        }

        if(section.contains("displayName")) {
            builder.name(section.getString("displayName"));
        }

        if(section.contains("enchantments")) {
            for(String key : section.getConfigurationSection("enchantments").getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                if(enchantment == null) continue;
                int level = section.getInt("enchantments." + key, enchantment.getStartLevel());
                builder.enchant(enchantment, level);
            }
        }

        if(section.contains("lore")) {
            List<Component> lore = section.getStringList("lore")
                    .stream()
                    .map(PaperUtils::formatMiniMessage)
                    .toList();
            builder.lore(lore);
        }

        /* 1.19+ functionality */
        ItemStack itemStack = builder.fetchCurrentItem();
        parseModernComponents(itemStack, section);

        if(section.contains("attributes")) {
            ConfigurationSection attributesSection = section.getConfigurationSection("attributes");
            if(attributesSection != null) {
                parseAttributes(itemStack, attributesSection);
            }
        }

        if(section.contains("components")) {
            ConfigurationSection jukeboxPlayableSection = section.getConfigurationSection("jukebox");
            if(jukeboxPlayableSection != null) {
                parseJukeboxPlayableComponent(itemStack, jukeboxPlayableSection);
            }

            ConfigurationSection customModelDataSection = section.getConfigurationSection("custom-model-data");
            if(customModelDataSection != null) {
                parseCustomModelDataComponent(itemStack, customModelDataSection);
            }

            ConfigurationSection weaponSection = section.getConfigurationSection("weapon");
            if(weaponSection != null) {
                parseWeaponComponent(itemStack, weaponSection);
            }

            ConfigurationSection foodSection = section.getConfigurationSection("food");
            if(foodSection != null) {
                parseFoodComponent(itemStack, foodSection);
            }

            ConfigurationSection consumableSection = section.getConfigurationSection("consumable");
            if(consumableSection != null) {
                parseConsumableComponent(itemStack, consumableSection);
            }

            ConfigurationSection toolSection = section.getConfigurationSection("tool");
            if(toolSection != null) {
                parseToolComponent(itemStack, toolSection);
            }

            ConfigurationSection equippableSection = section.getConfigurationSection("equippable");
            if(equippableSection != null) {
                parseEquippableComponent(itemStack, equippableSection);
            }
        }

        return builder;
    }

    private static void parseModernComponents(ItemStack itemStack, ConfigurationSection section) {
        set(itemStack, DataComponentTypes.ITEM_NAME, PaperUtils.fromConfig(section, "item-name"));
        set(itemStack, DataComponentTypes.GLIDER, section.getBoolean("glider"));
        set(itemStack, DataComponentTypes.TOOLTIP_STYLE, PaperUtils.key(section.getString("tooltip-style")).orElse(null));
        set(itemStack, DataComponentTypes.ITEM_MODEL, PaperUtils.key(section.getString("item-model")).orElse(null));

        //TODO: Damage resistant
        /*
        for(String string : section.getStringList("damage-resistant")) {
                NamespacedKey key = NamespacedKey.fromString(string);
                if(key == null) continue;
                Tag<DamageType> tag = Bukkit.getTag(DamageTypeTags.REGISTRY_DAMAGE_TYPES, key, DamageType.class);
                if(tag == null) continue;
                meta.setDamageResistant(tag);
            }
         */
    }

    private static void parseAttributes(ItemStack itemStack, ConfigurationSection section) {
        ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.itemAttributes();

        for(String key : section.getKeys(false)) {
            NamespacedKey namespacedKey = NamespacedKey.fromString(key);
            if(namespacedKey == null) continue;
            Attribute attribute = Registry.ATTRIBUTE.get(namespacedKey);
            if(attribute == null) continue;
            ConfigurationSection attributeSection = section.getConfigurationSection(key);
            AttributeModifier modifier = AttributeModifier.deserialize(attributeSection.getValues(false));
            modifiers.addModifier(attribute, modifier);
        }

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers);
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseJukeboxPlayableComponent(ItemStack itemStack, ConfigurationSection section) {
        Optional<Key> optional = PaperUtils.key(section.getString("song-key"));
        optional.ifPresent(key -> {
            JukeboxSong song = RegistryAccess.registryAccess().getRegistry(RegistryKey.JUKEBOX_SONG).get(key);
            if(song == null) return;
            set(itemStack, DataComponentTypes.JUKEBOX_PLAYABLE, JukeboxPlayable.jukeboxPlayable(song).build());
        });
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseCustomModelDataComponent(ItemStack itemStack, ConfigurationSection section) {
        CustomModelData.Builder builder = CustomModelData.customModelData()
                .addStrings(section.getStringList("strings"))
                .addFloats(section.getFloatList("floats"))
                .addFlags(section.getBooleanList("flags"));

        if(section.contains("colors")) {
            ConfigurationSection colorSection = section.getConfigurationSection("colors");
            for(String key : colorSection.getKeys(false)) {
                try {
                    Color color = Color.deserialize(colorSection.getConfigurationSection(key).getValues(false));
                    builder.addColor(color);
                } catch (RuntimeException ignored) { }
            }
        }

        itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, builder.build());
    }

    private static void parseWeaponComponent(ItemStack itemStack, ConfigurationSection section) {
        Weapon weapon = itemStack.getData(DataComponentTypes.WEAPON);
        itemStack.setData(DataComponentTypes.WEAPON, Weapon.weapon()
                .disableBlockingForSeconds((float) section.getDouble("disable-blocking-for-seconds", weapon != null ? weapon.disableBlockingForSeconds() : 0.0d))
                .itemDamagePerAttack(section.getInt("damage-per-attack", weapon != null ? weapon.itemDamagePerAttack() : 0))
                .build()
        );
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseFoodComponent(ItemStack itemStack, ConfigurationSection section) {
        FoodProperties food = itemStack.getData(DataComponentTypes.FOOD);
        itemStack.setData(DataComponentTypes.FOOD, FoodProperties.food()
                .nutrition(section.getInt("nutrition", food != null ? food.nutrition() : 0))
                .saturation((float) section.getDouble("saturation", food != null ? food.saturation() : 0.0d))
                .canAlwaysEat(section.getBoolean("can-always-eat", food != null && food.canAlwaysEat()))
                .build()
        );
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseConsumableComponent(ItemStack itemStack, ConfigurationSection section) {
        Consumable consumable = itemStack.getData(DataComponentTypes.CONSUMABLE);
        itemStack.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .animation(ItemUseAnimation.valueOf(section.getString("animation", "EAT")))
                .consumeSeconds((float) section.getDouble("consume-seconds", consumable != null ? consumable.consumeSeconds() : 0.0d))
                .hasConsumeParticles(section.getBoolean("has-consume-particles", consumable != null && consumable.hasConsumeParticles()))
                .sound(PaperUtils.key(section.getString("key")).orElse(SoundEventKeys.ENTITY_GENERIC_EAT))
                .build()
        );

        //TODO: Effects
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseToolComponent(ItemStack itemStack, ConfigurationSection section) {
        Tool tool = itemStack.getData(DataComponentTypes.TOOL);
        Tool.Builder builder = Tool.tool()
                .damagePerBlock(section.getInt("damage-per-block", tool != null ? tool.damagePerBlock() : 0))
                .defaultMiningSpeed((float) section.getDouble("default-mining-speed", tool != null ? tool.defaultMiningSpeed() : 0.0d));

        if(section.contains("rules")) {
            ConfigurationSection ruleSection = section.getConfigurationSection("rules");
            for(String materialName : ruleSection.getKeys(false)) {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if(material == null) continue;
                float speed = (float) ruleSection.getDouble(materialName + ".speed", 1.0f);
                boolean correctForDrops = ruleSection.getBoolean(materialName + ".correct-for-drops");
                //TODO: Add rule
            }
        }

        itemStack.setData(DataComponentTypes.TOOL, builder.build());
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseEquippableComponent(ItemStack itemStack, ConfigurationSection section) {
        Equippable equippable = itemStack.getData(DataComponentTypes.EQUIPPABLE);
        Equippable.Builder builder = Equippable.equippable(EquipmentSlot.valueOf(section.getString("slot", "HAND")))
                .dispensable(section.getBoolean("dispensable", equippable != null && equippable.dispensable()))
                .damageOnHurt(section.getBoolean("damage-on-hurt", equippable != null && equippable.damageOnHurt()))
                .swappable(section.getBoolean("swappable", equippable != null && equippable.swappable()))
                .canBeSheared(section.getBoolean("can-be-sheared", equippable != null && equippable.canBeSheared()))
                .equipOnInteract(section.getBoolean("equip-on-interact", equippable != null && equippable.equipOnInteract()))
                .assetId(PaperUtils.key(section.getString("asset-id")).orElse(equippable != null ? equippable.equipSound() : null))
                .cameraOverlay(PaperUtils.key(section.getString("camera-overlay")).orElse(equippable != null ? equippable.equipSound() : null))
                .equipSound(PaperUtils.key(section.getString("equip-sound")).orElse(equippable != null ? equippable.equipSound() : SoundEventKeys.ITEM_ARMOR_EQUIP_GENERIC));

        if(section.contains("allowed-entities")) {
            RegistryKeySet<EntityType> types = RegistrySet.keySet(RegistryKey.ENTITY_TYPE, section.getStringList("allowed-entities")
                    .stream().map(PaperUtils::key)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(RegistryKey.ENTITY_TYPE::typedKey)
                    .toList()
            );
            builder.allowedEntities(types);
        }

        itemStack.setData(DataComponentTypes.EQUIPPABLE, builder.build());
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseBlocksAttackComponent(ItemStack itemStack, ConfigurationSection section) {
        //TODO: This
    }

    private static <T> void set(ItemStack itemStack, DataComponentType.Valued<T> type, @Nullable T value) {
        if(value == null) return;
        itemStack.setData(type, value);
    }

    private static void set(ItemStack itemStack, DataComponentType.NonValued type, boolean flag) {
        if(flag) {
            itemStack.setData(type);
        } else if(itemStack.hasData(type)) {
            itemStack.unsetData(type);
        }
    }
}
