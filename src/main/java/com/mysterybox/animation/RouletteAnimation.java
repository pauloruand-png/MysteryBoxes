package com.mysterybox.animation;

import com.mysterybox.MysteryBoxesPlugin;
import com.mysterybox.model.Crate;
import com.mysterybox.model.CrateReward;
import com.mysterybox.util.ColorUtil;
import com.mysterybox.util.ItemBuilder;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class RouletteAnimation implements InventoryHolder {

    private static final Map<UUID, RouletteAnimation> ACTIVE_ANIMATIONS = new ConcurrentHashMap<>();

    private final MysteryBoxesPlugin plugin;
    private final Player player;
    private final Crate crate;
    private final CrateReward winningReward;
    private final Inventory inventory;

    private final List<CrateReward> track = new ArrayList<>();
    private int currentStep = 0;
    private final int totalSteps = 30; // Número de roladas
    private final int winningTrackIndex; // Índice exato na fita que cairá no slot 13
    private boolean finished = false;
    private BukkitTask task;

    public RouletteAnimation(MysteryBoxesPlugin plugin, Player player, Crate crate) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.winningReward = crate.chooseRandomReward();
        this.inventory = Bukkit.createInventory(this, 27, ColorUtil.color("&8Caixa: " + crate.getName()));

        // O slot central da linha do meio é o 13 (posição visível 4 na linha de 9 itens: 9, 10, 11, 12, [13], 14, 15, 16, 17)
        // Quando currentStep == totalSteps, o item no slot 13 será track.get(currentStep + 4)
        this.winningTrackIndex = totalSteps + 4;

        generateTrack();
        setupInventory();
    }

    private void generateTrack() {
        List<CrateReward> rewards = crate.getRewards();
        int trackSize = totalSteps + 15;

        for (int i = 0; i < trackSize; i++) {
            if (i == winningTrackIndex) {
                track.add(winningReward);
            } else {
                track.add(rewards.get(ThreadLocalRandom.current().nextInt(rewards.size())));
            }
        }
    }

    private void setupInventory() {
        ItemStack topBottomGlass = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
        ItemStack pointerTop = new ItemBuilder(Material.HOPPER)
                .name("&e&l▼ &6&lRECOMPENSA &e&l▼")
                .lore("&7O item que parar aqui será seu!")
                .build();
        ItemStack pointerBottom = new ItemBuilder(Material.REDSTONE_TORCH)
                .name("&e&l▲ &6&lRECOMPENSA &e&l▲")
                .build();

        for (int i = 0; i < 9; i++) {
            if (i == 4) {
                inventory.setItem(i, pointerTop);
                inventory.setItem(i + 18, pointerBottom);
            } else {
                inventory.setItem(i, topBottomGlass);
                inventory.setItem(i + 18, topBottomGlass);
            }
        }

        renderRow(0);
    }

    private void renderRow(int startIndex) {
        for (int i = 0; i < 9; i++) {
            int trackIndex = startIndex + i;
            if (trackIndex < track.size()) {
                inventory.setItem(9 + i, track.get(trackIndex).getDisplayItem());
            }
        }
    }

    public void start() {
        ACTIVE_ANIMATIONS.put(player.getUniqueId(), this);
        player.openInventory(inventory);

        scheduleNextStep(1);
    }

    private void scheduleNextStep(int delayTicks) {
        if (finished || !player.isOnline()) {
            finish(false);
            return;
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (finished || !player.isOnline()) {
                    finish(false);
                    return;
                }

                currentStep++;
                renderRow(currentStep);

                // Efeito sonoro
                float pitch = 0.8f + ((float) currentStep / (float) totalSteps) * 0.8f;
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, pitch);

                if (currentStep >= totalSteps) {
                    finish(true);
                } else {
                    int nextDelay = calculateDelay(currentStep);
                    scheduleNextStep(nextDelay);
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }

    private int calculateDelay(int step) {
        int remaining = totalSteps - step;
        if (remaining > 15) return 1;
        if (remaining > 10) return 2;
        if (remaining > 6) return 3;
        if (remaining > 3) return 5;
        if (remaining > 1) return 8;
        return 12; // desaceleração final
    }

    public synchronized void finish(boolean showVictoryEffects) {
        if (finished) return;
        finished = true;

        if (task != null) {
            task.cancel();
        }

        ACTIVE_ANIMATIONS.remove(player.getUniqueId());

        if (winningReward != null && player.isOnline()) {
            // Entrega da recompensa
            winningReward.give(player, crate.getName());

            String rewardWonMsg = plugin.getConfig().getString("messages.reward-won", "&aVocê ganhou: %reward%&a!")
                    .replace("%reward%", winningReward.getDisplayName());
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + rewardWonMsg));

            if (showVictoryEffects) {
                // Efeito visual no inventário: ilumina as bordas com vidro verde
                ItemStack winGlass = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).name("&a&lVENCEDOR!").build();
                inventory.setItem(3, winGlass);
                inventory.setItem(5, winGlass);
                inventory.setItem(12, winGlass);
                inventory.setItem(14, winGlass);
                inventory.setItem(21, winGlass);
                inventory.setItem(23, winGlass);

                // Sons de vitória
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);

                // Fogos de artifício caso configurado
                if (plugin.getConfig().getBoolean("animation.spawn-firework", true)) {
                    spawnFirework(player.getLocation());
                }

                // Fecha o inventário após 2.5 segundos
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory)) {
                            player.closeInventory();
                        }
                    }
                }.runTaskLater(plugin, 50L);
            }
        }
    }

    private void spawnFirework(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        Firework fw = world.spawn(location.clone().add(0, 1, 0), Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.ORANGE, Color.YELLOW, Color.GREEN)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    public static boolean isOpening(UUID playerId) {
        return ACTIVE_ANIMATIONS.containsKey(playerId);
    }

    public static RouletteAnimation getAnimation(UUID playerId) {
        return ACTIVE_ANIMATIONS.get(playerId);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
