package com.mysterybox.manager;

import com.mysterybox.MysteryBoxesPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LocationManager {

    private final MysteryBoxesPlugin plugin;
    private final File file;
    private FileConfiguration config;
    private final Map<String, String> crateLocations = new HashMap<>();
    private BukkitTask particleTask;

    public LocationManager(MysteryBoxesPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "locations.yml");
        load();
        startParticleTask();
    }

    public void load() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Não foi possível criar locations.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
        crateLocations.clear();

        if (config.contains("locations")) {
            for (String key : config.getConfigurationSection("locations").getKeys(false)) {
                String crateId = config.getString("locations." + key);
                crateLocations.put(key, crateId);
            }
        }
    }

    public void save() {
        config.set("locations", null);
        for (Map.Entry<String, String> entry : crateLocations.entrySet()) {
            config.set("locations." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar locations.yml: " + e.getMessage());
        }
    }

    public static String toKey(Location loc) {
        if (loc == null || loc.getWorld() == null) return "";
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    public static Location fromKey(String key) {
        if (key == null) return null;
        String[] split = key.split(",");
        if (split.length != 4) return null;
        World world = Bukkit.getWorld(split[0]);
        if (world == null) return null;
        try {
            int x = Integer.parseInt(split[1]);
            int y = Integer.parseInt(split[2]);
            int z = Integer.parseInt(split[3]);
            return new Location(world, x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setCrate(Location loc, String crateId) {
        crateLocations.put(toKey(loc), crateId);
        save();
    }

    public boolean removeCrate(Location loc) {
        String removed = crateLocations.remove(toKey(loc));
        if (removed != null) {
            save();
            return true;
        }
        return false;
    }

    public String getCrateId(Location loc) {
        return crateLocations.get(toKey(loc));
    }

    public boolean isCrate(Location loc) {
        return crateLocations.containsKey(toKey(loc));
    }

    public Map<String, String> getCrateLocations() {
        return crateLocations;
    }

    public void startParticleTask() {
        if (particleTask != null) {
            particleTask.cancel();
        }

        if (!plugin.getConfig().getBoolean("animation.ambient-particles", true)) {
            return;
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (String key : crateLocations.keySet()) {
                Location loc = fromKey(key);
                if (loc != null && loc.getWorld() != null && loc.isWorldLoaded()) {
                    // Partícula suave saindo do topo do bloco
                    Location particleLoc = loc.clone().add(0.5, 1.05, 0.5);
                    loc.getWorld().spawnParticle(Particle.PORTAL, particleLoc, 3, 0.15, 0.1, 0.15, 0.02);
                }
            }
        }, 20L, 20L);
    }

    public void stopParticleTask() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }
    }
}
