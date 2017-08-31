package com.avrgaming.civcraft.command;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.config.ConfigMaterial;
import com.avrgaming.civcraft.config.ConfigMaterialCategory;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;

import gpl.AttributeUtil;

public class CMatCommand extends CommandBase {
	
	@Override
	public void init() {
		command = "/cmat";
		displayName = "Crafted Material";
		sendUnknownToDefault = true;
		
		commands.put("find", "[name] Finds the recipe of the given name of the item.");
	}
	
	public void find_cmd() throws CivException {
		int MATCH_LIMIT = (9*3);
		
		if (args.length < 2) {
			throw new CivException("Enter a material name. (Tip: If you do not know the item's full name, type in part of it and we will try to find it for you.)");
//			throw new CivException("Enter a material name. (Tip: Use '%' at the end of a name to auto-complete, ex. 'crafted st%' will get 'crafted stone')");
		}
		
		StringBuilder buffer = new StringBuilder();
		for(int i = 1; i < args.length; i++) {
			buffer.append(' ').append(args[i]);
		}
		String name = buffer.toString().toLowerCase();
		String nn = name.toString().substring(0, 1).replace(" ", "")+name.toString().substring(1);
		nn = nn.replace(" ", "_");
		nn = nn.replace("%", "(\\w*)");
				
		ArrayList<String> potentialMatches = new ArrayList<String>();
		
		for (ConfigMaterialCategory cat : ConfigMaterialCategory.getCategories()) {
			for (ConfigMaterial mat : cat.materials.values()) {
				try {
					if (mat.id.contains(nn)) {
						potentialMatches.add(mat.id);
					}
				} catch (Exception e) {
					throw new CivException("Invalid pattern.");
				}
			}
		}
		
		if (potentialMatches.size() > MATCH_LIMIT) {
			throw new CivException("Too many potential matches. Refine your search.");
		}
		
		if (potentialMatches.size() == 0) {
			throw new CivException("Cannot find any matches for "+nn+".");
		}
		
		if (potentialMatches.size() > 0) {
			Inventory inv = Bukkit.createInventory(null, 9*3, "Potential Item Matches");
			for (String s : potentialMatches) {
				
				ItemStack stack = getInfoBookForItem(s);
				if (stack != null) {
					stack = LoreGuiItem.setAction(stack, "ShowRecipeNull");
					inv.addItem(LoreGuiItem.asGuiItem(stack));
				}
			}
			Player p = (Player) sender;
			p.openInventory(inv);
		}
	}
	
	public static ItemStack getInfoBookForItem(String matID) {
		LoreCraftableMaterial loreMat = LoreCraftableMaterial.getCraftMaterialFromId(matID);
		ItemStack stack = LoreMaterial.spawn(loreMat);
							
		if (!loreMat.isCraftable()) {
			return null;
		}
		
		AttributeUtil attrs = new AttributeUtil(stack);
		attrs.removeAll(); /* Remove all attribute modifiers to prevent them from displaying */
		LinkedList<String> lore = new LinkedList<String>();
		
		lore.add(""+ChatColor.RESET+ChatColor.BOLD+ChatColor.GOLD+"Click For Recipe");
		
		attrs.setLore(lore);				
		stack = attrs.getStack();
		return stack;
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}
	
	public void showBasicHelp() {
		CivMessage.sendHeading(sender, displayName+" Command Help");
		for (String c : commands.keySet()) {
			String info = commands.get(c);
			
			info = info.replace("[", CivColor.Yellow+"[");
			info = info.replace("]", "]"+CivColor.LightGray);
			info = info.replace("(", CivColor.Yellow+"(");
			info = info.replace(")", ")"+CivColor.LightGray);
						
			CivMessage.send(sender, CivColor.LightPurple+command+" "+c+CivColor.LightGray+" "+info);
		}
	}

	@Override
	public void permissionCheck() throws CivException {
	}
}
