package org.sinPlayerDeath.manager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sinPlayerDeath.SinPlayerDeath;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final SinPlayerDeath plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File usersFile;
    private final FileConfiguration usersConfig;

    public PlayerDataManager(SinPlayerDeath plugin) {
        this.plugin = plugin;
        this.usersFile = new File(plugin.getDataFolder(), "users.yml");
        if (!usersFile.exists()) plugin.saveResource("users.yml", false);
        this.usersConfig = YamlConfiguration.loadConfiguration(usersFile);
    }

    // Get or create PlayerData
    public PlayerData getData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid)) {
            PlayerData data = loadPlayerData(uuid); // Load from file if exists
            playerDataMap.put(uuid, data);
        }
        return playerDataMap.get(uuid);
    }

    // Save single player
    public void saveData(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        usersConfig.set("players." + uuid + ".pk_status", data.getPKStatus());
        usersConfig.set("players." + uuid + ".pk_level", data.getPKLevel());
        usersConfig.set("players." + uuid + ".pk_total_kills", data.getPkTotalKills());

        try {
            usersConfig.save(usersFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load from file
    private PlayerData loadPlayerData(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        if (usersConfig.contains("players." + uuid)) {
            data.setPKStatus(usersConfig.getBoolean("players." + uuid + ".pk_status", false));
            data.setPKLevel(usersConfig.getInt("players." + uuid + ".pk_level", 0));
            data.setPkTotalKills(usersConfig.getInt("players." + uuid + ".pk_total_kills", 0));
        }
        return data;
    }

    // Auto save all players every 1 minute
    public void startAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (UUID uuid : playerDataMap.keySet()) {
                saveData(uuid);
            }
        }, 20L * 60, 20L * 60); // first delay 1 min, repeat 1 min
    }

    // Save all players manually
    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            saveData(uuid);
        }
    }
}
