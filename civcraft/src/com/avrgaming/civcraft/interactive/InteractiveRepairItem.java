package com.avrgaming.civcraft.interactive;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.structure.Barracks;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class InteractiveRepairItem implements InteractiveResponse {

	double cost;
	String playerName;
	ItemStack stack;
	
	public InteractiveRepairItem(double cost, String playerName, ItemStack stack) {
		this.cost = cost;
		this.playerName = playerName;
		this.stack = stack;
	}
	
	public void displayMessage() {
		Player player;
		try {
			player = CivGlobal.getPlayer(playerName);
		} catch (CivException e) {
			return;
		}
		
		String name = "";
		if (stack.getItemMeta().hasDisplayName()) {
			name = stack.getItemMeta().getDisplayName();
		} else {
			name = CivData.getDisplayName(ItemManager.getId(stack), ItemManager.getData(stack));
		}
		
		CivMessage.sendHeading(player, "Repair!");
		CivMessage.send(player, CivColor.LightGreenBold+"Would you like to repair your "+name+CivColor.LightGreenBold+"?");
		CivMessage.send(player, CivColor.LightGreenBold+"I can repair it for "+CivColor.Yellow+CivColor.BOLD+cost+" coins.");
		CivMessage.send(player, CivColor.LightGreenBold+"If that's ok, please type 'yes'. Type anything else to cancel.");
	}
	
	
	@Override
	public void respond(String message, Resident resident) {
		Player player;
		try {
			player = CivGlobal.getPlayer(resident);
			resident.clearInteractiveMode();
			if (!message.equalsIgnoreCase("yes")) {
				CivMessage.send(resident, CivColor.LightGray+"Repair cancelled.");
				LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(stack);
				ItemStack newStack = LoreMaterial.spawn(craftMat);
				AttributeUtil attr = new AttributeUtil(stack); newStack = attr.getStack();
				player.getInventory().addItem(newStack);
				return;
			}
			Barracks.repairItemInHand(cost, resident.getName(), stack);
		} catch (CivException e) { e.printStackTrace(); }
	}
}
