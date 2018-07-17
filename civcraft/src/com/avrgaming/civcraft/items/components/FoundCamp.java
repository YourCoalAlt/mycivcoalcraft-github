/*************************************************************************
 * 
 * AVRGAMING LLC
 * __________________
 * 
 *  [2013] AVRGAMING LLC
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of AVRGAMING LLC and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to AVRGAMING LLC
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from AVRGAMING LLC.
 */
package com.avrgaming.civcraft.items.components;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.interactive.InteractiveCampName;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.util.CallbackInterface;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class FoundCamp extends ItemComponent implements CallbackInterface {

	@Override
	public void onPrepareCreate(AttributeUtil attrUtil) {
		attrUtil.addLore(ChatColor.RESET+CivColor.Gold+"Starts a Camp");
		attrUtil.addLore(ChatColor.RESET+CivColor.Rose+"<Right Click To Use>");		
	}
	
	public void foundCamp(Player player) throws CivException {
		Resident resident = CivGlobal.getResident(player);
		
		if (resident.hasTown()) {
			throw new CivException("You cannot found a camp when you're a member of a town.");
		}
		
		if (resident.hasCamp()) {
			throw new CivException("You cannot found a camp when you're a member of another camp.");
		}
			
		/*
		 * Build a preview for the Capitol structure.
		 */
		CivMessage.send(player, CivColor.LightGreenBold+"Checking structure position... Please wait.");
		ConfigBuildableInfo info = new ConfigBuildableInfo();
		info.id = "camp";
		info.displayName = "Camp";
		info.ignore_floating = false;
		info.template_base_name = "camp";
		info.tile_improvement = false;
		info.templateYShift = -1;
		
		Buildable.buildVerifyStatic(player, info, player.getLocation(), this);
	}
	
	public void onInteract(PlayerInteractEvent event) {
		event.setCancelled(true);
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != event.getItem().getType()) {
			CivMessage.sendError(event.getPlayer(), "Camps are only useable in your main hand.");
			return;
		}
		if (!event.getAction().equals(Action.RIGHT_CLICK_AIR) && !event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			CivMessage.sendError(event.getPlayer(), "Right click to found a camp.");
			return;
		}
		
		try {
			foundCamp(event.getPlayer());
		} catch (CivException e) {
			CivMessage.sendError(event.getPlayer(), e.getMessage());
		}
		return;
	}

	@Override
	public void execute(String playerName) {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		
		Resident resident = CivGlobal.getResident(playerName);
		CivMessage.sendHeading(player, "Setting up Camp!");
		CivMessage.send(player, CivColor.LightGreen+"You and your small band of travelers need a place to sleep for the night.");
		CivMessage.send(player, " ");
		CivMessage.send(player, CivColor.LightGreen+ChatColor.BOLD+"What shall your new camp be called?");
		CivMessage.send(player, CivColor.Gray+"(To cancel, type 'cancel')");
		resident.setInteractiveMode(new InteractiveCampName());
	}
}
