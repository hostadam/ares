/*
 * MIT License
 * Copyright (c) 2026 Hostadam
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.hostadam.utilities.item;

import com.github.hostadam.utilities.AdventureUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.SoundEventKeys;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class ItemDeserializer {

    private final ItemFactory factory;
    private final Map<String, ItemComponent> deserializableComponentMap = new HashMap<>();

    public ItemDeserializer(ItemFactory factory) {
        this.factory = factory;

        this.registerSimpleComponents();
        this.registerModernComponents();
    }

    public ItemStack fromConfigurationSection(ConfigurationSection section) {
        Material material = Material.getMaterial(section.getString("material", "stone").toUpperCase());
        if (material == null) return null;

        ItemBuilder builder = this.factory.newBuilder(material);
        for (String key : this.deserializableComponentMap.keySet()) {
            if (!section.contains(key)) continue;
            ItemComponent component = this.deserializableComponentMap.get(key);
            component.apply(builder, key, section);
        }

        return builder.build();
    }

    /**
     * Register all "simple" components for ItemBuilder deserialization.
     */
    private void registerSimpleComponents() {
        // Whether an item should glow (enchantment glint)
        this.registerComponent("glow", (builder, key, section) -> builder.withData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, section.getBoolean(key)));
        // Whether an item should be unbreakable
        this.registerComponent("unbreakable", (builder, key, section) -> builder.withData(DataComponentTypes.UNBREAKABLE, section.getBoolean(key)));
        // The durability of the item
        this.registerComponent("durability", (builder, key, section) -> builder.withData(DataComponentTypes.DAMAGE, section.getInt(key)));
        // The max durability of the item
        this.registerComponent("max-durability", (builder, key, section) -> builder.withData(DataComponentTypes.MAX_DAMAGE, section.getInt(key)));
        // The max stack size of the item
        this.registerComponent("max-stack-size", (builder, key, section) -> builder.withData(DataComponentTypes.MAX_STACK_SIZE, section.getInt(key)));
        // The amount of the item
        this.registerComponent("amount", (builder, key, section) -> builder.withAmount(section.getInt(key, 1)));
        // The custom, display name of the item
        this.registerComponent("name", (builder, key, section) -> builder.withName(section.getString(key)));
        // The skin of the item (given it is a player skull)
        this.registerComponent("playerSkull", (builder, key, section) -> builder.withPlayerSkullSkin(section.getString(key)));
        // The custom item name
        this.registerComponent("item-name", (builder, key, section) -> builder.withDataOrIgnore(DataComponentTypes.ITEM_NAME, AdventureUtils.parseMiniMessage(section.getString(key))));
        // Whether the item is a glider or not
        this.registerComponent("glider", (builder, key, section) -> builder.withData(DataComponentTypes.GLIDER, section.getBoolean(key)));
        // The key for the tooltip style
        this.registerComponent("tooltip-style", (builder, key, section) -> builder.withDataOrIgnore(DataComponentTypes.TOOLTIP_STYLE, AdventureUtils.key(section.getString(key)).orElse(null)));
        // The key for the item model
        this.registerComponent("item-model", (builder, key, section) -> builder.withDataOrIgnore(DataComponentTypes.ITEM_MODEL, AdventureUtils.key(section.getString(key)).orElse(null)));
        // The custom lore
        this.registerComponent("lore", (builder, key, section) -> builder.withLore(
                section.getStringList(key).stream().map(AdventureUtils::parseMiniMessage).toList()
        ));

        // Item flags
        this.registerComponent("itemFlags", (builder, key, section) -> builder.withItemFlags(
                section.getStringList(key).stream().map(string -> ItemFlag.valueOf(string.toUpperCase())).toArray(ItemFlag[]::new))
        );

        // The rarity of the item
        this.registerComponent("rarity", (builder, key, section) -> {
            ItemRarity rarity = ItemRarity.valueOf(Objects.requireNonNull(section.getString(key)).toUpperCase());
            builder.withData(DataComponentTypes.RARITY, rarity);
        });


        this.registerComponent("damage-resistance", (builder, path, section) -> {
            AdventureUtils.key(section.getString(path)).map(key -> TagKey.create(RegistryKey.DAMAGE_TYPE, key)).ifPresent(key -> {
                builder.withData(DataComponentTypes.DAMAGE_RESISTANT, DamageResistant.damageResistant(key));
            });
        });

        this.registerComponent("enchantments", (builder, key, section) -> {
            ConfigurationSection enchantments = section.getConfigurationSection(key);
            for(String enchantmentName : enchantments.getKeys(false)) {
                Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
                if(enchantment == null) continue;
                builder.withEnchant(enchantment, enchantments.getInt(enchantmentName, enchantment.getStartLevel()));
            }
        });

        this.registerComponent("attributes", (builder, key, section) -> {
            ConfigurationSection attributeSection = section.getConfigurationSection(key);
            if(attributeSection != null) {
                ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.itemAttributes();

                for(String attributeName : attributeSection.getKeys(false)) {
                    ConfigurationSection attributeSubSection = attributeSection.getConfigurationSection(attributeName);
                    if(attributeSubSection == null) continue;
                    Optional<Key> optional = AdventureUtils.key(attributeName);
                    if(optional.isEmpty()) continue;

                    Attribute attribute = Registry.ATTRIBUTE.get(optional.get());
                    if(attribute != null) {
                        modifiers.addModifier(attribute, AttributeModifier.deserialize(attributeSubSection.getValues(false)));
                    }
                }

                builder.withData(DataComponentTypes.ATTRIBUTE_MODIFIERS, modifiers.build());
            }
        });
    }

    /**
     * Register all 1.19+ components for ItemBuilder deserialization.
     */
    private void registerModernComponents() {
        this.registerComponent("components.food", (builder, key, section) -> {
            ConfigurationSection foodSection = section.getConfigurationSection(key);
            if(foodSection == null) return;
            FoodProperties food = builder.retrieveData(DataComponentTypes.FOOD);
            builder.withData(DataComponentTypes.FOOD, FoodProperties.food()
                    .nutrition(section.getInt("nutrition", food != null ? food.nutrition() : 0))
                    .saturation((float) section.getDouble("saturation", food != null ? food.saturation() : 0.0d))
                    .canAlwaysEat(section.getBoolean("can-always-eat", food != null && food.canAlwaysEat()))
                    .build()
            );
        });

        this.registerComponent("components.equippable", (builder, key, section) -> {
            ConfigurationSection equippableSection = section.getConfigurationSection(key);
            if(equippableSection == null) return;
            Equippable equippable = builder.retrieveData(DataComponentTypes.EQUIPPABLE);

            EquipmentSlot slot = EquipmentSlot.valueOf(equippableSection.getString("slot"));
            Equippable.Builder newBuilder = Equippable.equippable(slot)
                    .dispensable(equippableSection.getBoolean("dispensable", equippable != null && equippable.dispensable()))
                    .damageOnHurt(equippableSection.getBoolean("damage-on-hurt", equippable != null && equippable.damageOnHurt()))
                    .swappable(equippableSection.getBoolean("swappable", equippable != null && equippable.swappable()))
                    .canBeSheared(equippableSection.getBoolean("can-be-sheared", equippable != null && equippable.canBeSheared()))
                    .equipOnInteract(equippableSection.getBoolean("equip-on-interact", equippable != null && equippable.equipOnInteract()))
                    .assetId(AdventureUtils.key(equippableSection.getString("asset-id")).orElse(equippable != null ? equippable.assetId() : null))
                    .cameraOverlay(AdventureUtils.key(equippableSection.getString("camera-overlay")).orElse(equippable != null ? equippable.cameraOverlay() : null))
                    .equipSound(AdventureUtils.key(equippableSection.getString("equip-sound")).orElse(equippable != null ? equippable.equipSound() : SoundEventKeys.ITEM_ARMOR_EQUIP_GENERIC));

            if(equippableSection.contains("allowed-entities")) {
                RegistryKeySet<EntityType> types = RegistrySet.keySet(RegistryKey.ENTITY_TYPE, equippableSection.getStringList("allowed-entities")
                        .stream().map(AdventureUtils::key)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(RegistryKey.ENTITY_TYPE::typedKey)
                        .toList()
                );
                newBuilder.allowedEntities(types);
            }

            builder.withData(DataComponentTypes.EQUIPPABLE, newBuilder.build());
        });

        this.registerComponent("components.custom-model-data", (builder, key, section) -> {
            ConfigurationSection customModelDataSection = section.getConfigurationSection(key);
            if(customModelDataSection == null) return;

            CustomModelData.Builder newBuilder = CustomModelData.customModelData();

            if(customModelDataSection.contains("strings")) {
                newBuilder.addStrings(customModelDataSection.getStringList("strings"));
            }

            if(customModelDataSection.contains("floats")) {
                newBuilder.addFloats(customModelDataSection.getFloatList("floats"));
            }

            if(customModelDataSection.contains("flags")) {
                newBuilder.addFlags(customModelDataSection.getBooleanList("flags"));
            }

            if(customModelDataSection.contains("colors")) {
                newBuilder.addColors(customModelDataSection.getIntegerList("colors").stream().map(Color::fromARGB).toList());
            }

            builder.withData(DataComponentTypes.CUSTOM_MODEL_DATA, newBuilder.build());
        });

        this.registerComponent("components.tool", (builder, key, section) -> {
            ConfigurationSection toolSection = section.getConfigurationSection(key);
            if(toolSection == null) return;

            Tool tool = builder.retrieveData(DataComponentTypes.TOOL);
            Tool.Builder newBuilder = Tool.tool()
                    .damagePerBlock(section.getInt("damage-per-block", tool != null ? tool.damagePerBlock() : 0))
                    .defaultMiningSpeed((float) section.getDouble("default-mining-speed", tool != null ? tool.defaultMiningSpeed() : 0.0d));

            if(toolSection.contains("rules")) {
                ConfigurationSection ruleSection = section.getConfigurationSection("rules");
                for(String ruleName : ruleSection.getKeys(false)) {
                    RegistryKeySet<BlockType> types = RegistrySet.keySet(RegistryKey.BLOCK, ruleSection.getStringList(ruleName + ".materials").stream()
                            .map(AdventureUtils::key)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(RegistryKey.BLOCK::typedKey)
                            .toList()
                    );

                    float speed = (float) ruleSection.getDouble(ruleName + ".speed", 1.0f);
                    boolean correctForDrops = ruleSection.getBoolean(ruleName + ".correct-for-drops");
                    newBuilder.addRule(Tool.rule(types, speed, TriState.byBoolean(correctForDrops)));
                }
            }

            builder.withData(DataComponentTypes.TOOL, newBuilder.build());
        });

        this.registerComponent("components.consumable", (builder, key, section) -> {
            ConfigurationSection consumableSection = section.getConfigurationSection(key);
            if(consumableSection == null) return;
            Consumable consumable = builder.retrieveData(DataComponentTypes.CONSUMABLE);
            Consumable.Builder newBuilder = Consumable.consumable()
                    .animation(ItemUseAnimation.valueOf(section.getString("animation", "EAT")))
                    .consumeSeconds((float) section.getDouble("consume-seconds", consumable != null ? consumable.consumeSeconds() : 0.0d))
                    .hasConsumeParticles(section.getBoolean("has-consume-particles", consumable != null && consumable.hasConsumeParticles()))
                    .sound(AdventureUtils.key(section.getString("sound")).orElse(SoundEventKeys.ENTITY_GENERIC_EAT));

            if(section.contains("effects")) {
                ConfigurationSection effectSection = section.getConfigurationSection("effects");
                for(String effectName : effectSection.getKeys(false)) {
                    String type = effectSection.getString(effectName + ".type");
                    if(type == null) continue;
                    switch (type.toLowerCase()) {
                        case "teleport_randomly":
                            newBuilder.addEffect(ConsumeEffect.teleportRandomlyEffect((float) effectSection.getDouble(key + ".diameter")));
                            break;
                        case "clear_status_effects":
                            newBuilder.addEffect(ConsumeEffect.clearAllStatusEffects());
                            break;
                        case "remove_status_effects":
                            RegistryKeySet<PotionEffectType> types = RegistrySet.keySet(RegistryKey.MOB_EFFECT, effectSection.getStringList(key + ".types")
                                    .stream().map(AdventureUtils::key)
                                    .filter(Optional::isPresent)
                                    .map(Optional::get)
                                    .map(RegistryKey.MOB_EFFECT::typedKey)
                                    .toList()
                            );
                            newBuilder.addEffect(ConsumeEffect.removeEffects(types));
                            break;
                        case "apply_status_effects":
                            ConfigurationSection effectSubSection = effectSection.getConfigurationSection(key + ".effects");
                            List<PotionEffect> potionEffects = new ArrayList<>();
                            for(String unused : effectSubSection.getKeys(false)) {
                                ConfigurationSection subSection = effectSubSection.getConfigurationSection(unused);
                                potionEffects.add(new PotionEffect(subSection.getValues(false)));
                            }

                            newBuilder.addEffect(ConsumeEffect.applyStatusEffects(potionEffects, (float) effectSection.getDouble(key + ".probability", 1.0)));
                            break;
                        case "play_sound":
                            Optional<Key> namespacedKeyOptional = AdventureUtils.key(effectSection.getString(key + ".key"));
                            namespacedKeyOptional.ifPresent(namespacedKey -> newBuilder.addEffect(ConsumeEffect.playSoundConsumeEffect(namespacedKey)));
                            break;
                        default:
                    }
                }
            }

            builder.withData(DataComponentTypes.CONSUMABLE, newBuilder.build());
        });
    }

    private void registerComponent(String key, ItemComponent component) {
        this.deserializableComponentMap.put(key, component);
    }
}
