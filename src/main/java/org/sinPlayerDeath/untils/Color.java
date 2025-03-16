package org.sinPlayerDeath.untils;

import org.bukkit.ChatColor;

@SuppressWarnings("deprecation")
public class Color {
    public static String ChangeColor(String messages){
        return ChatColor.translateAlternateColorCodes('&', messages);
    }
}
