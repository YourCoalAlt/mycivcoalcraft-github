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

import org.bukkit.Material;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.ItemChangeResult;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class DurabilityOnDeath extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		int livesLeft = (int) (1 / this.getDouble("value"));
		attrs.addLore(CivColor.YellowBold+(livesLeft)+CivColor.LightGreen+" Lives Left");
		attrs.setCivCraftProperty("death_percent_value", String.valueOf(this.getDouble("value")));
		attrs.setCivCraftProperty("last_death", String.valueOf(System.currentTimeMillis()));
	}

	@Override
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
		if (result == null) {
			result = new ItemChangeResult();
			result.stack = sourceStack;
			result.destroyItem = false;
		}
		
		if (result.destroyItem) return result;
		
		ItemStack newStack = LoreEnhancement.getItemLivesLeftViaDurability(event.getEntity(), result.stack, true);
		if (newStack == null || newStack.getType() == Material.AIR) {
			result.destroyItem = true;
		} else {
			result.stack = newStack;
		}
		return result;
		
/*		AttributeUtil attrs = new AttributeUtil(result.stack);
		// if person died within last 3 seconds, do not take damage to prevent bug.
		if (attrs.getCivCraftProperty("last_death") != null) {
			if ((System.currentTimeMillis() - Long.valueOf(attrs.getCivCraftProperty("last_death"))) <= 3*1000) {
				return result;
			}
		} else {
			attrs.setCivCraftProperty("last_death", String.valueOf(System.currentTimeMillis()));
		}
		
		Player p = event.getEntity();
		double percent = this.getDouble("value");
		int maxDura = result.stack.getType().getMaxDurability();
		int reduction = (int)(maxDura*percent);
		int durabilityLeft = maxDura - result.stack.getDurability();
		attrs.setCivCraftProperty("last_death", String.valueOf(System.currentTimeMillis()));
		if (durabilityLeft > reduction) {
			int newDurability = (result.stack.getDurability() + reduction);
			attrs.getStack().setDurability((short)newDurability);
			
			int dmgpert = (durabilityLeft*100) / maxDura;
			int livesLeft = (int) (dmgpert / (percent*100)) - 1;
			
			String saved = "";
			for (String l : attrs.getLore()) {
				if (!l.contains(" Lives Left")) saved += l+";";
			}
			
			String newSave = "";
			for (String s : saved.split(";")) {
				if (s.contains(" Lives Left")) continue;
				else newSave += s+";";
			}
			
			attrs.setLore(newSave.split(";"));
			attrs.addLore(CivColor.YellowBold+livesLeft+CivColor.LightGreen+" Lives Left");
			result.stack = attrs.getStack();
			
			CivMessage.send(p, CivColor.LightGrayBold+"Your "+attrs.getName()+CivColor.LightGrayBold+" has "+
							CivColor.YellowBold+livesLeft+CivColor.LightGrayBold+" Lives until it breaks!");
		} else {
			CivMessage.send(p, CivColor.LightGrayBold+"Your "+attrs.getName()+CivColor.LightGrayBold+" has "+
					CivColor.YellowBold+"run out of lives"+CivColor.LightGrayBold+" and broke!");
			result.destroyItem = true;
		}
		return result;*/
	}

}