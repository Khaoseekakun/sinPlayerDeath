package org.sinPlayerDeath.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sinPlayerDeath.SinPlayerDeath;

import java.util.*;

public class playerDeath implements Listener {

    private final Map<UUID, Inventory> savedInventories = new HashMap<>();
    private final Map<UUID, Integer> savedEXP = new HashMap<>();
    private final Map<UUID, ItemStack> saveHELMET = new HashMap<>();
    private final Map<UUID, ItemStack> saveCHESTPLATE = new HashMap<>();
    private final Map<UUID, ItemStack> saveLEGGINGS = new HashMap<>();
    private final Map<UUID, ItemStack> saveBOOTS = new HashMap<>();
    private final Map<UUID, ItemStack> saveLeftHand = new HashMap<>();

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (savedInventories.containsKey(playerId)) {
            Inventory invPlayer = savedInventories.get(playerId);
            player.getInventory().setContents(invPlayer.getContents());
            savedInventories.remove(playerId);
        }

        if(saveHELMET.containsKey(playerId)){
            player.getInventory().setHelmet(saveHELMET.get(playerId));
            saveHELMET.remove(playerId);
        }
        if(saveCHESTPLATE.containsKey(playerId)){
            player.getInventory().setChestplate(saveCHESTPLATE.get(playerId));
            saveCHESTPLATE.remove(playerId);
        }
        if(saveLEGGINGS.containsKey(playerId)){
            player.getInventory().setLeggings(saveLEGGINGS.get(playerId));
            saveLEGGINGS.remove(playerId);
        }
        if(saveBOOTS.containsKey(playerId)){
            player.getInventory().setBoots(saveBOOTS.get(playerId));
            saveBOOTS.remove(playerId);
        }
        if(saveLeftHand.containsKey(playerId)){
            player.getInventory().setItemInOffHand(saveLeftHand.get(playerId));
            saveLeftHand.remove(playerId);
        }

        double expLostPercentage = SinPlayerDeath.config.getDouble("exp_lost", 0.2);

        if(savedEXP.containsKey(playerId)){
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

        List<ItemStack> item_drops = event.getDrops();

        if (SinPlayerDeath.config.getBoolean("has_permission_no_drop") && player.hasPermission("sinPlayerDeath.no_drop_all")) {

            event.setKeepInventory(true);
        } else {
            List<String> noDropItems = SinPlayerDeath.config.getStringList("item_no_drop");
            Inventory old_Inventory = Bukkit.createInventory(null,36);
            Iterator<ItemStack> iterator = item_drops.iterator();

            while (iterator.hasNext()) {
                ItemStack dropItem = iterator.next();
                if (noDropItems.contains(dropItem.getType().toString())) {

                    if (dropItem.getType().toString().contains("HELMET")) {
                        ItemStack currentHelmet = player.getInventory().getHelmet();
                        if (currentHelmet != null && currentHelmet.isSimilar(dropItem)) {
                            saveHELMET.put(player.getUniqueId(), dropItem);
                            iterator.remove();
                        }else{
                            old_Inventory.addItem(dropItem);
                            iterator.remove();
                        }
                    }
                    else if (dropItem.getType().toString().contains("CHESTPLATE")) {
                        ItemStack currentChestplate = player.getInventory().getChestplate();

                        if (currentChestplate != null && currentChestplate.isSimilar(dropItem)) {
                            saveCHESTPLATE.put(player.getUniqueId(), dropItem);
                            iterator.remove();
                        }else{
                            old_Inventory.addItem(dropItem);
                            iterator.remove();
                        }
                    } else if (dropItem.getType().toString().contains("LEGGINGS")) {
                        ItemStack currentLeggings = player.getInventory().getLeggings();
                        if (currentLeggings != null && currentLeggings.isSimilar(dropItem)) {
                            saveLEGGINGS.put(player.getUniqueId(), dropItem);
                            iterator.remove();
                        }else{
                            old_Inventory.addItem(dropItem);
                            iterator.remove();
                        }
                    } else if (dropItem.getType().toString().contains("BOOTS")) {
                        ItemStack currentBoots = player.getInventory().getBoots();
                        if (currentBoots != null && currentBoots.isSimilar(dropItem)) {
                            saveBOOTS.put(player.getUniqueId(), dropItem);
                            iterator.remove();
                        }else{
                            old_Inventory.addItem(dropItem);
                            iterator.remove();
                        }
                    }else{
                        ItemStack itemInOfHand = player.getInventory().getItemInOffHand();
                        if (itemInOfHand.isSimilar(dropItem)) {
                            saveLeftHand.put(player.getUniqueId(), dropItem);
                            iterator.remove();
                        }else{
                            old_Inventory.addItem(dropItem);
                            iterator.remove();
                        }
                    }
                }
            }
            savedInventories.put(player.getUniqueId(), old_Inventory);
        }
    }
}