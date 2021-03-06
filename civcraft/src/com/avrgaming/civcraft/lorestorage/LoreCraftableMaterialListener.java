package com.avrgaming.civcraft.lorestorage;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import com.avrgaming.civcraft.items.components.Tagged;
import com.avrgaming.civcraft.loreenhancements.LoreEnhancement;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.CivItem;

import gpl.AttributeUtil;

public class LoreCraftableMaterialListener implements Listener {

	@EventHandler(priority = EventPriority.LOW)
	public void OnCraftItemEvent(CraftItemEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player)event.getWhoClicked();
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(event.getInventory().getResult());
			if (craftMat == null) {
				ItemStack resultStack = event.getInventory().getResult();
				if (resultStack == null) return;
				
				// Disable notch apples
				if (resultStack.getType().equals(Material.GOLDEN_APPLE)) {
					CivMessage.sendError((Player)event.getWhoClicked(), "You cannot craft golden apples. Sorry.");
					event.setCancelled(true);
					return;
				}
				
/*				ConfigTechItem restrictedTechItem = CivSettings.techItems.get(ItemManager.getId(resultStack));
				if (restrictedTechItem != null) {
					ConfigTech tech = CivSettings.techs.get(restrictedTechItem.require_tech);
					CivMessage.sendError(player, "Your civilization doesn't have the required technology ("+tech.name+") to craft this item.");
					event.setCancelled(true);
					return;
				}*/
				return;
			}
			
			if (!craftMat.getConfigMaterial().playerHasTechnology(player)) {
				CivMessage.sendError(player, "You do not have the required technology ("+craftMat.getConfigMaterial().getRequireString()+") to craft this item.");
				event.setCancelled(true);
				return;
			}
		}
	}
	
	
