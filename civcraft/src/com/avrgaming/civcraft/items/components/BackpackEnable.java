package com.avrgaming.civcraft.items.components;

import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.backpack.Backpack;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class BackpackEnable extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		attrs.addLore(CivColor.Purple+"<Right Click to Open>");
	}
	
	public void onInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			event.setUseItemInHand(Result.DENY);
			return;
		}
		Backpack.spawnGuiBook(event.getPlayer(), true);
	}
	
	@Override
	public void onItemDrop(PlayerDropItemEvent event) {
		CivMessage.sendError(event.getPlayer(), "Cannot drop your Backpack, it is bounded to your body.");
		event.setCancelled(true);
	}
	
	public void onItemSpawn(ItemSpawnEvent event) {
		event.setCancelled(true);
	}
}
