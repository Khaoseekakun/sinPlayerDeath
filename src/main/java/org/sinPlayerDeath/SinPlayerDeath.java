package org.sinPlayerDeath;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinPlayerDeath.commands.reload;
import org.sinPlayerDeath.events.playerDeath;
import net.milkbowl.vault.economy.Economy;
import org.sinPlayerDeath.manager.PlayerDataManager;

import java.util.Objects;

public final class SinPlayerDeath extends JavaPlugin {

    public static FileConfiguration config;
    private static Economy econ = null;
    private static SinPlayerDeath instance;
    private static PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        this.saveDefaultConfig();
        config = getConfig();

        // Setup Vault
        if (!setupEconomy()) {
            getLogger().severe("Vault dependency not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register command
        Objects.requireNonNull(getCommand("playerdeath")).setExecutor(new reload());
        Objects.requireNonNull(getCommand("playerdeath")).setTabCompleter(new reload());
        // Register events
        getServer().getPluginManager().registerEvents(new playerDeath(), this);

        playerDataManager = new PlayerDataManager(this);
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PKPlaceholdersExpansion(this).register();
        }

        playerDataManager.startAutoSave();
        new TabUpdater(this).startTabUpdateTask();
        getLogger().info("SinPlayerDeath enabled successfully.");
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAll();
        getLogger().info("SinPlayerDeath disabled.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public static Economy getEconomy() {
        return econ;
    }
    public static FileConfiguration getPluginConfig() {
        return config;
    }
    public static SinPlayerDeath getInstance() {
        return instance;
    }

    public static PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
