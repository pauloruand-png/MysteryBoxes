package com.mysterybox.manager;

import com.mysterybox.MysteryBoxesPlugin;
import com.mysterybox.model.Crate;
import com.mysterybox.model.CrateKey;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class KeyManager {

    private final MysteryBoxesPlugin plugin;
    private final NamespacedKey keyTag;

    public KeyManager(MysteryBoxesPlugin plugin) {
        this.plugin = plugin;
        this.keyTag = new NamespacedKey(plugin, "crate_key_id");
    }

    public NamespacedKey getKeyTag() {
        return keyTag;
    }

    public ItemStack createKey(String crateId, int amount) {
        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null || crate.getKey() == null) {
            return null;
        }
        return crate.getKey().toItemStack(amount, keyTag);
    }

    public boolean isKey(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(keyTag, PersistentDataType.STRING);
    }

    public String getCrateIdFromKey(ItemStack item) {
        if (!isKey(item)) return null;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.get(keyTag, PersistentDataType.STRING);
    }

    public boolean matchesKey(ItemStack item, String crateId) {
        String keyCrateId = getCrateIdFromKey(item);
        return keyCrateId != null && keyCrateId.equalsIgnoreCase(crateId);
    }

    public boolean hasKey(Player player, String crateId) {
        if (player == null || crateId == null) return false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (matchesKey(item, crateId)) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeKey(Player player, String crateId) {
        if (player == null || crateId == null) return false;

        // Verifica primeiro a mão principal
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (matchesKey(mainHand, crateId)) {
            if (mainHand.getAmount() > 1) {
                mainHand.setAmount(mainHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            return true;
        }

        // Verifica mão secundária
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (matchesKey(offHand, crateId)) {
            if (offHand.getAmount() > 1) {
                offHand.setAmount(offHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInOffHand(null);
            }
            return true;
        }

        // Procura no inventário
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (matchesKey(item, crateId)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItem(i, null);
                }
                return true;
            }
        }

        return false;
    }
}
