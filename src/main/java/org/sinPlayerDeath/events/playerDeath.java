package org.sinPlayerDeath.events;

import de.tr7zw.nbtapi.NBTItem;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sinPlayerDeath.SinPlayerDeath;
import org.sinPlayerDeath.untils.Color;

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

    // ✅ Helper: check if item has "no_drop" NBT tag
    private boolean hasNoDropTag(ItemStack item) {
        if (item == null) return false;
        NBTItem nbt = new NBTItem(item);
        return nbt.hasKey("no_drop") && nbt.getBoolean("no_drop");
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        savedEXP.put(uuid, player.getTotalExperience());

        if (econ != null) {
            double balance = econ.getBalance(player);
            double percent = getMoneyLossPercentage(player);
            double lostMoney = balance * percent;
            econ.withdrawPlayer(player, lostMoney);
            String msgTemplate = SinPlayerDeath.config.getString("messages.money_lost",
                    "§cTake money for you {money_take} because you die");
            String msg = msgTemplate.replace("{money_take}", String.format("%.2f", lostMoney));
            player.sendMessage(Color.ChangeColor(msg));
        }

        List<ItemStack> item_drops = event.getDrops();

        if (SinPlayerDeath.config.getBoolean("has_permission_no_drop") && player.hasPermission("sinplayerdeath.no_drop_all")) {
            event.setKeepInventory(true);
            return;
        }

        if(Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY))){
            return;
        }

        List<String> noDropItems = SinPlayerDeath.config.getStringList("item_no_drop");
        Inventory old_Inventory = Bukkit.createInventory(null, 36);
        Iterator<ItemStack> iterator = item_drops.iterator();

        while (iterator.hasNext()) {
            ItemStack dropItem = iterator.next();
            if (dropItem == null) continue;

            boolean shouldNotDrop = noDropItems.contains(dropItem.getType().toString()) || hasNoDropTag(dropItem);

            if (!shouldNotDrop) {
                if (dropItem.getType().toString().contains("HELMET")) {
                    ItemStack currentHelmet = player.getInventory().getHelmet();
                    if (currentHelmet != null && currentHelmet.isSimilar(dropItem)) {
                        saveHELMET.put(uuid, dropItem);
                    } else {
                        old_Inventory.addItem(dropItem);
                    }
                } else if (dropItem.getType().toString().contains("CHESTPLATE")) {
                    ItemStack currentChestplate = player.getInventory().getChestplate();
                    if (currentChestplate != null && currentChestplate.isSimilar(dropItem)) {
                        saveCHESTPLATE.put(uuid, dropItem);
                    } else {
                        old_Inventory.addItem(dropItem);
                    }
                } else if (dropItem.getType().toString().contains("LEGGINGS")) {
                    ItemStack currentLeggings = player.getInventory().getLeggings();
                    if (currentLeggings != null && currentLeggings.isSimilar(dropItem)) {
                        saveLEGGINGS.put(uuid, dropItem);
                    } else {
                        old_Inventory.addItem(dropItem);
                    }
                } else if (dropItem.getType().toString().contains("BOOTS")) {
                    ItemStack currentBoots = player.getInventory().getBoots();
                    if (currentBoots != null && currentBoots.isSimilar(dropItem)) {
                        saveBOOTS.put(uuid, dropItem);
                    } else {
                        old_Inventory.addItem(dropItem);
                    }
                } else {
                    ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
                    if (itemInOffHand != null && itemInOffHand.isSimilar(dropItem)) {
                        saveLeftHand.put(uuid, dropItem);
                    } else {
                        old_Inventory.addItem(dropItem);
                    }
                }

                iterator.remove(); // ✅ Prevent from dropping
            }
        }

        savedInventories.put(uuid, old_Inventory);
    }

    private double getMoneyLossPercentage(Player player) {
        Map<String, Object> moneyLossSection = SinPlayerDeath.config.getConfigurationSection("money_lost").getValues(false);

        for (String key : moneyLossSection.keySet()) {
            if (!key.equalsIgnoreCase("default") && player.hasPermission("group." + key)) {
                return SinPlayerDeath.config.getDouble("money_lost." + key, SinPlayerDeath.config.getDouble("money_lost.default", 0.1));
            }
        }
        return SinPlayerDeath.config.getDouble("money_lost.default", 0.1);
    }
}
