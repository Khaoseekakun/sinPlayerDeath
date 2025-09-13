package org.sinPlayerDeath;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabUpdater {

    private final SinPlayerDeath plugin;

    public TabUpdater(SinPlayerDeath plugin) {
        this.plugin = plugin;
    }

    public void startTabUpdateTask() {
        // Update every 5 seconds (100 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerTab(player);
            }
        }, 0L, 100L);
    }

    private void updatePlayerTab(Player player) {
        // Get your placeholder values
        String pkPlaceholder = plugin.getServer()
                .getServicesManager()
                .getRegistration(me.clip.placeholderapi.PlaceholderAPI.class) != null
                ? me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%sinpd_pk_level%")
                : String.valueOf(plugin.getPlayerDataManager().getData(player.getUniqueId()).getPKLevel());

        // Set display in tab list
        String displayName = pkPlaceholder + " " + player.getName();
        if (displayName.length() > 16) {
            displayName = displayName.substring(0, 16); // Tab names are max 16 chars
        }

        player.setPlayerListName(displayName);
    }
}

