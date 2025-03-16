package org.sinPlayerDeath.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import org.sinPlayerDeath.SinPlayerDeath;
import org.sinPlayerDeath.untils.Color;

import java.io.File;

public class reload implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(commandSender instanceof Player player){
            if(player.hasPermission("sinPlayerDeath.reload")){
                reloadConfig();
                String reload_message = SinPlayerDeath.config.getString("messages.reload");
                player.sendMessage(Color.ChangeColor(reload_message));
            }else{
                String you_not_have_permission = SinPlayerDeath.config.getString("messages.no_permission");
                player.sendMessage(Color.ChangeColor(you_not_have_permission));
            }
        }else{
            reloadConfig();
            String reload_message = SinPlayerDeath.config.getString("messages.reload");
            commandSender.sendMessage(Color.ChangeColor(reload_message));
        }
        return true;
    }

    public static void reloadConfig(){
        File configFile = new File("plugins/sinPlayerDeath/", "config.yml");
        SinPlayerDeath.config = YamlConfiguration.loadConfiguration(configFile);
    }
}
