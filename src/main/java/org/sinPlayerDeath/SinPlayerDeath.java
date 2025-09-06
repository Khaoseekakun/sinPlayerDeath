package org.sinPlayerDeath;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinPlayerDeath.commands.reload;
import org.sinPlayerDeath.events.playerDeath;
import net.milkbowl.vault.economy.Economy;

import java.util.Objects;

public final class SinPlayerDeath extends JavaPlugin {

    public static FileConfiguration config;
    private static Economy econ = null;
    private static SinPlayerDeath instance;

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
        Objects.requireNonNull(getCommand("sinpd")).setExecutor(new reload());

        // Register events
        getServer().getPluginManager().registerEvents(new playerDeath(), this);

        getLogger().info("SinPlayerDeath enabled successfully.");
    }

    @Override
    public void onDisable() {
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
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static SinPlayerDeath getInstance() {
        return instance;
    }
}
