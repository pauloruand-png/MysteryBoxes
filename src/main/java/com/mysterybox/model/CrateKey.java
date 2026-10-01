package com.mysterybox.model;

import com.mysterybox.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CrateKey {

    private final String crateId;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final boolean glowing;

    public CrateKey(String crateId, Material material, String name, List<String> lore, boolean glowing) {
        this.crateId = crateId;
        this.material = material != null ? material : Material.TRIPWIRE_HOOK;
        this.name = name != null ? name : "&eChave";
        this.lore = lore != null ? lore : new ArrayList<>();
        this.glowing = glowing;
    }

    public String getCrateId() {
        return crateId;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public ItemStack toItemStack(int amount, NamespacedKey keyTag) {
        return new ItemBuilder(material, amount)
                .name(name)
                .lore(lore)
                .glow(glowing)
                .setStringData(keyTag, crateId)
                .build();
    }
}
