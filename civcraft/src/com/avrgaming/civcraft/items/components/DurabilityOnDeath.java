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
		
/*		AttributeUtil attrs = new AttributeUtil(result.stack);
		
		// if person died within last 4 seconds, do not take damage to prevent bug.
		if ((System.currentTimeMillis() - Long.valueOf(attrs.getCivCraftProperty("last_death"))) <= 4*1000) {
			return result;
		}
		
		double percent = this.getDouble("value");
		int reduction = (int)(result.stack.getType().getMaxDurability()*percent);
		int durabilityLeft = result.stack.getType().getMaxDurability() - result.stack.getDurability();
		
		if (durabilityLeft > reduction) {
			result.stack.setDurability((short)(result.stack.getDurability() + reduction));
			
			int dmgpert = (durabilityLeft*100) / result.stack.getType().getMaxDurability();
			int livesLeft = (int) (dmgpert / (percent*100)) - 1;
			
			String saved = "";
			for (String l : attrs.getLore()) {
				if (!l.contains(" Lives Left")) saved += l+";";
			}
			
			attrs.setLore(saved.split(";"));
			attrs.addLore(CivColor.YellowBold+livesLeft+CivColor.LightGreen+" Lives Left");
			CivMessage.sendNoRepeat(event.getEntity(), CivColor.LightGrayBold+"Your "+attrs.getName()+CivColor.LightGrayBold+" has "+
					CivColor.YellowBold+livesLeft+CivColor.LightGrayBold+" Lives until it breaks!");
		} else {
			result.destroyItem = true;
			CivMessage.sendNoRepeat(event.getEntity(), CivColor.LightGrayBold+"Your "+attrs.getName()+CivColor.LightGrayBold+" has "+
					CivColor.YellowBold+"run out of lives"+CivColor.LightGrayBold+" and broke!");
		}
		
		result.stack = attrs.getStack();*/
		
		ItemStack newStack = LoreEnhancement.deductLivesAndDurability(event.getEntity(), result.stack, this.getDouble("value"), true);
		if (newStack == null || newStack.getType() == Material.AIR) {
			result.destroyItem = true;
		} else {
			result.stack = newStack;
		}
		
		return result;
	}

}