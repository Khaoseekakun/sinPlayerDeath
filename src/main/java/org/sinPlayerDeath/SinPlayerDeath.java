package org.sinPlayerDeath;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.sinPlayerDeath.commands.reload;
import org.sinPlayerDeath.events.playerDeath;

import java.util.Objects;

public final class SinPlayerDeath extends JavaPlugin {

    public static FileConfiguration config;


    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();
        config = getConfig();

        Objects.requireNonNull(getCommand("reload")).setExecutor(new reload());

        getServer().getPluginManager().registerEvents(new playerDeath(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
