package com.mysterybox.listener;

import com.mysterybox.MysteryBoxesPlugin;
import com.mysterybox.animation.RouletteAnimation;
import com.mysterybox.gui.PreviewGUI;
import com.mysterybox.model.Crate;
import com.mysterybox.util.ColorUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class CrateInteractListener implements Listener {

    private final MysteryBoxesPlugin plugin;

    public CrateInteractListener(MysteryBoxesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        // Evita disparar duas vezes (mão principal e secundária)
        if (event.getHand() != EquipmentSlot.HAND) return;

        Location loc = event.getClickedBlock().getLocation();
        if (!plugin.getLocationManager().isCrate(loc)) return;

        Player player = event.getPlayer();
        String crateId = plugin.getLocationManager().getCrateId(loc);
        Crate crate = plugin.getCrateManager().getCrate(crateId);

        if (crate == null) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cEsta caixa misteriosa não está configurada corretamente."));
            event.setCancelled(true);
            return;
        }

        // Clique com botão esquerdo: Abrir Prévia de Recompensas
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Se for admin no criativo agachado, deixa quebrar ou avisa
            if (player.getGameMode() == GameMode.CREATIVE && player.isSneaking() && player.hasPermission("mysteryboxes.admin")) {
                return;
            }

            event.setCancelled(true);
            if (player.hasPermission("mysteryboxes.preview")) {
                new PreviewGUI(crate).open(player);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
            }
            return;
        }

        // Clique com botão direito: Abrir Caixa Misteriosa com a Chave
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);

            if (!player.hasPermission("mysteryboxes.open")) {
                player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.no-permission")));
                return;
            }

            if (RouletteAnimation.isOpening(player.getUniqueId())) {
                player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.already-opening")));
                return;
            }

            // Verificar se o jogador possui a chave necessária
            if (!plugin.getKeyManager().hasKey(player, crateId)) {
                String keyName = crate.getKey() != null ? crate.getKey().getName() : "&eChave";
                String needKeyMsg = plugin.getConfig().getString("messages.need-key", "&cVocê precisa de uma %key% para abrir esta caixa!")
                        .replace("%key%", keyName);
                player.sendMessage(ColorUtil.color(plugin.getPrefix() + needKeyMsg));

                String tipMsg = plugin.getConfig().getString("messages.left-click-preview", "&7Dica: Clique com botão esquerdo para ver os prêmios.");
                player.sendMessage(ColorUtil.color(tipMsg));

                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            // Consumir 1 chave e iniciar a roleta
            boolean consumed = plugin.getKeyManager().consumeKey(player, crateId);
            if (!consumed) {
                player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cErro ao consumir a chave!"));
                return;
            }

            String openingMsg = plugin.getConfig().getString("messages.opening", "&aAbrindo a caixa misteriosa...");
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + openingMsg));

            new RouletteAnimation(plugin, player, crate).start();
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (plugin.getLocationManager().isCrate(loc)) {
            Player player = event.getPlayer();
            // Permite quebrar apenas se for admin agachado no criativo
            if (player.getGameMode() == GameMode.CREATIVE && player.isSneaking() && player.hasPermission("mysteryboxes.admin")) {
                plugin.getLocationManager().removeCrate(loc);
                player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&eCaixa misteriosa removida do bloco quebrado."));
                return;
            }

            event.setCancelled(true);
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.cannot-break-crate")));
        }
    }
}
