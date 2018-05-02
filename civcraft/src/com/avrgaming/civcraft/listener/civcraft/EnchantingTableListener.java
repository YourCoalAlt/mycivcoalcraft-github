package com.avrgaming.civcraft.listener.civcraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class EnchantingTableListener {
	
	// Obect = Enchant/Enhance, Integer = Max Lvl when enchanting
	private Map<Object, Integer> enchants = new HashMap<Object, Integer>();
	
	/* Slot 0 = Enchanting Item, Slot 1-4 required items, Slot 6 EXP Cost, Slot 7 Coin Cost
		Slots 10, 12, 14, 16 show enchant options. These will be randomly selected based on the item and 100% for sure going to be on the item.
		However, additional enchants may be obtained from selections.
		In rows 3 & 4, allow enchants to be put onto an item for a price
		
		All enchants will be available from EXP Costs 50 to 200 levels.
			Maybe a resource good make it cost up to 5% less?
			
		Only enchanting items will cost coins if you request certain enchants.
			Enchants you have researched in the library will cost the same to purchase when enchanting
			Enchants not researched for a town (or civ?) library will cost 10% more to buy
			Enchants to a town (or civ?) without a library will cost 20% more to buy
			
		Enchant/Enhance #1 = 50-100 levels
		Enchant/Enhance #2 = 100-150 levels
		Enchant/Enhance #3 = 150-200 levels
		Enchant/Enhance #4 = 50-200 levels
		
	 */
	
	private ArrayList<Object> getEnchants() {
		ArrayList<Object> e = new ArrayList<Object>();
		
		// Figure out how to get all enchants/enhances available, randomly choose one
		
		return e;
	}
	
}
