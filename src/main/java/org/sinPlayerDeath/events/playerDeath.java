package org.sinPlayerDeath.events;

import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.sinPlayerDeath.SinPlayerDeath;
import org.sinPlayerDeath.manager.PlayerData;
import org.sinPlayerDeath.untils.Color;
import org.sinPlayerDeath.untils.RegionUtils;

import java.util.*;

public class playerDeath implements Listener {

    private final Map<UUID, Inventory> savedInventories = new HashMap<>();
    private final Map<UUID, Integer> savedEXP = new HashMap<>();
    private final Map<UUID, ItemStack> saveHELMET = new HashMap<>();
    private final Map<UUID, ItemStack> saveCHESTPLATE = new HashMap<>();
    private final Map<UUID, ItemStack> saveLEGGINGS = new HashMap<>();
    private final Map<UUID, ItemStack> saveBOOTS = new HashMap<>();
    private final Map<UUID, ItemStack> saveLeftHand = new HashMap<>();

    private final Economy econ;

    public playerDeath() {
        this.econ = SinPlayerDeath.getEconomy(); // get Vault econ from your main class
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerData victimData = SinPlayerDeath.getPlayerDataManager().getData(victim.getUniqueId());

        if (victimData.getPKStatus()) {
            // ยกเลิกการ cancel ของ event ทั้งหมด
            event.setCancelled(false);
        }
    }

    private void handleInventoryDrops(PlayerDeathEvent event, Player player) {
        UUID uuid = player.getUniqueId();
        List<ItemStack> drops = event.getDrops();
        List<String> noDropItems = SinPlayerDeath.config.getStringList("item_no_drop");
        Inventory oldInventory = Bukkit.createInventory(null, 36);

        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack dropItem = iterator.next();
            if (dropItem == null) continue;

            boolean shouldNotDrop = noDropItems.contains(dropItem.getType().toString()) || hasNoDropTag(dropItem);

            if (shouldNotDrop) {
                // ตรวจสอบ armor & offhand
                switch (dropItem.getType().toString()) {
                    case "DIAMOND_HELMET", "IRON_HELMET", "GOLD_HELMET", "CHAINMAIL_HELMET", "LEATHER_HELMET" ->
                            saveHELMET.put(uuid, dropItem);
                    case "DIAMOND_CHESTPLATE", "IRON_CHESTPLATE", "GOLD_CHESTPLATE", "CHAINMAIL_CHESTPLATE", "LEATHER_CHESTPLATE" ->
                            saveCHESTPLATE.put(uuid, dropItem);
                    case "DIAMOND_LEGGINGS", "IRON_LEGGINGS", "GOLD_LEGGINGS", "CHAINMAIL_LEGGINGS", "LEATHER_LEGGINGS" ->
                            saveLEGGINGS.put(uuid, dropItem);
                    case "DIAMOND_BOOTS", "IRON_BOOTS", "GOLD_BOOTS", "CHAINMAIL_BOOTS", "LEATHER_BOOTS" ->
                            saveBOOTS.put(uuid, dropItem);
                    default -> {
                        if (player.getInventory().getItemInOffHand() != null && player.getInventory().getItemInOffHand().isSimilar(dropItem)) {
                            saveLeftHand.put(uuid, dropItem);
                        } else {
                            oldInventory.addItem(dropItem);
                        }
                    }
                }
                iterator.remove(); // ป้องกันการดรอป
            }
        }

