package com.mysterybox.listener;

import com.mysterybox.animation.RouletteAnimation;
import com.mysterybox.gui.PreviewGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnimationListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInv = event.getView().getTopInventory();

        if (topInv.getHolder() instanceof RouletteAnimation) {
            event.setCancelled(true);
            return;
        }

        if (topInv.getHolder() instanceof PreviewGUI) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.BARRIER) {
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (topInv.getHolder() instanceof RouletteAnimation || topInv.getHolder() instanceof PreviewGUI) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (topInv.getHolder() instanceof RouletteAnimation) {
            RouletteAnimation anim = (RouletteAnimation) topInv.getHolder();
            // Se o jogador fechar o inventário durante a roleta, garante a entrega imediata do prêmio
            anim.finish(false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (RouletteAnimation.isOpening(player.getUniqueId())) {
            RouletteAnimation anim = RouletteAnimation.getAnimation(player.getUniqueId());
            if (anim != null) {
                anim.finish(false);
            }
        }
    }
}
