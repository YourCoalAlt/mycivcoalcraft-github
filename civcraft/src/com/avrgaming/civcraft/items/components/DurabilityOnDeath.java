package com.avrgaming.civcraft.items.components;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.lorestorage.ItemChangeResult;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class DurabilityOnDeath extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		int livesLeft = (int) (1 / this.getDouble("value"));
		attrs.addLore(CivColor.YellowBold+(livesLeft)+CivColor.LightGreen+" Lives Left");
		attrs.setCivCraftProperty("death_percent_value", String.valueOf(this.getDouble("value")));
	}
	
	@Override
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
		if (result == null) {
			result = new ItemChangeResult();
			result.stack = sourceStack;
			result.destroyItem = false;
		}
		
//		// No need to destroy item, we will do it now inside of this (via web)
//		result.stack = LoreEnhancement.getItemLivesLeftViaDurability(event.getEntity(), result.stack, true);
		
		Player p = event.getEntity();
		double percent = this.getDouble("value");
		
		int maxDura = result.stack.getType().getMaxDurability();
		int reduction = (int)(maxDura*percent);
		int durabilityLeft = maxDura - result.stack.getDurability();
		
		if (durabilityLeft >= reduction) {
			int newDurability = (result.stack.getDurability() + reduction);
			result.stack.setDurability((short)newDurability);
			
			AttributeUtil attrs = new AttributeUtil(result.stack);
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
			return result;
		} else {
			result.stack = new ItemStack(Material.WEB);
			AttributeUtil attrs = new AttributeUtil(result.stack);
			String brokeName = result.stack.getItemMeta().getDisplayName();
			attrs.setName(brokeName+" - "+CivColor.LightPurpleBold+"BROKEN");
			attrs.addLore(CivColor.LightGrayItalic+"Your "+brokeName+CivColor.LightGrayItalic+" ran out of Lives and broke!");
			attrs.addEnhancement("LoreEnhancementSoulBound", null, null);	
			result.stack = attrs.getStack();
			
			boolean isEmpty = false;
			for (int i = 0; i < p.getInventory().getContents().length; i++) {
				if (p.getInventory().getContents()[i].getType() == Material.AIR && i < 36) {
					isEmpty = true;
					break;
				}
			}
			
			if (isEmpty) {
				p.getInventory().addItem(result.stack);
			} else {
				CivMessage.send(p, CivColor.LightGrayItalic+"We dropped items back on the ground due to a full inventory.");
				p.getWorld().dropItem(p.getLocation(), result.stack);
			}
		}
		
		return result;
	}
}
