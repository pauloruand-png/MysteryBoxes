package com.mysterybox;

import com.mysterybox.command.CrateCommand;
import com.mysterybox.listener.AnimationListener;
import com.mysterybox.listener.CrateInteractListener;
import com.mysterybox.manager.CrateManager;
import com.mysterybox.manager.KeyManager;
import com.mysterybox.manager.LocationManager;
import com.mysterybox.util.ColorUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class MysteryBoxesPlugin extends JavaPlugin {

    private static MysteryBoxesPlugin instance;
    private KeyManager keyManager;
    private CrateManager crateManager;
    private LocationManager locationManager;
    private String prefix;

    @Override
    public void onEnable() {
        instance = this;

        // Salvar configuração padrão
        saveDefaultConfig();
        this.prefix = ColorUtil.color(getConfig().getString("prefix", "&6&l[CAIXA]&r "));

        // Inicializar Gerenciadores
        this.keyManager = new KeyManager(this);
        this.crateManager = new CrateManager(this);
        this.locationManager = new LocationManager(this);

        // Registrar Eventos
        getServer().getPluginManager().registerEvents(new CrateInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new AnimationListener(), this);

        // Registrar Comandos e Tab Completer
        PluginCommand cmd = getCommand("caixa");
        if (cmd != null) {
            CrateCommand crateCmd = new CrateCommand(this);
            cmd.setExecutor(crateCmd);
            cmd.setTabCompleter(crateCmd);
        }

        getLogger().info("========================================");
        getLogger().info("  MysteryBoxes v" + getDescription().getVersion() + " ativado com sucesso!");
        getLogger().info("  Compatibilidade: Minecraft 1.21.x (Java 21)");
        getLogger().info("  Caixas carregadas: " + crateManager.getCrates().size());
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (locationManager != null) {
            locationManager.stopParticleTask();
            locationManager.save();
        }

        getLogger().info("MysteryBoxes desativado com sucesso.");
    }

    public static MysteryBoxesPlugin getInstance() {
        return instance;
    }

    public KeyManager getKeyManager() {
        return keyManager;
    }

    public CrateManager getCrateManager() {
        return crateManager;
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public String getPrefix() {
        return prefix;
    }
}
