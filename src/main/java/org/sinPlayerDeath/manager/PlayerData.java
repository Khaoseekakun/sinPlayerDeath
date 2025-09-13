package org.sinPlayerDeath.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.permissions.PermissionAttachment;
import org.sinPlayerDeath.SinPlayerDeath;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private boolean hasPKstatus;
    private int pkLevel;
    private int pkTotalKills;

    // Map สำหรับเก็บ PermissionAttachment ของผู้เล่นแต่ละคน
    private static final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.hasPKstatus = false;
        this.pkLevel = 0;
        this.pkTotalKills = 0;
    }

    public static Map<UUID, PermissionAttachment> getAttachments() {
        return attachments;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void resetPKStatus() {
        this.hasPKstatus = false;
        this.pkLevel = 0;
        removePKPermissions();
        autoSave();
    }

    public int getPkTotalKills() {
        return pkTotalKills;
    }

    public void setPkTotalKills(int pkTotalKills) {
        this.pkTotalKills = pkTotalKills;
        autoSave();
    }

    public void addPkTotalKills(int increment) {
        this.pkTotalKills += increment;
        if (this.pkTotalKills < 0) this.pkTotalKills = 0;

        int newLevel = 0;
        for (String key : SinPlayerDeath.getPluginConfig().getConfigurationSection("pk_upLevel").getKeys(false)) {
            int requiredKills = SinPlayerDeath.getPluginConfig().getInt("pk_upLevel." + key);
            if (this.pkTotalKills >= requiredKills) {
                newLevel = Math.max(newLevel, Integer.parseInt(key.replace("pklevel", "")));
            }
        }

        this.pkLevel = newLevel;
        this.hasPKstatus = newLevel > 0;

        updatePKPermissions(); // update permission ทุกครั้ง
        autoSave();
    }

    public boolean getPKStatus() {
        return hasPKstatus;
    }

    public void setPKStatus(boolean status) {
        this.hasPKstatus = status;
        if (status) {
            updatePKPermissions();
        } else {
            removePKPermissions();
        }
        autoSave();
    }

    public int getPKLevel() {
        return pkLevel;
    }

    public void setPKLevel(int level) {
        this.pkLevel = level;
        this.hasPKstatus = level > 0;
        updatePKPermissions();
        autoSave();
    }

    // ---------------------------
    // Permission Management
    // ---------------------------
    public void updatePKPermissions() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        PermissionAttachment attachment = attachments.get(uuid);
        if (attachment == null) {
            attachment = player.addAttachment(JavaPlugin.getPlugin(SinPlayerDeath.class));
            attachments.put(uuid, attachment);
        }

        // ลบ permission เก่าทั้งหมดก่อน
        attachment.unsetPermission("sinpk.level1");
        attachment.unsetPermission("sinpk.level2");
        attachment.unsetPermission("sinpk.level3");

        // เพิ่ม permission ตาม level
        if (hasPKstatus) {
            if (pkLevel >= 1) attachment.setPermission("sinpk.level1", true);
            if (pkLevel >= 2) attachment.setPermission("sinpk.level2", true);
            if (pkLevel >= 3) attachment.setPermission("sinpk.level3", true);
        }
    }

    public void removePKPermissions() {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;

        PermissionAttachment attachment = attachments.remove(uuid);
        if (attachment != null) {
            attachment.remove(); // ลบ attachment ทั้งหมด
        }
    }

    // ---------------------------
    // Auto-save helper
    // ---------------------------
    private void autoSave() {
        if (SinPlayerDeath.getPlayerDataManager() != null) {
            SinPlayerDeath.getPlayerDataManager().saveData(uuid);
        }
    }
}
