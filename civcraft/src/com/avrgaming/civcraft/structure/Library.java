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
import java.util.ArrayList;

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

import com.avrgaming.civcraft.components.AttributeBiome;
import com.avrgaming.civcraft.components.NonMemberFeeComponent;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.LibraryEnchantment;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.StructureSign;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.template.Template;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Library extends Structure {
	
	private int level;
	public AttributeBiome cultureBeakers;
	private NonMemberFeeComponent nonMemberFeeComponent;
	
	ArrayList<LibraryEnchantment> enchantments = new ArrayList<LibraryEnchantment>();
	
	public static Enchantment getEnchantFromString(String name) {
		// Armor Enchantments
		if (name.equalsIgnoreCase("protection")) {
			return Enchantment.PROTECTION_ENVIRONMENTAL;
		}
		if (name.equalsIgnoreCase("fire_protection")) {
			return Enchantment.PROTECTION_FIRE;
		}
		if (name.equalsIgnoreCase("feather_falling")) {
			return Enchantment.PROTECTION_FALL;
		}
		if (name.equalsIgnoreCase("blast_protection")) {
			return Enchantment.PROTECTION_EXPLOSIONS;
		}
		if (name.equalsIgnoreCase("projectile_protection")) {
			return Enchantment.PROTECTION_PROJECTILE;
		}
		if (name.equalsIgnoreCase("respiration")) {
			return Enchantment.OXYGEN;
		}
		if (name.equalsIgnoreCase("aqua_affinity")) {
			return Enchantment.WATER_WORKER;
		}
		
		// Sword Enchantments
		if (name.equalsIgnoreCase("sharpness")) {
			return Enchantment.DAMAGE_ALL;
		}
		if (name.equalsIgnoreCase("smite")) {
			return Enchantment.DAMAGE_UNDEAD;
		}
		if (name.equalsIgnoreCase("bane_of_arthropods")) {
			return Enchantment.DAMAGE_ARTHROPODS;
		}
		if (name.equalsIgnoreCase("knockback")) {
			return Enchantment.KNOCKBACK;
		}
		if (name.equalsIgnoreCase("fire_aspect")) {
			return Enchantment.FIRE_ASPECT;
		}
		if (name.equalsIgnoreCase("looting")) {
			return Enchantment.LOOT_BONUS_MOBS;
		}
		
		// Tool Enchantments
		if (name.equalsIgnoreCase("efficiency")) {
			return Enchantment.DIG_SPEED;
		}
		if (name.equalsIgnoreCase("silk_touch")) {
			return Enchantment.SILK_TOUCH;
		}
		if (name.equalsIgnoreCase("unbreaking")) {
			return Enchantment.DURABILITY;
		}
		if (name.equalsIgnoreCase("fortune")) {
			return Enchantment.LOOT_BONUS_BLOCKS;
		}
		
		// Bow Enchantments
		if (name.equalsIgnoreCase("power")) {
			return Enchantment.ARROW_DAMAGE;
		}
		if (name.equalsIgnoreCase("punch")) {
			return Enchantment.ARROW_KNOCKBACK;
		}
		if (name.equalsIgnoreCase("flame")) {
			return Enchantment.ARROW_FIRE;
		}
		if (name.equalsIgnoreCase("infinity")) {
			return Enchantment.ARROW_INFINITE;
		}
		return null;
	}
	
	public double getNonResidentFee() {
		return this.nonMemberFeeComponent.getFeeRate();
	}
	
	public void setNonResidentFee(double nonResidentFee) {
		this.nonMemberFeeComponent.setFeeRate(nonResidentFee);
	}
	
	private String getNonResidentFeeString() {
		return "Fee: "+((int)(getNonResidentFee()*100) + "%").toString();		
	}
	
	protected Library(Location center, String id, Town town)
			throws CivException {
		super(center, id, town);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onSave();
	}

	public Library(ResultSet rs) throws SQLException, CivException {
		super(rs);
		nonMemberFeeComponent = new NonMemberFeeComponent(this);
		nonMemberFeeComponent.onLoad();
	}
	
	@Override
	public void loadSettings() {
		super.loadSettings();	
	}

	public int getLevel() {
		return level;
	}


	public void setLevel(int level) {
		this.level = level;
	}
	
	private StructureSign getSignFromSpecialId(int special_id) {
		for (StructureSign sign : getSigns()) {
			int id = Integer.valueOf(sign.getAction());
			if (id == special_id) {
				return sign;
			}
		}
		return null;
	}
	
	@Override
	public void updateSignText() {
		int count = 0;
		for (LibraryEnchantment enchant : this.enchantments) {
			StructureSign sign = getSignFromSpecialId(count);
			if (sign == null) {
				CivLog.error("sign from special id was null, id:"+count);
				return;
			}
			sign.setText(enchant.displayName+"\n"+
					"Level "+enchant.level+"\n"+
					getNonResidentFeeString()+"\n"+
					"For "+enchant.price);
			sign.update();
			count++;
		}
	
		for (; count < getSigns().size(); count++) {
			StructureSign sign = getSignFromSpecialId(count);
			sign.setText("Library Slot\nEmpty");
			sign.update();
		}
	}
	
	public void validateEnchantment(ItemStack item, LibraryEnchantment ench) throws CivException {
		if (ench.enchant != null) {
			
			if(!ench.enchant.canEnchantItem(item)) {
				throw new CivException("You cannot enchant this item with this enchantment.");
			}
			
			if (item.containsEnchantment(ench.enchant) && item.getEnchantmentLevel(ench.enchant) > ench.level) {
				throw new CivException("You already have this enchantment at this level, or better.");
			}
			
			
		} else {
			if (!ench.enhancement.canEnchantItem(item)) {
				throw new CivException("You cannot enchant this item with this enchantment.");
			}
			
			if (ench.enhancement.hasEnchantment(item)) {
				throw new CivException("You already have this enchantment.");
			}
		}
	}
	
	public ItemStack addEnchantment(ItemStack item, LibraryEnchantment ench) {
		if (ench.enchant != null) {
			item.addUnsafeEnchantment(ench.enchant, ench.level);
		} else {
			item = LoreMaterial.addEnhancement(item, ench.enhancement, 1);
		}
		return item;
	}
	
	public void add_enchantment_to_tool(Player player, StructureSign sign, PlayerInteractEvent event) throws CivException {
		int special_id = Integer.valueOf(sign.getAction());

		if (!event.hasItem()) {
			CivMessage.send(player, CivColor.Rose+"You must have the item you wish to enchant in hand.");
			return;
		}
		ItemStack item = event.getItem();
		
		if (special_id >= this.enchantments.size()) {
			throw new CivException("Library enchantment not ready.");
		}
		
		
		LibraryEnchantment ench = this.enchantments.get(special_id);
		this.validateEnchantment(item, ench);
		
		int payToTown = (int) Math.round(ench.price*getNonResidentFee());
		Resident resident;
				
		resident = CivGlobal.getResident(player.getName());
		Town t = resident.getTown();	
		if (t == this.getTown()) {
				// Pay no taxes! You're a member.
				payToTown = 0;
		}					
				
		// Determine if resident can pay.
		if (!resident.getTreasury().hasEnough(ench.price+payToTown)) {
			CivMessage.send(player, CivColor.Rose+"You do not have enough money, you need "+ench.price+payToTown+ " coins.");
			return;
		}
				
		// Take money, give to server, TEH SERVER HUNGERS ohmnom nom
		resident.getTreasury().withdraw(ench.price);
		
		// Send money to town for non-resident fee
		if (payToTown != 0) {
			getTown().deposit(payToTown);
			
			CivMessage.send(player, CivColor.Yellow + "Paid "+ payToTown+" coins in non-resident taxes.");
		}
				
		// Successful payment, process enchantment.
		ItemStack newStack = this.addEnchantment(item, ench);
		player.getInventory().setItemInMainHand(newStack);
		CivMessage.send(player, CivColor.LightGreen+"Enchanted with "+ench.displayName+"!");
	}

	@Override
	public void processSignAction(Player player, StructureSign sign, PlayerInteractEvent event) {
		try {
			add_enchantment_to_tool(player, sign, event);
		} catch (CivException e) {
			CivMessage.send(player, CivColor.Rose+e.getMessage());
		}	
	}
	
	@Override
	public String getDynmapDescription() {
		String out = "<u><b>Library</u></b><br/>";
		
		if (this.enchantments.size() == 0) {
			out += "Nothing stocked.";
		} 
		else {
			for (LibraryEnchantment mat : this.enchantments) {
				out += mat.displayName+" for "+mat.price+"<br/>";
			}
		}
		return out;
	}
	
	
	public ArrayList<LibraryEnchantment> getEnchants() {
		return enchantments;
	}
	
	public void addEnchant(LibraryEnchantment enchant) throws CivException {
		enchantments.add(enchant);
	}
	
	public void removeEnchant(LibraryEnchantment enchant) throws CivException {
		ArrayList<LibraryEnchantment> newEnchs = new ArrayList<LibraryEnchantment>(); boolean found = false;
		for (LibraryEnchantment e : this.enchantments) {
			if (e.displayName.equals(enchant.displayName) && e.level == enchant.level && e.price == enchant.price) {
				found = true; continue;
			} else {
				LibraryEnchantment readdEnchant = new LibraryEnchantment(e.displayName, e.level, e.price);
				newEnchs.add(readdEnchant);
			}
		}
		
		this.reset(); this.enchantments.addAll(newEnchs);
		if (!found) {
			CivLog.warning("Could not remove enchant from Library: "+enchant.displayName+","+enchant.level+","+enchant.price);
		}
	}
	
	@Override
	public String getMarkerIconName() {
		return "bookshelf";
	}

	public void reset() {
		this.enchantments.clear();
		this.updateSignText();
	}
	
	@Override
	public void onPostBuild(BlockCoord absCoord, SimpleBlock sb) {
		switch (sb.command) {
		case "/librarian":
			spawnEnchantingVillager(absCoord.getLocation(), (byte)sb.getData());
			break;
		}
	}
	
	// XXX Villager Information
	
	public void spawnEnchantingVillager(Location loc, int direction) {
		Location vLoc = new Location(loc.getWorld(), loc.getX()+0.5, loc.getY(), loc.getZ()+0.5, Template.faceVillager(direction), 0f);
		Villager v = loc.getWorld().spawn(vLoc, Villager.class);
		v.teleport(vLoc);
		v.setAdult();
		v.setAI(false);
		v.setCustomName("Library Enchanter");
		v.setProfession(Profession.LIBRARIAN);
		CivGlobal.addStructureVillager(v);
	}
	
	public void openEnchantGUI(Player p, Town t) {
		Resident res = CivGlobal.getResident(p);
		Inventory inv = Bukkit.createInventory(null, 9*3, t.getName()+"'s Library Enchanter");
		inv.setItem(0, LoreGuiItem.build(CivColor.LightBlueBold+"Information", ItemManager.getId(Material.PAPER), 0, 
				CivColor.RESET+"This is the Library enchanting menu. You can,",
				CivColor.RESET+"click on an enchantment to add to the item of",
				CivColor.RESET+"your choice, and then a new GUI will display to",
				CivColor.RESET+"insert the item in. Enchanting will require",
				CivColor.RESET+"books, beakers, and possibly trade resources."
				));
		
		for (LibraryEnchantment e : this.enchantments) {
			String out = "";
			out = CivColor.Green+"Enchant: "+CivColor.LightGreen+e.displayName+";";
			out += CivColor.Green+"   At Level: "+CivColor.LightGreen+e.level+";";
			
			if (res.getTreasury().hasEnough(e.price)) out += CivColor.Green+"Cost: "+CivColor.LightGreen+e.price;
			else out += CivColor.Red+"Cost: "+CivColor.Rose+e.price;
			
			ItemStack en = LoreGuiItem.build(e.displayName, 403, 0, out.split(";"));
			inv.addItem(en);
		}
		
		p.openInventory(inv);
	}
}
