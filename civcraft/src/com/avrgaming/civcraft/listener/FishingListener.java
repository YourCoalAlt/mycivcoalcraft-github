package com.avrgaming.civcraft.listener;

import java.text.DecimalFormat;
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
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

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
				} else if (d.loot_type.contains("junk")) {
					dc *= 0.9;
				} else if (d.loot_type.contains("fish")) {
					dc *= 0.8;
				} else {
					CivLog.warning("Fishing Event had unknown loot type, "+d.loot_type);
				}
			}
			
			ResidentExperience re = CivGlobal.getResidentE(p);
			float mod = (float) (((double) (re.getFishingLevel()-1) / 2) / 100);
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
		if (event.getState() == PlayerFishEvent.State.BITE) {
			CivMessage.send(event.getPlayer(), CivColor.LightGray+"Nibble Nibble!");
		}
		if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
			Player p = event.getPlayer();
			ResidentExperience re = CivGlobal.getResidentE(p);
			
			event.getCaught().remove();
			ArrayList<ConfigFishing> dropped = getRandomDrops(p);
			
			int exp = 0;
			double res_exp = 0;
			if (dropped.size() == 0) {
				ItemStack fish = ItemManager.createItemStack(ItemManager.getId(Material.RAW_FISH), 1);
				p.getWorld().dropItem(p.getLocation(), fish);
				
				try {
					res_exp = CivSettings.getDouble(CivSettings.fishingConfig, "default_res_exp");
				} catch (InvalidConfiguration e) {
					res_exp = 0.1;
					e.printStackTrace();
				}
				
				Random rand = new Random();
				exp = rand.nextInt(5);
				CivMessage.send(p, CivColor.YellowItalic+"You've fished up a "+CivColor.LightPurple+"Raw Fish");
			} else {
				for (ConfigFishing d : dropped) {
					res_exp += d.res_exp;
					exp += getEXPDrop(p, d.exp_min, d.exp_max);
					if (d.custom_id != null) {
						LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
						ItemStack cust = LoreMaterial.spawn(LoreMaterial.materialMap.get(craftMat.getConfigId()));
						p.getWorld().dropItem(p.getLocation(), cust);
					} else {
						ItemStack reg = ItemManager.createItemStack(d.type_id, 1, (short)d.type_data);
						p.getWorld().dropItem(p.getLocation(), reg);
						CivMessage.send(p, CivColor.YellowItalic+"You've fished up a "+CivColor.LightPurple+CivData.getDisplayName(d.type_id, d.type_data));
					}
				}
			}
			
			DecimalFormat df = new DecimalFormat("0.00");
			double mod = re.getFishingLevel() + 1;
			mod /= 2;
			
			int eEXP = (int) (exp*mod) / 2;
			event.setExpToDrop(eEXP);
			
			double genrf = res_exp*mod;
			double rfEXP = Double.valueOf(df.format(genrf));
			re.addFishingEXP(rfEXP);
		}
	}
}
