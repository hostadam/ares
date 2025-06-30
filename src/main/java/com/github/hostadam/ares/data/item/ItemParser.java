package com.github.hostadam.ares.data.item;

import com.github.hostadam.ares.utils.StringUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.*;
import org.bukkit.inventory.meta.components.consumable.ConsumableComponent;
import org.bukkit.tag.DamageTypeTags;

import java.util.List;

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
            builder.name(StringUtils.formatHex(section.getString("displayName")));
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
            List<String> lore = section.getStringList("lore")
                    .stream()
                    .map(StringUtils::formatHex)
                    .toList();
            builder.lore(lore);
        }

        // TODO: https://github.com/oraxen/oraxen/blob/master/core/src/main/java/io/th0rgal/oraxen/items/ItemParser.java

        /* 1.19+ functionality */
        ItemMeta itemMeta = builder.fetchCurrentMeta();
        parseModernComponents(itemMeta, section);

        if(section.contains("components")) {
            ConfigurationSection toolComponentSection = section.getConfigurationSection("tool");
            if(toolComponentSection != null) {
                parseToolComponent(itemMeta, toolComponentSection);
            }

            ConfigurationSection weaponComponentSection = section.getConfigurationSection("weapon");
            if(weaponComponentSection != null) {
                parseWeaponComponent(itemMeta, weaponComponentSection);
            }

            ConfigurationSection foodComponentSection = section.getConfigurationSection("food");
            if(foodComponentSection != null) {
                parseFoodComponent(itemMeta, foodComponentSection);
            }

            ConfigurationSection consumableComponentSection = section.getConfigurationSection("consumable");
            if(consumableComponentSection != null) {
                parseConsumableComponent(itemMeta, consumableComponentSection);
            }

            ConfigurationSection equippableComponentSection = section.getConfigurationSection("equippable");
            if(equippableComponentSection != null) {
                parseEquippableComponent(itemMeta, equippableComponentSection);
            }
        }

        return builder;
    }

    private static void parseModernComponents(ItemMeta meta, ConfigurationSection section) {
        meta.setItemName(section.getString("item-name", meta.getItemName()));
        meta.setGlider(section.getBoolean("glider", meta.isGlider()));

        if(section.contains("tooltip-style")) {
            meta.setTooltipStyle(NamespacedKey.fromString(section.getString("tooltip-style")));
        }

        if(section.contains("item-model")) {
            NamespacedKey key = NamespacedKey.fromString(section.getString("item-model"));
            if(key != null) meta.setItemModel(key);
        }

        if(section.contains("damage-resistant")) {
            for(String string : section.getStringList("damage-resistant")) {
                NamespacedKey key = NamespacedKey.fromString(string);
                if(key == null) continue;
                Tag<DamageType> tag = Bukkit.getTag(DamageTypeTags.REGISTRY_DAMAGE_TYPES, key, DamageType.class);
                if(tag == null) continue;
                meta.setDamageResistant(tag);
            }
        }
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseWeaponComponent(ItemMeta meta, ConfigurationSection section) {
        WeaponComponent component = meta.getWeapon();
        component.setItemDamagePerAttack(Math.max(section.getInt("damage-per-attack", component.getItemDamagePerAttack()), 0));
        component.setDisableBlockingForSeconds((float) Math.max(section.getDouble("disable-blocking-for-seconds", component.getDisableBlockingForSeconds()), 0f));
        meta.setWeapon(component);
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseFoodComponent(ItemMeta meta, ConfigurationSection section) {
        FoodComponent component = meta.getFood();
        component.setNutrition(Math.max(section.getInt("nutrition", component.getNutrition()), 0));
        component.setCanAlwaysEat(section.getBoolean("can-always-eat", component.canAlwaysEat()));
        component.setSaturation((float) Math.max(section.getDouble("saturation", component.getSaturation()), 0.0f));
        meta.setFood(component);
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseConsumableComponent(ItemMeta meta, ConfigurationSection section) {
        ConsumableComponent component = meta.getConsumable();
        component.setAnimation(ConsumableComponent.Animation.valueOf(section.getString("animation", "EAT")));
        component.setConsumeParticles(section.getBoolean("consume-particles", component.hasConsumeParticles()));
        component.setConsumeSeconds((float) Math.max(section.getDouble("consume-seconds", component.getConsumeSeconds()), 0.0f));

        if(section.contains("sound")) {
            NamespacedKey key = NamespacedKey.fromString(section.getString("sound"));
            component.setSound(key == null ? component.getSound() : Registry.SOUNDS.get(key));
        }

        //TODO: Effects
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseToolComponent(ItemMeta meta, ConfigurationSection section) {
        ToolComponent component = meta.getTool();
        component.setDamagePerBlock(Math.max(section.getInt("damage-per-block", component.getDamagePerBlock()), 0));
        component.setDefaultMiningSpeed((float) Math.max(section.getDouble("mining-speed", component.getDefaultMiningSpeed()), 0f));
        component.setCanDestroyBlocksInCreative(section.getBoolean("can-destroy-blocks-in-creative", component.canDestroyBlocksInCreative()));

        if(section.contains("rules")) {
            ConfigurationSection ruleSection = section.getConfigurationSection("rules");
            for(String materialName : ruleSection.getKeys(false)) {
                Material material = Material.getMaterial(materialName.toUpperCase());
                if(material == null) continue;
                float speed = (float) ruleSection.getDouble(materialName + ".speed", 1.0f);
                boolean correctForDrops = ruleSection.getBoolean(materialName + ".correct-for-drops");
                component.addRule(material, speed, correctForDrops);
            }
        }

        meta.setTool(component);
    }

    @SuppressWarnings({ "UnstableApiUsage"})
    private static void parseEquippableComponent(ItemMeta meta, ConfigurationSection section) {
        EquippableComponent component = meta.getEquippable();
        component.setSlot(EquipmentSlot.valueOf(section.getString("slot", "BODY")));

        List<EntityType> types = section.getStringList("allowed-entities").stream().map(EntityType::valueOf).toList();
        component.setAllowedEntities(types.isEmpty() ? null : types);
        component.setDispensable(section.getBoolean("dispensable", component.isDispensable()));
        component.setCanBeSheared(section.getBoolean("can-be-sheared", component.isCanBeSheared()));
        component.setDamageOnHurt(section.getBoolean("damage-on-hurt", component.isDamageOnHurt()));
        component.setSwappable(section.getBoolean("swappable", component.isSwappable()));

        if(section.contains("equip-sound")) {
            NamespacedKey key = NamespacedKey.fromString(section.getString("equip-sound"));
            component.setEquipSound(key == null ? component.getEquipSound() : Registry.SOUNDS.get(key));
        }

        if(section.contains("model")) {
            component.setModel(NamespacedKey.fromString(section.getString("model")));
        }

        if(section.contains("camera-overlay")) {
            component.setCameraOverlay(NamespacedKey.fromString(section.getString("camera-overlay")));
        }

        meta.setEquippable(component);
    }
}
