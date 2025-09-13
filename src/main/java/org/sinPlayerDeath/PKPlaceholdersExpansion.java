package org.sinPlayerDeath;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sinPlayerDeath.manager.PlayerData;
import org.sinPlayerDeath.untils.Color;

public class PKPlaceholdersExpansion extends PlaceholderExpansion {

    private final SinPlayerDeath plugin;

    public PKPlaceholdersExpansion(SinPlayerDeath plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "sinpd"; // ใช้ %sinpd_<name>% ใน PlaceholderAPI
    }

    @Override
    public @NotNull String getAuthor() {
        return "YourName";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // ไม่ต้อง reload ทุกครั้ง
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        PlayerData data = plugin.getPlayerDataManager().getData(player.getUniqueId());

        switch (identifier.toLowerCase()) {
            case "pk_status":
                return data.getPKStatus() ? "Active" : "Inactive";

            case "pk_status_text":
                return data.getPKStatus()
                        ? Color.ChangeColor(plugin.getPluginConfig().getString("placeholders.pk_status_active", "Active"))
                        : Color.ChangeColor(plugin.getPluginConfig().getString("placeholders.pk_status_inactive", "Inactive"));

            case "pk_totalkills":
                int totalKills = data.getPkTotalKills();
                java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###");
                return formatter.format(totalKills);

            case "pk_level":
                int level = data.getPKLevel();
                String key = "pklevel" + level;
                String value = plugin.getPluginConfig().getString("placeholders." + key);
                if (value == null) return "";
                return Color.ChangeColor(value);

            case "bounty":
                int bounty = plugin.getPluginConfig().getInt("bounty_per_kill") * data.getPkTotalKills();
                return formatNumber(bounty);

            default:
                return null;
        }
    }

    /**
     * ฟังก์ชันช่วย format number ให้เป็นแบบ 1k, 10k, 1m
     */
    private String formatNumber(double number) {
        if (number >= 1_000_000) {
            return String.format("%.1fm", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fk", number / 1_000.0);
        } else {
            if (number % 1 == 0) {
                return String.format("%.0f", number);
            } else {
                return String.format("%.2f", number);
            }
        }
    }
}
