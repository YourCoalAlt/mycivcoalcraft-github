package com.avrgaming.civcraft.listener;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigFishing;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.exception.InvalidConfiguration;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.object.ResidentExperience.EXPSlots;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

public class FishingListener implements Listener {
	
	public ArrayList<ConfigFishing> getRandomDrops(Player p) {
		Random rand = new Random();		
		ArrayList<ConfigFishing> dropped = new ArrayList<ConfigFishing>();
		
		for (ConfigFishing d : CivSettings.fishingDrops) {
			double dc = d.drop_chance;
			ItemStack rod = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
			if (rod.getType() == Material.FISHING_ROD && rod.containsEnchantment(Enchantment.LUCK)) {
				if (d.loot_type.contains("treasure")) {
					dc *= 1.1;
				} else if (d.loot_type.contains("legendary")) {
					dc *= 1.02;
				} else if (d.loot_type.contains("junk")) {
					dc *= 0.925;
				} else if (d.loot_type.contains("fish")) {
					dc *= 0.85;
				} else {
					CivLog.warning("Fishing Event had unknown loot type: "+d.loot_type);
				}
			}
			
			ResidentExperience re = CivGlobal.getResidentE(p);
			float mod = (float) (((double) (re.getEXPLevel(EXPSlots.FISHING)-1) / 2) / 100);
			if (mod > 0) {
				if (d.loot_type.contains("treasure")) {
					dc += (mod/2);
				} else if (d.loot_type.contains("legendary")) {
					dc += (mod/10);
				} else if (d.loot_type.contains("junk") || d.loot_type.contains("fish")) {
					dc -= mod;
				} else {
					CivLog.warning("Fishing Event had unknown loot type, "+d.loot_type);
				}
			}
			
			if (dc < 0) {
				dc = d.drop_chance;
			}
			
			int chance = rand.nextInt(10000);
			if (chance < (dc*10000)) {
				dropped.add(d);
			}
		}
		return dropped;
	}
	
	public Integer getEXPDrop(Player p, int min, int max) {
		Random rand = new Random();
		int exp = rand.nextInt(max)+1;
		if (exp < min) exp = min;
		if (exp > max) exp = max;
		
		ItemStack rod = p.getInventory().getItem(p.getInventory().getHeldItemSlot());
		if (rod.getType() == Material.FISHING_ROD && rod.containsEnchantment(Enchantment.LUCK)) {
			exp *= 2;
		}
		return exp;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerFish(PlayerFishEvent event) throws CivException {
		if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
			Player p = event.getPlayer();
			ResidentExperience re = CivGlobal.getResidentE(p);
			
			event.getCaught().remove();
			ArrayList<ConfigFishing> dropped = getRandomDrops(p);
			
			int exp = 0;
			double res_exp = 0;
			if (dropped.size() == 0) {
				ItemStack fish = CivItem.newStack(Material.RAW_FISH);
				CivItem.givePlayerItem(p, fish, p.getLocation(), null, fish.getAmount(), false);
				CivMessage.send(p, CivColor.YellowItalic+"You've fished up a "+CivColor.LightPurple+"Raw Fish");
				
				try {
					res_exp = CivSettings.getDouble(CivSettings.fishingConfig, "default_res_exp");
				} catch (InvalidConfiguration e) {
					res_exp = 0.1;
					e.printStackTrace();
				}
				
				Random rand = new Random();
				exp = rand.nextInt(5);
			} else {
				for (ConfigFishing d : dropped) {
					res_exp += d.res_exp;
					exp += getEXPDrop(p, d.exp_min, d.exp_max);
					if (d.custom_id != null) {
						LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
						ItemStack cust = LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()));
						CivItem.givePlayerItem(p, cust, p.getLocation(), null, cust.getAmount(), false);
						CivMessage.send(p, CivColor.YellowItalic+"You've fished up a "+CivColor.LightPurple+craftMat.getName());
					} else {
						ItemStack stack = CivItem.newStack(d.type_id, d.type_data, true);
						CivItem.givePlayerItem(p, stack, p.getLocation(), null, stack.getAmount(), false);
						CivMessage.send(p, CivColor.YellowItalic+"You've fished up a "+CivColor.LightPurple+CivData.getStackName(stack));
					}
				}
			}
			
			double mod = (re.getEXPLevel(EXPSlots.FISHING) + 1) / 2;
			int eEXP = (int) (exp*mod) / 2;
			event.setExpToDrop(eEXP);
			re.addResEXP(EXPSlots.FISHING, res_exp);
		}
	}
}