        savedInventories.put(uuid, oldInventory);
    }


    // ✅ Helper: check if item has "no_drop" NBT tag
    private boolean hasNoDropTag(ItemStack item) {
        if (item == null) return false;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey("no_drop") && (nbt.getBoolean("no_drop") || nbt.getInteger("no_drop") == 1);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (savedInventories.containsKey(playerId)) {
            Inventory invPlayer = savedInventories.get(playerId);
            player.getInventory().setContents(invPlayer.getContents());
            savedInventories.remove(playerId);
        }

        if (saveHELMET.containsKey(playerId)) {
            player.getInventory().setHelmet(saveHELMET.get(playerId));
            saveHELMET.remove(playerId);
        }
        if (saveCHESTPLATE.containsKey(playerId)) {
            player.getInventory().setChestplate(saveCHESTPLATE.get(playerId));
            saveCHESTPLATE.remove(playerId);
        }
        if (saveLEGGINGS.containsKey(playerId)) {
            player.getInventory().setLeggings(saveLEGGINGS.get(playerId));
            saveLEGGINGS.remove(playerId);
        }
        if (saveBOOTS.containsKey(playerId)) {
            player.getInventory().setBoots(saveBOOTS.get(playerId));
            saveBOOTS.remove(playerId);
        }
        if (saveLeftHand.containsKey(playerId)) {
            player.getInventory().setItemInOffHand(saveLeftHand.get(playerId));
            saveLeftHand.remove(playerId);
        }

        // ✅ XP handling
        double expLostPercentage = SinPlayerDeath.config.getDouble("exp_lost", 0.2);
        if (savedEXP.containsKey(playerId)) {
            int totalXP = savedEXP.get(playerId);
            int lostXP = (int) (totalXP * expLostPercentage);
            int remainingXP = totalXP - lostXP;
            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0);
            player.giveExp(remainingXP);
            savedEXP.remove(playerId);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = SinPlayerDeath.getPlayerDataManager().getData(uuid);

        if (data != null && data.getPKStatus()) {
            data.updatePKPermissions(); // sync permission ตอน login
        }
    }
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PermissionAttachment attachment = PlayerData.getAttachments().remove(uuid);
        if (attachment != null) {
            attachment.remove(); // ลบ attachment ออกจาก player
        }
    }
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Player killer = player.getKiller();
        UUID playerId = player.getUniqueId();
        double balance = econ.getBalance(player);
        PlayerData deathData = SinPlayerDeath.getPlayerDataManager().getData(playerId);

        // ตรวจสอบ region PvP
        boolean isInAllowedPvp = new RegionUtils().isInAllowedPvpRegion(player);

        boolean pkStatus = false; // ผู้ตายเป็น PK หรือไม่ สำหรับ logic bounty

        if (killer != null) {
            PlayerData killerData = SinPlayerDeath.getPlayerDataManager().getData(killer.getUniqueId());

            if (isInAllowedPvp) {
                event.setKeepInventory(true);
                return;
            } else {
                // PK logic ปกติ
                if (deathData.getPKStatus()) {
                    // ผู้ตายเป็น PK → ผู้ฆ่าไม่ติด PK, ได้ bounty
                    pkStatus = true;
                    killer.sendMessage(Color.ChangeColor(
                            SinPlayerDeath.config.getString("messages.kill_pk_player",
                                            "§aYou killed §c{player} §aand you did not get PK point because he is PK player")
                                    .replace("{player}", player.getName()))
                    );

                    // จ่าย bounty
                    double bounty = deathData.getPkTotalKills() * SinPlayerDeath.config.getDouble("bounty_per_kill", 100.0);

                        if(balance < bounty){
                            if(balance <= 0) {
                                bounty = 0;
                            } else {
                                bounty = balance;
                            }
                        }

                        econ.depositPlayer(killer, bounty);

                        String cmiCommand = "money take " + player.getName() + " " + bounty + " -s";

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmiCommand);

                        killer.sendMessage(Color.ChangeColor(
                                SinPlayerDeath.config.getString("messages.bounty_paid",
                                                "§aYou received a bounty of {bounty} for killing {player}")
                                        .replace("{player}", player.getName())
                                        .replace("{bounty}", String.format("%.2f", bounty))
                        ));

                    // reset PK ของผู้ตาย
                    deathData.setPKStatus(false);
                } else {
                    // ผู้ตายไม่ใช่ PK → normal PK logic
                    killerData.setPKStatus(true);
                    killerData.addPkTotalKills(1);
                }

                SinPlayerDeath.getPlayerDataManager().saveData(killer.getUniqueId());
            }
        }

        // บันทึก XP
        savedEXP.put(playerId, player.getTotalExperience());

        // เงิน
            double percent = getMoneyLossPercentage(player);
            double lostMoney = balance * percent;

            // ถ้าเป็น PK ผู้ตาย → bounty คำนวณแล้วข้างบน
            if (!isInAllowedPvp && pkStatus) {
                // ไม่หักซ้ำ
                lostMoney = balance * percent;
            }

            String cmiCommand = "money take " + player.getName() + " " + lostMoney + " -s";
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmiCommand);

            String msg = SinPlayerDeath.config.getString("messages.money_lost",
                    "§cTake money for you {money_take} because you die").replace("{money_take}", String.format("%.2f", lostMoney));
            player.sendMessage(Color.ChangeColor(msg));

        // ไอเท็ม
        if (Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))) return;

        if (SinPlayerDeath.config.getBoolean("has_permission_no_drop") && player.hasPermission("sinplayerdeath.no_drop_all")) {
            event.getDrops().clear();
            event.setKeepInventory(true);
            event.setDroppedExp(0);
        } else {
            handleInventoryDrops(event, player);
        }
    }

    private double getMoneyLossPercentage(Player player) {
        PlayerData deathData = SinPlayerDeath.getPlayerDataManager().getData(player.getUniqueId());
        Map<String, Object> moneyLossSection = SinPlayerDeath.config.getConfigurationSection("money_lost").getValues(false);
        double pkLostDefault = 0;
        if (deathData.getPKStatus()) {
            if(deathData.getPKLevel() == 1){
                pkLostDefault = SinPlayerDeath.config.getDouble("pk_lost.pk_level_1", 0.1);
            }
            if(deathData.getPKLevel() == 2){
                pkLostDefault = SinPlayerDeath.config.getDouble("pk_lost.pk_level_2", 0.2);
            }
            if(deathData.getPKLevel() == 3){
                pkLostDefault = SinPlayerDeath.config.getDouble("pk_lost.pk_level_3", 0.3);
            }
        }

        for (String key : moneyLossSection.keySet()) {
            if (!key.equalsIgnoreCase("default") && player.hasPermission("group." + key)) {
                return SinPlayerDeath.config.getDouble("money_lost." + key, SinPlayerDeath.config.getDouble("money_lost.default", 0.1))+pkLostDefault;
            }
        }

        return SinPlayerDeath.config.getDouble("money_lost.default", 0.1)+pkLostDefault;

    }
}
