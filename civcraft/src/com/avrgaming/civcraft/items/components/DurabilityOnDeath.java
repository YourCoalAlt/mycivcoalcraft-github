package com.avrgaming.civcraft.items.components;

import org.bukkit.Material;
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
	}
	
	@Override
	public ItemChangeResult onDurabilityDeath(PlayerDeathEvent event, ItemChangeResult result, ItemStack sourceStack) {
		if (result == null) {
			result = new ItemChangeResult();
			result.stack = sourceStack;
			result.destroyItem = false;
		}
		
		if (result.destroyItem) return result;
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
			
			CivMessage.send(event.getEntity(), CivColor.LightGrayBold+"Your "+attrs.getName()+CivColor.LightGrayBold+" has "+
							CivColor.YellowBold+livesLeft+CivColor.LightGrayBold+" Lives until it breaks!");
		} else {
			int slot = 999;
			for (int i = 0; i < event.getEntity().getInventory().getContents().length; i++) {
				if (event.getEntity().getInventory().getContents()[i] == result.stack &&
						event.getEntity().getInventory().getContents()[i].getItemMeta() == result.stack.getItemMeta() && i < 36) {
					slot = i;
					continue;
				}
			}
			
			ItemStack is = new ItemStack(Material.WEB);
			AttributeUtil attrs = new AttributeUtil(is);
			String brokeName = result.stack.getItemMeta().getDisplayName();
			attrs.setName(brokeName+" - "+CivColor.LightPurpleBold+"BROKEN");
			attrs.addLore("Your "+brokeName+" ran out of Lives and broke!");
			attrs.addEnhancement("LoreEnhancementSoulBound", null, null);	
			is = attrs.getStack();
			
			if (slot != 999) {
				event.getEntity().getInventory().setItem(slot, is);
			} else {
				result.destroyItem = true;
				event.getEntity().getInventory().addItem(is);
			}
		}
		return result;
	}
}
