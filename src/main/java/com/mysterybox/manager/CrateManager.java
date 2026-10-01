package com.mysterybox.manager;

import com.mysterybox.MysteryBoxesPlugin;
import com.mysterybox.model.Crate;
import com.mysterybox.model.CrateKey;
import com.mysterybox.model.CrateReward;
import com.mysterybox.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class CrateManager {

    private final MysteryBoxesPlugin plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private File file;
    private FileConfiguration config;

    public CrateManager(MysteryBoxesPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), "crates.yml");
        if (!file.exists()) {
            plugin.saveResource("crates.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(file);
        crates.clear();

        ConfigurationSection cratesSection = config.getConfigurationSection("crates");
        if (cratesSection == null) {
            plugin.getLogger().warning("Nenhuma seção 'crates' encontrada em crates.yml!");
            return;
        }

        for (String crateId : cratesSection.getKeys(false)) {
            try {
                ConfigurationSection sec = cratesSection.getConfigurationSection(crateId);
                if (sec == null) continue;

                String name = sec.getString("name", crateId);

                // Carregar chave
                ConfigurationSection keySec = sec.getConfigurationSection("key");
                CrateKey key = null;
                if (keySec != null) {
                    Material mat = Material.matchMaterial(keySec.getString("material", "TRIPWIRE_HOOK"));
                    String keyName = keySec.getString("name", "&eChave");
                    List<String> keyLore = keySec.getStringList("lore");
                    boolean glowing = keySec.getBoolean("glowing", true);
                    key = new CrateKey(crateId, mat, keyName, keyLore, glowing);
                }

                // Carregar recompensas
                List<CrateReward> rewards = new ArrayList<>();
                ConfigurationSection rewardsSec = sec.getConfigurationSection("rewards");
                if (rewardsSec != null) {
                    for (String rewardId : rewardsSec.getKeys(false)) {
                        ConfigurationSection rSec = rewardsSec.getConfigurationSection(rewardId);
                        if (rSec == null) continue;

                        double chance = rSec.getDouble("chance", 10.0);
                        
                        // Item de exibição
                        ItemStack displayItem = parseItem(rSec.getConfigurationSection("display"));
                        if (displayItem == null) {
                            displayItem = new ItemStack(Material.CHEST);
                        }

                        // Itens físicos a entregar
                        List<ItemStack> items = new ArrayList<>();
                        if (rSec.contains("items")) {
                            List<Map<?, ?>> itemMaps = rSec.getMapList("items");
                            for (Map<?, ?> map : itemMaps) {
                                ItemStack it = parseItemFromMap(map);
                                if (it != null) items.add(it);
                            }
                        }

                        // Comandos
                        List<String> commands = rSec.getStringList("commands");

                        // Broadcast
                        boolean broadcast = rSec.getBoolean("broadcast", false);
                        String broadcastMsg = rSec.getString("broadcast-message", "");

                        rewards.add(new CrateReward(rewardId, chance, displayItem, items, commands, broadcast, broadcastMsg));
                    }
                }

                Crate crate = new Crate(crateId, name, key, rewards);
                crates.put(crateId.toLowerCase(), crate);
                plugin.getLogger().info("Caixa carregada: " + crateId + " (" + rewards.size() + " recompensas)");

            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao carregar caixa '" + crateId + "': " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private ItemStack parseItem(ConfigurationSection sec) {
        if (sec == null) return null;
        String matName = sec.getString("material", "STONE");
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;

        int amount = sec.getInt("amount", 1);
        ItemBuilder builder = new ItemBuilder(mat, amount);

        if (sec.contains("name")) {
            builder.name(sec.getString("name"));
        }
        if (sec.contains("lore")) {
            builder.lore(sec.getStringList("lore"));
        }
        if (sec.contains("enchantments")) {
            for (String ench : sec.getStringList("enchantments")) {
                builder.enchantFromString(ench);
            }
        }
        if (sec.getBoolean("glowing", false)) {
            builder.glow(true);
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private ItemStack parseItemFromMap(Map<?, ?> map) {
        if (map == null) return null;
        String matName = map.containsKey("material") ? map.get("material").toString() : "STONE";
        Material mat = Material.matchMaterial(matName);
        if (mat == null) mat = Material.STONE;

        int amount = 1;
        if (map.containsKey("amount")) {
            try {
                amount = Integer.parseInt(map.get("amount").toString());
            } catch (NumberFormatException ignored) {}
        }

        ItemBuilder builder = new ItemBuilder(mat, amount);

        if (map.containsKey("name")) {
            builder.name(map.get("name").toString());
        }
        if (map.containsKey("lore") && map.get("lore") instanceof List) {
            builder.lore((List<String>) map.get("lore"));
        }
        if (map.containsKey("enchantments") && map.get("enchantments") instanceof List) {
            for (Object ench : (List<?>) map.get("enchantments")) {
                if (ench != null) builder.enchantFromString(ench.toString());
            }
        }

        return builder.build();
    }

    public Crate getCrate(String crateId) {
        if (crateId == null) return null;
        return crates.get(crateId.toLowerCase());
    }

    public Collection<Crate> getCrates() {
        return crates.values();
    }

    public Set<String> getCrateIds() {
        return crates.keySet();
    }

    public void reload() {
        load();
    }
}
