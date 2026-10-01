package com.mysterybox.model;

import com.mysterybox.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CrateReward {

    private final String id;
    private final double chance;
    private final ItemStack displayItem;
    private final List<ItemStack> items;
    private final List<String> commands;
    private final boolean broadcast;
    private final String broadcastMessage;

    public CrateReward(String id, double chance, ItemStack displayItem,
                       List<ItemStack> items, List<String> commands,
                       boolean broadcast, String broadcastMessage) {
        this.id = id;
        this.chance = chance;
        this.displayItem = displayItem;
        this.items = items != null ? items : new ArrayList<>();
        this.commands = commands != null ? commands : new ArrayList<>();
        this.broadcast = broadcast;
        this.broadcastMessage = broadcastMessage != null ? broadcastMessage : "";
    }

    public String getId() {
        return id;
    }

    public double getChance() {
        return chance;
    }

    public ItemStack getDisplayItem() {
        return displayItem.clone();
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public List<String> getCommands() {
        return commands;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public String getBroadcastMessage() {
        return broadcastMessage;
    }

    public String getDisplayName() {
        if (displayItem.hasItemMeta() && displayItem.getItemMeta().hasDisplayName()) {
            return displayItem.getItemMeta().getDisplayName();
        }
        return displayItem.getType().name();
    }

    public void give(Player player, String crateName) {
        if (player == null || !player.isOnline()) return;

        boolean droppedAny = false;
        // Entregar itens físicos (ou displayItem caso items e commands estejam vazios)
        List<ItemStack> toGive = new ArrayList<>(items);
        if (toGive.isEmpty() && commands.isEmpty() && displayItem != null) {
            toGive.add(displayItem.clone());
        }

        for (ItemStack item : toGive) {
            if (item != null) {
                HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());
                if (!leftover.isEmpty()) {
                    droppedAny = true;
                    for (ItemStack drop : leftover.values()) {
                        player.getWorld().dropItemNaturally(player.getLocation(), drop);
                    }
                }
            }
        }

        if (droppedAny) {
            player.sendMessage(ColorUtil.color("&e[!] Seu inventário estava cheio. Parte dos itens caiu no chão!"));
        }

        // Executar comandos pelo console
        for (String cmd : commands) {
            String formatted = cmd.replace("%player%", player.getName())
                    .replace("%crate%", crateName)
                    .replace("%reward%", getDisplayName());
            if (formatted.startsWith("/")) {
                formatted = formatted.substring(1);
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formatted);
        }

        // Transmissão global caso ativado
        if (broadcast && !broadcastMessage.isEmpty()) {
            String msg = broadcastMessage.replace("%player%", player.getName())
                    .replace("%crate%", crateName)
                    .replace("%reward%", getDisplayName());
            Bukkit.broadcastMessage(ColorUtil.color(msg));
        }
    }
}
