package com.avrgaming.civcraft.items.components;

import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.interactive.InteractiveTeleportToPlayer;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class TeleportPlayer extends ItemComponent {
	
	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
		attrUtil.addLore(CivColor.LightGray+" - Teleport to a Player - ");
		attrUtil.addLore(CivColor.Gold+" « Right Click To Use » ");
	}
	
	public void onInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		if (event.getAction().equals(Action.LEFT_CLICK_AIR)) {
			Player p = event.getPlayer();
			Resident resident = CivGlobal.getResident(p);
			if (resident == null) {
				CivMessage.sendError(p, "You must be a registered resident to found a civ. This shouldn't happen. Contact an admin.");
				return;
			}
			
			/* Save the location so we dont have to re-validate the structure position. */
			resident.desiredTownLocation = p.getLocation();
			CivMessage.sendHeading(p, "Teleporting to Player");
			CivMessage.send(p, CivColor.LightGreen+"You found the magical tool to allow you to travel accross the world!");
			CivMessage.send(p, CivColor.LightGreen+"What player do you wish to teleport to? Please type your response.");
			CivMessage.send(p, " ");
			CivMessage.send(p, CivColor.LightGray+"(To cancel, type 'cancel')");
			resident.setInteractiveMode(new InteractiveTeleportToPlayer());
		}
	}
}
