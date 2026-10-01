package com.mysterybox.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material != null ? material : Material.STONE, Math.max(1, amount));
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack itemStack) {
        this.item = itemStack != null ? itemStack.clone() : new ItemStack(Material.STONE);
        this.meta = this.item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(ColorUtil.color(name));
        }
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        if (meta != null && lore != null) {
            meta.setLore(ColorUtil.color(lore));
        }
        return this;
    }

    public ItemBuilder lore(String... lines) {
        return lore(Arrays.asList(lines));
    }

    public ItemBuilder addLore(String... lines) {
        if (meta != null && lines != null) {
            List<String> current = meta.hasLore() && meta.getLore() != null ? meta.getLore() : new ArrayList<>();
            for (String line : lines) {
                current.add(ColorUtil.color(line));
            }
            meta.setLore(current);
        }
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null && enchantment != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder enchantFromString(String enchantStr) {
        if (enchantStr == null || !enchantStr.contains(":")) return this;
        String[] split = enchantStr.split(":");
        String name = split[0].trim().toLowerCase();
        int level = 1;
        try {
            level = Integer.parseInt(split[1].trim());
        } catch (NumberFormatException ignored) {}

        Enchantment ench = getEnchantment(name);
        if (ench != null) {
            enchant(ench, level);
        }
        return this;
    }

    @SuppressWarnings("deprecation")
    private Enchantment getEnchantment(String name) {
        try {
            NamespacedKey key = NamespacedKey.minecraft(name.replace("minecraft:", ""));
            Enchantment ench = Registry.ENCHANTMENT.get(key);
            if (ench != null) return ench;
        } catch (Exception ignored) {}

        try {
            return Enchantment.getByName(name.toUpperCase());
        } catch (Exception ignored) {}

        return null;
    }

    public ItemBuilder glow(boolean glowing) {
        if (meta != null && glowing) {
            try {
                meta.setEnchantmentGlintOverride(true);
            } catch (Throwable t) {
                meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
        }
        return this;
    }

    public ItemBuilder flags(ItemFlag... flags) {
        if (meta != null && flags != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemBuilder setStringData(NamespacedKey key, String value) {
        if (meta != null && key != null && value != null) {
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
}
