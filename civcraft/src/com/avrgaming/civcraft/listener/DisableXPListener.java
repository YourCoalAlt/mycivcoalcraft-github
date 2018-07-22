package com.avrgaming.civcraft.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

public class DisableXPListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void onExpBottleEvent(ExpBottleEvent event) {
		event.setExperience(0);
	}
	
/*	@EventHandler(priority = EventPriority.LOW)
	public void onItemSpawnEvent(ItemSpawnEvent event) {
//		if (event.getEntity().getType().equals(EntityType.EXPERIENCE_ORB)) {
//			event.setCancelled(true);
//		}
	}*/
	
/*	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerPrepareEnchant(PrepareItemEnchantEvent event) {
		for (EnchantmentOffer eo : event.getOffers()) {
			if (eo == null) continue;
			eo.setCost(eo.getCost()*10);
		}
	}*/
	
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		if (event.getClickedBlock() == null || CivItem.getId(event.getClickedBlock()) == CivData.AIR) return;
		
		Block block = event.getClickedBlock();
		if (block.getType().equals(Material.ENCHANTMENT_TABLE)) {
			if (!event.getPlayer().isOp()) {
				CivMessage.sendError(event.getPlayer(), "Cannot use enchantment tables. XP and Levels disabled in CivCraft.");
				event.setCancelled(true);
			}
		}
		
//		if (block.getType().equals(Material.ANVIL)) {
//			CivMessage.sendError(event.getPlayer(), "Cannot use anvils. XP and Levels disabled in CivCraft.");
//			event.setCancelled(true);
//		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerExpChange(PlayerExpChangeEvent event) {
		Resident res = CivGlobal.getResident(event.getPlayer());
		
		CivMessage.send(res, CivColor.LightGreen+"Picked up "+CivColor.Yellow+event.getAmount()+CivColor.LightGreen+" coins.");
		res.getTreasury().deposit(event.getAmount());
		convertExpCheck(event.getPlayer());
		event.setAmount(0);
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerExpLevelChange(PlayerLevelChangeEvent event) {
		convertExpCheck(event.getPlayer());
	}
	
	private void convertExpCheck(Player p) {
		int amt = (int) p.getTotalExperience();
		if (amt > 0) {
			p.setLevel(0);
			p.setExp(0);
			p.setTotalExperience(0);
			CivMessage.send(p, CivColor.Yellow+"Converted "+CivColor.Rose+amt+CivColor.Yellow+" XP to coins.");
			Resident resident = CivGlobal.getResident(p);
			resident.getTreasury().deposit(amt);
		}
	}
	
}
