package org.sinPlayerDeath.untils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.sinPlayerDeath.SinPlayerDeath;

import java.util.List;

public class RegionUtils {

    FileConfiguration config = SinPlayerDeath.getPluginConfig();
    List<String> pvpRegions = config.getStringList("region_allow_pvp");

    public boolean isInAllowedPvpRegion(Player player) {
        // WorldEdit player adapter
        com.sk89q.worldedit.entity.Player wePlayer = BukkitAdapter.adapt(player);
        com.sk89q.worldedit.util.Location loc = BukkitAdapter.adapt(player.getLocation());

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(wePlayer.getWorld());
        if (regions == null) return false;

        // ใช้ BlockVector3.at แทน constructor
        ApplicableRegionSet set = regions.getApplicableRegions(
                BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())
        );

        for (ProtectedRegion region : set) {
            if (pvpRegions.contains(region.getId())) {
                return true;
            }
        }
        return false;
    }

}
