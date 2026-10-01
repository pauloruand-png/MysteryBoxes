package com.mysterybox.gui;

import com.mysterybox.model.Crate;
import com.mysterybox.model.CrateReward;
import com.mysterybox.util.ColorUtil;
import com.mysterybox.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class PreviewGUI implements InventoryHolder {

    private final Crate crate;
    private final Inventory inventory;

    public PreviewGUI(Crate crate) {
        this.crate = crate;
        int size = calculateInventorySize(crate.getRewards().size());
        this.inventory = Bukkit.createInventory(this, size, ColorUtil.color("&8Prévia: " + crate.getName()));
        setupItems();
    }

    private int calculateInventorySize(int itemsCount) {
        if (itemsCount <= 7) return 27;
        if (itemsCount <= 16) return 36;
        if (itemsCount <= 25) return 45;
        return 54;
    }

    private void setupItems() {
        int size = inventory.getSize();

        // Painéis decorativos nas bordas
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").build();
        for (int i = 0; i < size; i++) {
            if (i < 9 || i >= size - 9 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Preencher recompensas
        double totalChance = crate.getTotalChance();
        int slot = 10;
        for (CrateReward reward : crate.getRewards()) {
            while (slot < size && (slot % 9 == 0 || slot % 9 == 8)) {
                slot++;
            }
            if (slot >= size - 9) break;

            double percent = totalChance > 0 ? (reward.getChance() / totalChance) * 100.0 : 0.0;
            ItemStack display = reward.getDisplayItem();

            ItemStack previewItem = new ItemBuilder(display)
                    .addLore(
                            "&7-----------------------",
                            "&6Probabilidade: &e" + String.format(java.util.Locale.US, "%.1f", percent) + "%",
                            "&7-----------------------"
                    )
                    .build();

            inventory.setItem(slot++, previewItem);
        }

        // Botão Fechar no rodapé
        ItemStack closeBtn = new ItemBuilder(Material.BARRIER)
                .name("&cFechar")
                .lore("&7Clique para fechar este menu.")
                .build();
        inventory.setItem(size - 5, closeBtn);
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    public Crate getCrate() {
        return crate;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
