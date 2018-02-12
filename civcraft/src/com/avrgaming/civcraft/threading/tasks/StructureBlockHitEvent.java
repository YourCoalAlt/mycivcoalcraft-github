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
package com.avrgaming.civcraft.threading.tasks;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.BuildableDamageBlock;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.war.WarStats;

import gpl.AttributeUtil;

public class StructureBlockHitEvent implements Runnable {

	/* Called when a structure block is hit, this async task quickly determines
	 * if the block hit should take damage during war. */
	String playerName;
	BlockCoord coord;
	BuildableDamageBlock dmgBlock;
	World world;
	
	public StructureBlockHitEvent(String player, BlockCoord coord, BuildableDamageBlock dmgBlock, World world) {
		this.playerName = player;
		this.coord = coord;
		this.dmgBlock = dmgBlock;
		this.world = world;
	}
	
	@Override
	public void run() {
		if (playerName == null) {
			return;
		}
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) { //Player offline now?
			return;
		}
		if (dmgBlock.allowDamageNow(player)) {
			int damage = 1;
			LoreMaterial material = LoreMaterial.getMaterial(player.getInventory().getItemInMainHand());
			if (material != null) {
				damage = material.onStructureBlockBreak(dmgBlock, damage);
			}
			
			if (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
				AttributeUtil attrs = new AttributeUtil(player.getInventory().getItemInMainHand());
				for (LoreEnhancement enhance : attrs.getEnhancements()) {
					int addDamage = enhance.onStructureBlockBreak(dmgBlock, damage);
					CivMessage.send(player, CivColor.LightGray+enhance.getDisplayName()+" "+enhance.getLevel(attrs)+" does "+(addDamage)+" extra damage!");
					damage += addDamage;
				}
			}
			
			WarStats.incrementPlayerDamageBuildings(player.getName(), damage);
			dmgBlock.getOwner().onDamage(damage, world, player, dmgBlock.getCoord(), dmgBlock);
		} else {
			CivMessage.sendErrorNoRepeat(player, "This block belongs to a "+dmgBlock.getOwner().getDisplayName()+" and cannot be destroyed right now.");
		}
	}
}