//	private boolean checkCustomMismatch(ItemStack item1, ItemStack item2) {
//		if (LoreMaterial.isCustom(item1)) {
//		//	CivLog.debug("\tmatrix is custom.");
//			if (!LoreMaterial.isCustom(item2)) {
//				/* custom item mismatch. */
//			//	CivLog.debug("custom mismatch.");
//				return false;
//			}
//			
//			LoreMaterial mMatrixMaterial = LoreMaterial.getMaterial(item2);
//			
//			if (!(mMatrixMaterial instanceof LoreCraftableMaterial)) {
//				/* some other kind of custom item, not valid. */
//			//	CivLog.debug("another type of lorecraft.");
//				return false;
//			}
//			
//			LoreCraftableMaterial isMaterial = (LoreCraftableMaterial) LoreMaterial.getMaterial(item1);
//			LoreCraftableMaterial matrixCraftMaterial = (LoreCraftableMaterial)mMatrixMaterial;
//			
//		//	CivLog.debug("\tmatrix:"+isMaterial.getConfigId()+" vs "+matrixCraftMaterial.getConfigId());
//			if (!isMaterial.getConfigId().equals(matrixCraftMaterial.getConfigId())) {
//				/* custom item id's don't match. */
//			//	CivLog.debug("invalid custom");
//				return false;
//			}
//			
//			/* By reaching this point, this itemstack is in the right location and matches this recipe. */
//			//CivLog.debug("item ok.");
//		} else {
//			//CivLog.debug("\tmatrix not custom");
//		}
//		return true;
//	}
	private boolean matrixContainsCustom(ItemStack[] matrix) {
		for (ItemStack stack : matrix) {
			if (LoreMaterial.isCustom(stack)) {
				return true;
			}
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void OnPrepareItemCraftEvent(PrepareItemCraftEvent event) {
		if (event.getRecipe() instanceof ShapedRecipe) {
			String key = LoreCraftableMaterial.getShapedRecipeKey(event.getInventory().getMatrix());
			LoreCraftableMaterial loreMat = LoreCraftableMaterial.shapedKeys.get(key);

			if (loreMat == null) {
				if(LoreCraftableMaterial.isCustom(event.getRecipe().getResult())) {
					/* Result is custom, but we have found no custom recipie. Set to blank. */
					event.getInventory().setResult(CivItem.airStack());
					return;
				}
				
				if (matrixContainsCustom(event.getInventory().getMatrix())) {
					event.getInventory().setResult(CivItem.airStack());
					return;
				}
				
				return;
			} else {
				if(!LoreCraftableMaterial.isCustom(event.getRecipe().getResult())) {
					/* Result is not custom, but recipie is. Set to blank. */
					if (!loreMat.isVanilla()) {
						if (!LoreEnhancement.isTool(event.getRecipe().getResult())) {
							event.getInventory().setResult(CivItem.airStack());
							return;
						}
					}
				}
			}
			
			String matName =loreMat.getId(); 
			if (matName.contains("_alt")) {
				String id = matName.replaceAll("_alt(.*)", "");
				loreMat = LoreCraftableMaterial.getCraftMaterialFromId(id);
			}
			
			ItemStack newStack;
			if (!loreMat.isVanilla()) {
				newStack = LoreMaterial.spawn(loreMat);
				AttributeUtil attrs = new AttributeUtil(newStack);
				loreMat.applyAttributes(attrs);
				newStack.setAmount(loreMat.getCraftAmount());
			} else {
				newStack = CivItem.newStack(loreMat.getTypeID(), loreMat.getCraftAmount(), loreMat.getDamage());
			}
			event.getInventory().setResult(newStack);
			
		} else if (event.getRecipe() instanceof ShapelessRecipe) {
			String key = LoreCraftableMaterial.getShapelessRecipeKey(event.getInventory().getMatrix());
			LoreCraftableMaterial loreMat = LoreCraftableMaterial.shapelessKeys.get(key);
						
			if (loreMat == null) {
				if(LoreCraftableMaterial.isCustom(event.getRecipe().getResult())) {
					/* Result is custom, but we have found no custom recipie. Set to blank. */
					event.getInventory().setResult(CivItem.airStack());
					return;
				}
				
				if (matrixContainsCustom(event.getInventory().getMatrix())) {
					event.getInventory().setResult(CivItem.airStack());
					return;
				}
				
				return;
			} else {
				if(!LoreCraftableMaterial.isCustom(event.getRecipe().getResult())) {
					/* Result is not custom, but recipie is. Set to blank. */
					if (!loreMat.isVanilla()) {
						event.getInventory().setResult(CivItem.airStack());
						return;
					}
				}
			}
			
			String matName =loreMat.getId();
			if (matName.contains("_alt")) {
				String id = matName.replaceAll("_alt(.*)", "");
				loreMat = LoreCraftableMaterial.getCraftMaterialFromId(id);
			}
			
			ItemStack newStack;
			if (!loreMat.isVanilla()) {
				newStack = LoreMaterial.spawn(loreMat);
				AttributeUtil attrs = new AttributeUtil(newStack);
				loreMat.applyAttributes(attrs);
				newStack.setAmount(loreMat.getCraftAmount());
			} else {
				newStack = CivItem.newStack(loreMat.getTypeID(), loreMat.getCraftAmount(), loreMat.getDamage());
			}
			event.getInventory().setResult(newStack);
		}
		
		ItemStack result = event.getInventory().getResult();
		LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(result);
		if (craftMat != null) {
			if (craftMat.hasComponent("Tagged")) {
				String tag = Tagged.matrixHasSameTag(event.getInventory().getMatrix());
				if (tag == null) {
					event.getInventory().setResult(CivItem.airStack());
					return;
				}
				
				Tagged tagged = (Tagged)craftMat.getComponent("Tagged");
				ItemStack stack = tagged.addTag(event.getInventory().getResult(), tag);
				AttributeUtil attrs = new AttributeUtil(stack);
				attrs.addLore(CivColor.Gray+tag);
				stack = attrs.getStack();
				event.getInventory().setResult(stack);
			}
		}
	}
	
}
