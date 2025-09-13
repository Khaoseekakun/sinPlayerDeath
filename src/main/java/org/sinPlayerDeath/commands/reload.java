package org.sinPlayerDeath.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sinPlayerDeath.SinPlayerDeath;
import org.sinPlayerDeath.manager.PlayerData;
import org.sinPlayerDeath.untils.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class reload implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.unknown_subcommand")));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "all-reset" -> handleAllReset(sender, args);
            case "pk-reset" -> handlePKReset(sender, args);
            case "setpklevel" -> handleSetPKLevel(sender, args);
            case "setpkkills" -> handleSetPKKills(sender, args);
            default -> sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.unknown_subcommand")));
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (sender.hasPermission("sinPlayerDeath.reload")) {
            reloadConfig();
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.reload")));
        } else {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.no_permission")));
        }
    }

    private void handleAllReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sinPlayerDeath.admin.reset")) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.no_permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.usage_all_reset")));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            PlayerData pd = SinPlayerDeath.getPlayerDataManager().getData(target.getUniqueId());
            pd.setPKLevel(0);
            pd.setPkTotalKills(0);
            SinPlayerDeath.getPlayerDataManager().saveData(target.getUniqueId());
            String msg = Objects.requireNonNull(SinPlayerDeath.config.getString("messages.reset_player"))
                    .replace("{player}", target.getName());
            sender.sendMessage(Color.ChangeColor(msg));
        } else {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.player_not_found")));
        }
    }

    private void handlePKReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sinPlayerDeath.admin.reset")) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.no_permission")));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.usage_pk_reset")));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            PlayerData pd = SinPlayerDeath.getPlayerDataManager().getData(target.getUniqueId());
            pd.resetPKStatus();
            SinPlayerDeath.getPlayerDataManager().saveData(target.getUniqueId());
            String msg = Objects.requireNonNull(SinPlayerDeath.config.getString("messages.reset_pk_status"))
                    .replace("{player}", target.getName());
            sender.sendMessage(Color.ChangeColor(msg));
        } else {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.player_not_found")));
        }
    }

    private void handleSetPKLevel(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sinPlayerDeath.admin.setpk")) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.no_permission")));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.usage_setpklevel")));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.number_required").replace("{type}", "Level")));
                return;
            }
            PlayerData pd = SinPlayerDeath.getPlayerDataManager().getData(target.getUniqueId());
            pd.setPKLevel(level);
            SinPlayerDeath.getPlayerDataManager().saveData(target.getUniqueId());
            sender.sendMessage(Color.ChangeColor(
                    SinPlayerDeath.config.getString("messages.set_pk_level")
                            .replace("{player}", target.getName())
                            .replace("{level}", String.valueOf(level))
            ));
        } else {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.player_not_found")));
        }
    }

    private void handleSetPKKills(CommandSender sender, String[] args) {
        if (!sender.hasPermission("sinPlayerDeath.admin.setpkkills")) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.no_permission")));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.usage_setpkkills")));
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target != null) {
            int kills;
            try {
                kills = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.number_required").replace("{type}", "Kills")));
                return;
            }
            PlayerData pd = SinPlayerDeath.getPlayerDataManager().getData(target.getUniqueId());
            pd.setPkTotalKills(kills);
            SinPlayerDeath.getPlayerDataManager().saveData(target.getUniqueId());
            sender.sendMessage(Color.ChangeColor(
                    SinPlayerDeath.config.getString("messages.set_pk_kills")
                            .replace("{player}", target.getName())
                            .replace("{kills}", String.valueOf(kills))
            ));
        } else {
            sender.sendMessage(Color.ChangeColor(SinPlayerDeath.config.getString("messages.player_not_found")));
        }
    }

    public static void reloadConfig() {
        File configFile = new File("plugins/sinPlayerDeath/", "config.yml");
        SinPlayerDeath.config = YamlConfiguration.loadConfiguration(configFile);
    }

    // ---------------- TabComplete ----------------
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("all-reset");
            completions.add("pk-reset");
            completions.add("setpklevel");
            completions.add("setpkkills");
        } else if (args.length == 2) {
            for (Player p : Bukkit.getOnlinePlayers()) completions.add(p.getName());
        }
        return completions;
    }
}
