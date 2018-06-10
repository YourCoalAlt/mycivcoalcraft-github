package com.avrgaming.civcraft.command.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.command.CommandBase;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class AdminItemCommand extends CommandBase {

	@Override
	public void init() {
		command = "/ad item";
		displayName = "Admin Item";
		
		commands.put("enhance", "[name] - Adds the specified enhancement.");
		commands.put("give", "[player] [custom_id] [amount] - Gives player this custom item.");
	}

	public void give_cmd() throws CivException {
		Resident resident = getNamedResident(1);
		String id = getNamedString(2, "Enter a custom id from materials.yml");
		int amount = getNamedInteger(3);
		
		Player player = CivGlobal.getPlayer(resident);
		
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(id);
		if (craftMat == null) {
			throw new CivException("No custom item with id:"+id);
		}
		
		ItemStack stack = LoreCraftableMaterial.spawn(craftMat);
		
		stack.setAmount(amount);
		HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
		for (ItemStack is : leftovers.values()) {
			player.getWorld().dropItem(player.getLocation(), is);
		}
		
		CivMessage.sendSuccess(player, "Gave item.");
	}
	
	public void enhance_cmd() throws CivException {
		Player player = getPlayer();
		HashMap<String, LoreEnhancement> enhancements = new HashMap<String, LoreEnhancement>();
		ItemStack inHand = getPlayer().getInventory().getItemInMainHand();
		AttributeUtil attr = new AttributeUtil(inHand);
		
		for (LoreEnhancement le : LoreEnhancement.enhancements.values()) {
			enhancements.put(le.getDisplayName().toLowerCase(), le);
		}

		if (inHand == null || ItemManager.getId(inHand) == CivData.AIR) {
			throw new CivException("You must have an item in your hand to enhance it.");
		}
		
		if (args.length < 2) {
			CivMessage.sendHeading(sender, "Possible Enchants");
			String out = "";
			for (String str : enhancements.keySet()) {
				out += str + ", ";
			}
			CivMessage.send(sender, out);
			return;
		}
		
		if (args.length == 2 && args.length < 3) {
			throw new CivException("Please enter a number of times to enhance this item.");
		}
		
		String name = getNamedString(1, "enchantname");
		Integer mult = getNamedInteger(2);
		
		if (mult > 10000) {
			throw new CivException("Cannot enhance an item more than 10,000 times at once.");
		}
		
		ArrayList<String> le = new ArrayList<String>();
		for (LoreEnhancement les : attr.getEnhancements()) le.add(les.getDisplayName());
		
		name.toLowerCase();
		for (String str : enhancements.keySet()) {
			if (name.equals(str)) {
				LoreEnhancement enh = enhancements.get(str);
				if (enh.getMaxLevel() == 1) {
					if (attr.hasEnhancement(enh.getInitName())) {
						throw new CivException("Cannot add this enhancement since this item already has it and can only have it once.");
					} else {
						ItemStack stack = LoreMaterial.addEnhancement(inHand, enh, 1);
						player.getInventory().setItemInMainHand(stack);
						CivMessage.sendSuccess(sender, "Enhanced with "+name+" x"+1);
						CivMessage.send(sender, CivColor.LightGrayItalic+"(Only added once since the max level for this enhancement is 1)");
						return;
					}
				} else {
					ItemStack stack = LoreMaterial.addEnhancement(inHand, enh, mult);
					player.getInventory().setItemInMainHand(stack);
					CivMessage.sendSuccess(sender, "Enhanced with "+name+" x"+mult);
					return;
				}
			}
		}
	}
	
	@Override
	public void doDefaultAction() throws CivException {
		showHelp();
	}

	@Override
	public void showHelp() {
		showBasicHelp();
	}

	@Override
	public void permissionCheck() throws CivException {
		
	}

}
