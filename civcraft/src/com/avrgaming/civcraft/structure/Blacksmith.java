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
package com.avrgaming.civcraft.structure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.database.session.SessionEntry;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.items.components.Catalyst;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.threading.tasks.NotificationTask;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.BukkitObjects;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.TimeTools;

import gpl.AttributeUtil;

public class Blacksmith extends Structure {
	
	private static final long COOLDOWN = 1;
	//private static final double BASE_CHANCE = 0.8;
	public static double SMELT_TIME_SECONDS_PER_ITEM = 8.5;
	public static int SMELT_TIME_SECONDS = (int)(3600*1.5);
	public static double YIELD_RATE = 1.25;
	
	private Date lastUse = new Date();
	
	private NonMemberFeeComponent nonMemberFeeComponent;
	
	public static HashMap<BlockCoord, Blacksmith> blacksmithAnvils = new HashMap<BlockCoord, Blacksmith>();

	protected Blacksmith(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
	}

	public Blacksmith(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
	}
	
	public double getNonResidentFee() {
		return nonMemberFeeComponent.getFeeRate();
	}

	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}
	
	private String getNonResidentFeeString() {
		return "Fee: "+((int)(this.nonMemberFeeComponent.getFeeRate()*100) + "%").toString();		
	}
	
	@Override
	public String getDynmapDescription() {
		return null;
	}
	
	@Override
	public String getMarkerIconName() {
		return "factory";
	}
	
	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {
		int special_id = Integer.valueOf(sign.getAction());
		
		Date now = new Date();
		
		long diff = now.getTime() - lastUse.getTime();
		diff /= 1000;
		
		if (diff < Blacksmith.COOLDOWN) {
			throw new CivException("Blacksmith is on cooldown. Please wait another "+(Blacksmith.COOLDOWN - diff)+" seconds.");
		}
		
		lastUse = now;
		
		switch (special_id) {
		case 0:
			this.deposit_forge(player);
			break;
		case 1:
			double cost = CivSettings.getDoubleStructure("blacksmith.forge_cost");
			this.perform_forge(player, cost);
			break;
//		case 1:
//			this.perform_forge_beta(player, player.getInventory().getItemInMainHand(), player.getInventory().getItemInOffHand(), 0);
//			break;
		}
	}
	
	/*
	public void perform_forge_beta(Player p, ItemStack mh, ItemStack oh, double cost) throws CivException {
		mh = p.getInventory().getItemInMainHand();
		oh = p.getInventory().getItemInOffHand();
		
		Map<Enchantment, Integer> mhe = mh.getEnchantments();
		Map<Enchantment, Integer> ohe = oh.getEnchantments();
		
		Map<Enchantment, Integer> newEnch = new HashMap<Enchantment, Integer>();
		newEnch.putAll(mhe);
		newEnch.putAll(ohe);
		String newEnchantList = "Enchantments:";
		
		if (mh.getType() == Material.DIAMOND_PICKAXE && oh.getType() == Material.DIAMOND_PICKAXE) {
			if (mhe.containsKey(Enchantment.DIG_SPEED) && ohe.containsKey(Enchantment.DIG_SPEED)) {
				int mhl = mhe.get(Enchantment.DIG_SPEED); int ohl = ohe.get(Enchantment.DIG_SPEED);
				if (mhl == ohl) {
					int thl = mhl;
					if (thl < 10) {
						if (getUpgradeChance(p, Enchantment.DIG_SPEED, thl) == true) {
							CivMessage.sendSuccess(p, "Successfully turned your Efficiency "+thl+" tools to a level "+(thl+1)+" tool!");
							newEnch.put(Enchantment.DIG_SPEED, thl+1);
							newEnchantList += " Efficiency "+(thl+1)+",";
						} else {
							CivMessage.sendError(p, "Failed to upgrade your Efficiency "+thl+" tools to level "+(thl+1)+".");
						}
					} else if (thl >= 10) {
						CivMessage.sendError(p, "Cannot overenchant anymore, Efficiency can only be maxed at level 10!");
					}
				}
			}
			
			if (mhe.containsKey(Enchantment.DURABILITY) && ohe.containsKey(Enchantment.DURABILITY)) {
				int mhl = mhe.get(Enchantment.DURABILITY); int ohl = ohe.get(Enchantment.DURABILITY);
				if (mhl == ohl) {
					int thl = mhl;
					if (thl < 10) {
						if (getUpgradeChance(p, Enchantment.DURABILITY, thl) == true) {
							CivMessage.sendSuccess(p, "Successfully turned your Unbreaking "+thl+" tools to a level "+(thl+1)+" tool!");
							newEnch.put(Enchantment.DURABILITY, thl+1);
							newEnchantList += " Unbreaking "+(thl+1)+",";
						} else {
							CivMessage.sendError(p, "Failed to upgrade your Unbreaking "+thl+" tools to level "+(thl+1)+".");
						}
					} else if (thl >= 10) {
						CivMessage.sendError(p, "Cannot overenchant anymore, Unbreaking can only be maxed at level 10!");
					}
				}
			}
			
			//TODO finish
			if (mhe.containsKey(Enchantment.LOOT_BONUS_BLOCKS) && ohe.containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
				int mhl = mhe.get(Enchantment.LOOT_BONUS_BLOCKS); int ohl = ohe.get(Enchantment.LOOT_BONUS_BLOCKS);
				if (mhl == ohl) {
					int thl = mhl;
					if (thl < 5) {
						if (getUpgradeChance(p, Enchantment.LOOT_BONUS_BLOCKS, thl) == true) {
							CivMessage.sendSuccess(p, "Successfully turned your Fortune "+thl+" tools to a level "+(thl+1)+" tool!");
							newEnch.put(Enchantment.LOOT_BONUS_BLOCKS, thl+1);
							newEnchantList += " Unbreaking "+(thl+1)+",";
						} else {
							CivMessage.sendError(p, "Failed to upgrade your Fortune "+thl+" tools to level "+(thl+1)+".");
						}
					} else if (thl >= 5) {
						CivMessage.sendError(p, "Cannot overenchant anymore, Fortune can only be maxed at level 10!");
					}
				}
			}
			
			mh.setAmount(0);
			oh.setAmount(0);
			CivMessage.send(p, "Adding your new, combined item to your inventory...");
			CivMessage.send(p, "Your item now contains the following "+newEnchantList.toString());
			ItemStack newItem = new ItemStack(Material.DIAMOND_PICKAXE, 1);
			newItem.addUnsafeEnchantments(newEnch);
			p.getInventory().addItem(newItem);
		}
		
		
	}
	*/
	
	public boolean getUpgradeChance(Player p, Enchantment enchant, int level) throws CivException {
		Random rand = new Random(); //# out of 100
		if (enchant == Enchantment.DIG_SPEED || enchant == Enchantment.DURABILITY) {
			int ch = level*10;
			if (ch == 100) ch = 95;
			int chance = (rand.nextInt(100));
			if (chance >= ch) {
				return true;
			} else {
				return false;
			}
		} else if (enchant == Enchantment.LOOT_BONUS_BLOCKS) {
			int ch = level*20-10;
			int chance = (rand.nextInt(100));
			if (chance >= ch) {
				return true;
			} else {
				return false;
			}
		} else {
			CivMessage.sendError(p, "Couldn't find upgrade for "+enchant.getName().toString());
			return false;
		}
	}
	
	
	@Override
	public void updateSignText() {
		double cost = CivSettings.getDoubleStructure("blacksmith.forge_cost");
		
		for (StructureSign sign : getSigns()) {
			int special_id = Integer.valueOf(sign.getAction());

			switch (special_id) {
			case 0:
				sign.setText("Deposit\nWithdraw\nCatalyst");
				break;
			case 1:
				sign.setText("Forge!\n"+
						"For "+cost+" Coins\n"+
						getNonResidentFeeString());			
				break;
			case 2:
				sign.setText("Deposit\nOre\nResidents Only");
				break;
			case 3:
				sign.setText("Withdraw\nOre\nResidents Only");
				break;
			}
				
			sign.update();
		}
	}
	
	public String getKey(Player player, Structure struct, String tag) {
		return player.getUniqueId().toString()+"_"+struct.getConfigId()+"_"+struct.getCorner().toString()+"_"+tag; 
	}

	public void saveItem(ItemStack item, String key) {
		
		String value = ""+ItemManager.getId(item)+":";
		
		for (Enchantment e : item.getEnchantments().keySet()) {
			value += ItemManager.getId(e)+","+item.getEnchantmentLevel(e);
			value += ":";
		}
		
		sessionAdd(key, value);
	}
	
	public void saveCatalyst(LoreCraftableMaterial craftMat, String key) {
		String value = craftMat.getConfigId();
		sessionAdd(key, value);
	}
	
	public static boolean canSmelt(int blockid) {
		switch (blockid) {
		case CivData.GOLD_ORE:
		case CivData.IRON_ORE:
			return true;
		}
		return false;
	}
		
	/*
	 * Converts the ore id's into the ingot id's
	 */
	public static int convertType(int blockid) {
		switch(blockid) {
		case CivData.IRON_ORE:
			return CivData.IRON_INGOT;
		case CivData.GOLD_ORE:
			return CivData.GOLD_INGOT;
		case CivData.LAPIS_ORE:
			return CivData.DYE;
		case CivData.REDSTONE_ORE:
			return CivData.REDSTONE_DUST;
		case CivData.SAND:
			return CivData.GLASS;
		case CivData.CACTUS:
			return CivData.DYE;
		}
//		return -1;
		return CivData.STONE;
	}
	
	/*
	 * Deposit forge will take the current item in the player's hand
	 * and deposit its information into the sessionDB. It will store the 
	 * item's id, data, and damage.
	 */
	public void deposit_forge(Player player) throws CivException {
		
		ItemStack item = player.getInventory().getItemInMainHand();
		
		ArrayList<SessionEntry> sessions = null;
		String key = this.getKey(player, this, "forge");
		sessions = CivGlobal.getSessionDB().lookup(key);
		
		if (sessions == null || sessions.size() == 0) {
			/* Validate that the item being added is a catalyst */
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterial(item);
			if (craftMat == null || !craftMat.hasComponent("Catalyst")) {
				throw new CivException("You must deposit a catalyst into the forge.");
			}
			
			/* Item is a catalyst. Add it to the session DB. */
			saveCatalyst(craftMat, key);
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount()-1);
			} else {
				player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
			//	player.getInventory().remove(item);
			}
			
			CivMessage.sendSuccess(player, "Deposited Catalyst.");
		} else {
			/* Catalyst already in blacksmith, withdraw it. */
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(sessions.get(0).value);
			if (craftMat == null) {
				throw new CivException("Error withdrawing catalyst from blacksmith. File a bug report!");
			}
			
			ItemStack stack = LoreMaterial.spawn(craftMat);
			HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(stack);
			if (leftovers.size() > 0) {
				for (ItemStack is : leftovers.values()) {
					player.getWorld().dropItem(player.getLocation(), is);
				}
			}
			CivGlobal.getSessionDB().delete_all(key);
			CivMessage.sendSuccess(player, "Withdrawn Catalyst");
		}
	}
	
	/* 
	 * Perform forge will perform the over-enchantment algorithm and determine
	 * if this player is worthy of a higher level pick. If successful it will
	 * give the player the newly created pick.
	 */
	public void perform_forge(Player player, double cost) throws CivException {

		/* Try and retrieve any catalyst in the forge. */
		String key = getKey(player, this, "forge");
		ArrayList<SessionEntry> sessions = CivGlobal.getSessionDB().lookup(key);
		
		/* Search for free catalyst. */
		ItemStack stack = player.getInventory().getItemInMainHand();
		AttributeUtil attrs = new AttributeUtil(stack);
		Catalyst catalyst;
		
		
		String freeStr = attrs.getCivCraftProperty("freeCatalyst");
		if (freeStr == null) {
			/* No free enhancements on item, search for catalyst. */
			if (sessions == null || sessions.size() == 0) {
				throw new CivException("No catalyst in the forge. Deposit one first.");
			}
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(sessions.get(0).value);
			if (craftMat == null) {
				throw new CivException("Error getting catalyst from blacksmith. File a bug report!");
			}
			
			catalyst = (Catalyst)craftMat.getComponent("Catalyst");
			if (catalyst == null) {
				throw new CivException("Error getting catalyst from blacksmith. Please file a bug report.");
			}
		} else {
			String[] split = freeStr.split(":");
			Double level = Double.valueOf(split[0]);
			String mid = split[1];
			
			LoreCraftableMaterial craftMat = LoreCraftableMaterial.getCraftMaterialFromId(mid);
			if (craftMat == null) {
				throw new CivException("Error getting catalyst from blacksmith. File a bug report!");
			}

			catalyst = (Catalyst)craftMat.getComponent("Catalyst");
			if (catalyst == null) {
				throw new CivException("Error getting catalyst from blacksmith. Please file a bug report.");
			}
			
			/* reduce level and reset item. */
			level--;
			
			String lore[] = attrs.getLore();
			for (int i = 0; i < lore.length; i++) {
				String str = lore[i];
				if (str.contains("free enhancements")) {
					if (level != 0) {
						lore[i] = CivColor.LightBlue+level+" free enhancements! Redeem at blacksmith.";
					} else {
						lore[i] = "";
					}
					break;
				}
			}
			attrs.setLore(lore);
			
			if (level != 0) {
				attrs.setCivCraftProperty("freeCatalyst", level+":"+mid);
			} else {
				attrs.removeCivCraftProperty("freeCatalyst");
			}
			
			player.getInventory().setItemInMainHand(attrs.getStack());
			
		}
		
		stack = player.getInventory().getItemInMainHand();
		ItemStack enhancedItem = catalyst.getEnchantedItem(stack);
		
		if (enhancedItem == null) {
			throw new CivException("You cannot use this catalyst on this item.");
		}
		
		/* Consume the enhancement. */
		CivGlobal.getSessionDB().delete_all(key);
		
		if (!catalyst.enchantSuccess(enhancedItem)) {
			/* 
			 * There is a one in third chance that our item will break.
			 * Sucks, but this is what happened here.
			 */
			player.getInventory().setItemInMainHand(ItemManager.createItemStack(CivData.AIR, 1));
			CivMessage.sendError(player, "Enhancement failed. Item has broken.");
			return;
		} else {
			player.getInventory().setItemInMainHand(enhancedItem);
			CivMessage.sendSuccess(player, "Enhancement succeeded!");
			return;
		}
	}
	
	// XXX New Blacksmith Code
	
	public void spawnSmelterVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Blacksmith Smelter");
		v.setProfession(Profession.BLACKSMITH);
		for (Villager vg : CivGlobal.structureVillagers.keySet()) {
			if (vg.getLocation().equals(v.getLocation())) {
				CivGlobal.removeStructureVillager(v);
				v.remove();
			}
		}
		CivGlobal.addStructureVillager(v);
	}
	
	public void openSmeltGUI(Player p, Town town) {
		Inventory inv = Bukkit.createInventory(null, 9*2, town.getName()+"'s Smelter Operator");
		
		inv.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Blacksmith Smelter Menu. You can",
				CivColor.RESET+"use it to smelt different types of ores to",
				CivColor.RESET+"get a better yield than furnances. The more",
				CivColor.RESET+"type of an item you deposit, your chance of",
				CivColor.RESET+"better yield gets increased!"
				));
		
		ItemStack item = new ItemStack(Material.MAGMA, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(CivColor.WhiteBold+"Current Smelts");
		List<String> lore = new ArrayList<>();
		lore.addAll(this.checkCurrentSmelts(p));
		lore.add(CivColor.Purple+"<Click to Withdraw Completed Smelts>");
		meta.setLore(lore);
		item.setItemMeta(meta);
		inv.setItem(9, item);
		
		for (int i = 10; i < 18; i++) {
			ItemStack is = LoreGuiItem.build(CivColor.WhiteBold+"^^^^", ItemManager.getId(Material.IRON_FENCE), 0, CivColor.LightGreenBold+"Deposit");
			inv.setItem(i, is);
		}
		
		p.openInventory(inv);
	}
	
	public List<String> checkCurrentSmelts(Player player) {
		List<String> checks = new ArrayList<>();
		
		// Only members can use the smelter
		Resident res = CivGlobal.getResident(player.getName());
		if (!res.hasTown() || this.getTown() != res.getTown()) {
			checks.add("Can't use this smelter, you are not a town member!");
			return checks;
		}
		
		String key = getKey(player, this, "smelt");
		ArrayList<SessionEntry> entries = null;
		entries = CivGlobal.getSessionDB().lookup(key);
		
		if (entries == null || entries.size() == 0) {
			checks.add(CivColor.Rose+"No smelts to withdraw.");
			return checks;
		}
		
		for (SessionEntry se : entries) {
			String split[] = se.value.split(":");
			int itemId = Integer.valueOf(split[0]);
			int amount = Integer.valueOf(split[1]);
			int data = Integer.valueOf(split[2]);
			long now = System.currentTimeMillis();
			int secondsBetween = CivGlobal.getSecondsBetween(se.time, now);
			
			if (secondsBetween < (Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amount)) {
				DecimalFormat df1 = new DecimalFormat("0.#");
				double timeLeft = ((double)(Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amount) - (double)secondsBetween) / (double)60;
				checks.add(CivColor.Yellow+amount+" "+CivData.getDisplayName(itemId, data)+" will be finished in "+df1.format(timeLeft)+" minutes.");
				continue;
			} else {
				checks.add(CivColor.LightGreen+amount+" "+CivData.getDisplayName(itemId, data)+" is finished.");
				continue;
			}
		}	
		return checks;
	}
	
	public void depositSmelt(Player player, Material mat, int amt, int data) {
		int iid = ItemManager.getId(mat);
		String value = convertType(iid)+":"+(int) (amt*Blacksmith.YIELD_RATE)+":"+data;
		String key = getKey(player, this, "smelt");
		sessionAdd(key, value);
		
		// Schedule a message to notify the player when the smelting is finished.
		BukkitObjects.scheduleAsyncDelayedTask(new NotificationTask(player.getName(), 
				CivColor.LightGreen+" Your stack of "+amt+" "+CivData.getDisplayName(iid, data)+" has finished smelting."), TimeTools.toTicks((long) (Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amt)));
		
		
		// TODO We need to make a timer to add to resident's mail or else they won't recieve the items.
/*		int full = 0; int partial = 0;
		for (int stackRefine = (int) (amt*Blacksmith.YIELD_RATE); stackRefine >= 64; stackRefine -= 64) {
			if (stackRefine < 64) partial = stackRefine;
			else full += 1;
		}
		
		for (int i = 0; i < full; i++) {
			ItemStack item = new ItemStack(ItemManager.getMaterial(iid), 64);
			String is = InventorySerializer.getSerializedItemStack(item);
			BukkitObjects.scheduleAsyncDelayedTask(new MailToResidentTask(player.getUniqueId().toString(), is), TimeTools.toTicks((long) (Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amt)));
		}
		
		if (partial > 0) {
			ItemStack item = new ItemStack(ItemManager.getMaterial(iid), partial);
			String is = InventorySerializer.getSerializedItemStack(item);
			BukkitObjects.scheduleAsyncDelayedTask(new MailToResidentTask(player.getUniqueId().toString(), is), TimeTools.toTicks((long) (Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amt)));
		}*/
		
		CivMessage.send(player,CivColor.LightGreen+ "Deposited "+amt+" "+CivData.getDisplayName(iid, data)+".");
	}
	
	public void withdrawSmelts(Player p) {
		Resident res = CivGlobal.getResident(p.getName());
		if (!res.hasTown() || this.getTown() != res.getTown()) {
			CivMessage.sendError(p, "Can only use the smelter if you are a town member.");
			return;
		}
		
		String key = getKey(p, this, "smelt");
		ArrayList<SessionEntry> entries = null;
		entries = CivGlobal.getSessionDB().lookup(key);
		
		if (entries == null || entries.size() == 0) {
			CivMessage.sendError(p, "No items to withdraw from the smelter at this time.");
			return;
		}
		
		Inventory inv = p.getInventory();
		HashMap <Integer, ItemStack> leftovers = null;
		
		for (SessionEntry se : entries) {
			String split[] = se.value.split(":");
			int itemId = Integer.valueOf(split[0]);
			int amount = Integer.valueOf(split[1]);
			int data = Integer.valueOf(split[2]);
			long now = System.currentTimeMillis();
			int secondsBetween = CivGlobal.getSecondsBetween(se.time, now);
			
			// First determine the time between two events.
			if (secondsBetween > (Blacksmith.SMELT_TIME_SECONDS_PER_ITEM*amount)) {
				ItemStack stack = new ItemStack(ItemManager.getMaterial(itemId), amount, (short)data);
				if (stack != null) leftovers = inv.addItem(stack);
				
				// If this stack was successfully withdrawn, delete it from the DB.
				if (leftovers.size() == 0) {
					CivGlobal.getSessionDB().delete(se.request_id, se.key);
					CivMessage.send(p, CivColor.LightGreen+"Withdrew "+amount+" "+CivData.getDisplayName(itemId, data));
					break;
				} else {
					// We do not have space in our inventory, inform the player.
					CivMessage.send(p, CivColor.Rose+"Not enough inventory space for all items.");
					
					// If the leftover size is the same as the size we are trying to withdraw, do nothing.
					int leftoverAmount = CivGlobal.getLeftoverSize(leftovers);
					
					if (leftoverAmount == amount) {
						continue;
					}
					
					if (leftoverAmount <= 0) {
						CivGlobal.getSessionDB().delete(se.request_id, se.key);
					} else {							
						// Some of the items were deposited into the players inventory but the sessionDB 
						// still has the full amount stored, update the db to only contain the leftovers.
						String newValue = itemId+":"+leftoverAmount+":"+data;			
						CivGlobal.getSessionDB().update(se.request_id, se.key, newValue);
					}
				}
			} else {
				continue;
			}
			break; // only withdraw one item at a time.
		}
	}
}
