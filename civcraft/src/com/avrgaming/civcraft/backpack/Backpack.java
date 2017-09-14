package com.avrgaming.civcraft.backpack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Base64;
import java.util.LinkedList;

import javax.xml.bind.DatatypeConverter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigEXPGenericLevel;
import com.avrgaming.civcraft.config.ConfigFishing;
import com.avrgaming.civcraft.config.ConfigMaterial;
import com.avrgaming.civcraft.config.ConfigMaterialCategory;
import com.avrgaming.civcraft.lorestorage.LoreCraftableMaterial;
import com.avrgaming.civcraft.lorestorage.LoreGuiItem;
import com.avrgaming.civcraft.lorestorage.LoreGuiItemListener;
import com.avrgaming.civcraft.lorestorage.LoreMaterial;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.ResidentExperience;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import gpl.AttributeUtil;

public class Backpack {

	public static Inventory tutorialInventory = null;
	public static Inventory craftingHelpInventory = null;
	public static Inventory guiInventory = null;
	public static final int MAX_CHEST_SIZE = 6;
	
	public static Inventory experienceHelpInv = null;
	public static Inventory miningRateInv = null;
	public static Inventory fishingRateInv = null;
	
	public static void showExperienceHelp(Player player) {
		if (experienceHelpInv == null) {
			experienceHelpInv = Bukkit.getServer().createInventory(player, 9*3, "Experience Categories");
			
//			ItemStack mining = LoreGuiItem.build(CivColor.LightBlueBold+"Mining", CivData.WOOD_PICKAXE, 0, CivColor.LightGray+"« Click for Rates »");
//			mining = LoreGuiItem.setAction(mining, "OpenInventory");
//			mining = LoreGuiItem.setActionData(mining, "invType", "showMiningRates");
//			experienceHelpInv.addItem(mining);
			
			ItemStack fishing = LoreGuiItem.build(CivColor.LightBlueBold+"Fishing", CivData.RAW_FISH, 0, CivColor.LightGray+"« Click for Rates »");
			fishing = LoreGuiItem.setAction(fishing, "OpenInventory");
			fishing = LoreGuiItem.setActionData(fishing, "invType", "showFishingRates");
			experienceHelpInv.addItem(fishing);
			
			ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Main Menu");
			backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
			backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
			backButton = LoreGuiItem.setActionData(backButton, "invName", guiInventory.getName());
			experienceHelpInv.setItem((9*3)-1, backButton);
			
			LoreGuiItemListener.guiInventories.put(experienceHelpInv.getName(), experienceHelpInv);
		}
		
		if (player != null && player.isOnline() && player.isValid()) {
			player.openInventory(experienceHelpInv);	
		}
	}
	
	public static void showFishingRates(Player player) {
		fishingRateInv = null;
		if (fishingRateInv == null) {
			fishingRateInv = Bukkit.getServer().createInventory(player, 9*3, "Fishing Drops");
			
			Resident res = CivGlobal.getResident(player);
			ResidentExperience re = CivGlobal.getResidentE(player);
			if (res != null && re != null) {
//				ConfigEXPFishingLevel fishinglvl = CivSettings.expFishingLevels.get(re.getFishingLevel());
				for (ConfigFishing d : CivSettings.fishingDrops) {
					double dc = d.drop_chance;
					float mod = (float) (((double) (re.getFishingLevel()-1) / 2) / 100);
					double mod2 = re.getFishingLevel() + 1;
					if (d.loot_type.contains("treasure")) {
						dc += (mod/2);
					} else if (d.loot_type.contains("legendary")) {
						dc += (mod/10);
					} else if (d.loot_type.contains("junk") || d.loot_type.contains("fish")) {
						dc -= mod;
					} else {
						CivLog.warning("Fishing Rates (Backpack) had unknown loot type, "+d.loot_type);
					}
					
					if (dc < 0) {
						dc = d.drop_chance;
					}
					
					String out = "";
					out += CivColor.Green+"Type: "+CivColor.LightGreen+d.loot_type.substring(0, 1).toUpperCase()+d.loot_type.substring(1)+";";
					DecimalFormat df = new DecimalFormat("#.###");
					out += CivColor.Green+"Chance: "+CivColor.LightGreen+(df.format(dc*100))+"%;";
					out += CivColor.Green+"XP Orbs: "+CivColor.LightGreen+((int) (d.exp_min*mod2) / 2)+"-"+((int) (d.exp_max*mod2) / 2)+";";
					out += CivColor.Green+"Fishing XP: "+CivColor.LightGreen+Double.valueOf(df.format((d.res_exp*mod2)+0.0));
					
					if (d.custom_id != null) {
						LoreCraftableMaterial cmat = LoreCraftableMaterial.getCraftMaterialFromId(d.custom_id);
						out += ";Custom Material";
						if (cmat.isCraftable()) {
							//out += ";"+CivColor.LightGreen+"Click For Recipe";
							ItemStack stack = getInfoBookForItem(cmat.getConfigId(), out);
							stack = LoreGuiItem.setAction(stack, "ShowRecipeNull");
							fishingRateInv.addItem(LoreGuiItem.asGuiItem(stack));
						} else {
							out += ";"+CivColor.Rose+"Not Craftable";
							ItemStack fishing = LoreGuiItem.build(CivColor.White+cmat.getName(), cmat.getTypeID(), cmat.getDamage(), out.split(";"));
							fishingRateInv.addItem(fishing);
						}
					} else {
						out += ";"+CivColor.LightGray+"Vanilla Item";
						ItemStack fishing = LoreGuiItem.build(CivColor.White+CivData.getDisplayName(d.type_id, d.type_data), d.type_id, d.type_data, out.split(";"));
						fishingRateInv.addItem(fishing);
					}
				}
			} else {
				ItemStack fishing = LoreGuiItem.build(CivColor.LightBlueBold+"Fishing", CivData.RAW_FISH, 0, 
						CivColor.Rose+"Error getting experience.",
						CivColor.RESET+"Relog or contact an admin!");
				fishingRateInv.addItem(fishing);
			}
			
			ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Experience Menu");
			backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
			backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
			backButton = LoreGuiItem.setActionData(backButton, "invName", experienceHelpInv.getName());
			fishingRateInv.setItem((9*3)-1, backButton);
			
			LoreGuiItemListener.guiInventories.put(fishingRateInv.getName(), fishingRateInv);
		}
		
		if (player != null && player.isOnline() && player.isValid()) {
			player.openInventory(fishingRateInv);	
		}
	}
	
/*	public static void showMiningRates(Player player) {
		miningRateInv = null;
		if (miningRateInv == null) {
			miningRateInv = Bukkit.getServer().createInventory(player, 9*3, "Mining Drops");
			
			Resident res = CivGlobal.getResident(player);
			ResidentExperience re = CivGlobal.getResidentE(player);
			if (res != null && re != null) {
//				ConfigEXPFishingLevel fishinglvl = CivSettings.expFishingLevels.get(re.getFishingLevel());
				for (ConfigEXPMining d : CivSettings.resxpMiningBlocks.values()) {
					double mod = re.getFishingLevel() + 1; mod /= 2;
					
					String out = "";
					out += CivColor.Green+"Type: "+CivColor.LightGreen+"Ore;";
					DecimalFormat df = new DecimalFormat("#.##");
					out += CivColor.Green+"XP Orbs: "+CivColor.LightGreen+"N/A;";
					out += CivColor.Green+"Fishing XP: "+CivColor.LightGreen+Double.valueOf(df.format((d.resxp*mod)));
					
					out += ";"+CivColor.LightGray+"Vanilla Item";
					ItemStack mining = LoreGuiItem.build(CivColor.White+CivData.getDisplayName(d.id, 0), d.id, 0, out.split(";"));
					miningRateInv.addItem(mining);
				}
			} else {
				ItemStack mining = LoreGuiItem.build(CivColor.LightBlueBold+"Mining", CivData.WOOD_PICKAXE, 0, 
						CivColor.Rose+"Error getting experience.",
						CivColor.RESET+"Relog or contact an admin!");
				miningRateInv.addItem(mining);
			}
			
			ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Experience Menu");
			backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
			backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
			backButton = LoreGuiItem.setActionData(backButton, "invName", experienceHelpInv.getName());
			miningRateInv.setItem((9*3)-1, backButton);
			
			LoreGuiItemListener.guiInventories.put(miningRateInv.getName(), miningRateInv);
		}
		
		if (player != null && player.isOnline() && player.isValid()) {
			player.openInventory(miningRateInv);	
		}
	}*/
	
	public static void showTownMenu(Player p) {
		Resident res = CivGlobal.getResident(p);
		if (res != null) {
			if (!res.hasTown()) {
				Inventory inv = Bukkit.createInventory(null, 9*1, "Cannot View - No Town");
				p.openInventory(inv);
			} else {
				res.getTown().getTownHall().openMainInfoGUI(p, res.getTown());
			}
		}
	}
	
	public static void showTutorialInventory(Player player) {	
		if (tutorialInventory == null) {
			tutorialInventory = Bukkit.getServer().createInventory(player, 9*3, "CivCraft Tutorial");
			
			tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"What is CivCraft?", ItemManager.getId(Material.WORKBENCH), 0, 
				CivColor.RESET+"CivCraft is a game about building civilizations set in a large,",
				CivColor.RESET+"persistent world filled with players.",
				CivColor.RESET+"Players start out as nomads, gathering",
				CivColor.RESET+"resources and making allies until they can build a camp.",
				CivColor.RESET+"Gather more resources and allies and found a civilization!",
				CivColor.RESET+CivColor.LightGreen+"Research technology! Build structures! Conquer the world!"
				));
		
			tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Explore", ItemManager.getId(Material.COMPASS), 0, 
					CivColor.RESET+"Venture outward from spawn into the wild",
					CivColor.RESET+"and find a spot to settle. You may encounter",
					CivColor.RESET+"trade resources, and other player towns which",
					CivColor.RESET+"will infulence your decision on where to settle.",
					CivColor.RESET+"Different biomes generate different resources."
					));
			
			tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Resources and Materials", ItemManager.getId(Material.DIAMOND_ORE), 0, 
					CivColor.RESET+"CivCraft contains many new custom items.",
					CivColor.RESET+"These items are crafted using a crafting bench",
					CivColor.RESET+"and combining many more normal Minecraft items",
					CivColor.RESET+"into higher tier items. Certain items like iron, gold,",
					CivColor.RESET+"diamonds and emeralds can be exchanged for coins at "+CivColor.Yellow+"Bank",
					CivColor.RESET+"structures. Coins can be traded for materials at the "+CivColor.Yellow+"Market"
					));
			
			tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Towns", ItemManager.getId(Material.FENCE), 0, 
					CivColor.RESET+"Towns can be created by players to protect",
					CivColor.RESET+"areas from outsiders. Inside a town the owners are",
					CivColor.RESET+"free to build creatively without interference from griefers",
					CivColor.RESET+"Towns cost materials to create and coins to maintain.",
					CivColor.RESET+"Towns can build functional structures which allow it's",
					CivColor.RESET+"residents access to more features. Towns can only be built",
					CivColor.RESET+"inside of a civilization."
					));
			
			tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Civilizations", ItemManager.getId(Material.GOLD_HELMET), 0, 
					CivColor.RESET+"Civilizations are collections of towns",
					CivColor.RESET+"All towns inside of the civilization share technology",
					CivColor.RESET+"which is researched by the civ. Many items and structures",
					CivColor.RESET+"in CivCraft are only obtainable through the use of technology",
					CivColor.RESET+"Founding your own civ is a lot of work, you must be a natural",
					CivColor.RESET+"leader and bring people together in order for your civ to survive",
					CivColor.RESET+"and flourish."
					));
			
			if (CivGlobal.isCasualMode()) {
				tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"Casual War!", ItemManager.getId(Material.FIREWORK), 0, 
						CivColor.RESET+"War allows civilizations to settle their differences.",
						CivColor.RESET+"In casual mode, Civs have to the option to request war from",
						CivColor.RESET+"each other. The winner of a war is awarded a trophy which can be",
						CivColor.RESET+"displayed in an item frame for bragging rights.",
						CivColor.RESET+"After a civilization is defeated in war, war must be requested again."
						));
			} else {
				tutorialInventory.addItem(LoreGuiItem.build(CivColor.LightBlueBold+"War!", ItemManager.getId(Material.IRON_SWORD), 0, 
						CivColor.RESET+"War allows civilizations to settle their differences.",
						CivColor.RESET+"Normally, all structures inside a civilization are protected",
						CivColor.RESET+"from damage. However civs have to the option to declare war on",
						CivColor.RESET+"each other and do damage to each other's structures, and even capture",
						CivColor.RESET+"towns from each other. Each weekend, WarTime is enabled for two hours",
						CivColor.RESET+"during which players at war must defend their civ and conquer their enemies."
						));
			}
			
			tutorialInventory.setItem(8, LoreGuiItem.build(CivColor.LightBlueBold+"More Info?", ItemManager.getId(Material.BOOK_AND_QUILL), 0, 
					CivColor.RESET+"There is much more information you will require for your",
					CivColor.RESET+"journey into CivCraft. Please visit the wiki at ",
					CivColor.RESET+CivColor.LightGreenBold+"http://civcraft.net/wiki",
					CivColor.RESET+"For more detailed information about CivCraft and it's features."
					));
			
			tutorialInventory.setItem(9, LoreGuiItem.build(CivColor.LightBlueBold+"QUEST: Build a Camp", ItemManager.getId(Material.BOOK_AND_QUILL), 0, 
					CivColor.RESET+"First things first, in order to start your journey",
					CivColor.RESET+"you must first build a camp. Camps allow you to store",
					CivColor.RESET+"your materials safely, and allow you to obtain leadership",
					CivColor.RESET+"tokens which can be crafted into a civ. The recipe for a camp is below."
					));
			
			//tutorialInventory.setItem(18,getInfoBookForItem("civ_found_camp"));
			
			tutorialInventory.setItem(10, LoreGuiItem.build(CivColor.LightBlueBold+"QUEST: Found a Civ", ItemManager.getId(Material.BOOK_AND_QUILL), 0, 
					CivColor.RESET+"Next, you'll want to start a civilization.",
					CivColor.RESET+"To do this, you must first obtain leadership tokens",
					CivColor.RESET+"by feeding bread to your camp's longhouse.",
					CivColor.RESET+"Once you have enough leadership tokens.",
					CivColor.RESET+"You can craft the founding flag item below."
					));
			
			//tutorialInventory.setItem(19,getInfoBookForItem("civ_found_civ"));
			
			tutorialInventory.setItem(11, LoreGuiItem.build(CivColor.LightBlueBold+"Need to know a recipe?", ItemManager.getId(Material.WORKBENCH), 0, 
					CivColor.RESET+"Type /res book to obtain the tutorial book",
					CivColor.RESET+"and then click on 'Crafting Recipies'",
					CivColor.RESET+"Every new item in CivCraft is listed here",
					CivColor.RESET+"along with how to craft them.",
					CivColor.RESET+"Good luck!"
					));
		
			LoreGuiItemListener.guiInventories.put(tutorialInventory.getName(), tutorialInventory);
		}
		
		if (player != null && player.isOnline() && player.isValid()) {
			player.openInventory(tutorialInventory);	
		}
	}
	
	public static ItemStack getInfoBookForItem(String matID) {
		LoreCraftableMaterial loreMat = LoreCraftableMaterial.getCraftMaterialFromId(matID);
		ItemStack stack = LoreMaterial.spawn(loreMat, 1);
		if (!loreMat.isCraftable()) {
			return null;
		}
		
		AttributeUtil attrs = new AttributeUtil(stack);
		attrs.removeAll(); // Remove all attribute modifiers to prevent them from displaying
		LinkedList<String> lore = new LinkedList<String>();
		lore.add(CivColor.GoldBold+"Click For Recipe");
		attrs.setLore(lore);				
		stack = attrs.getStack();
		return stack;
	}
	
	public static ItemStack getInfoBookForItem(String matID, String lore) {
		LoreCraftableMaterial loreMat = LoreCraftableMaterial.getCraftMaterialFromId(matID);
		ItemStack stack = LoreMaterial.spawn(loreMat, 1);
		if (!loreMat.isCraftable()) {
			return null;
		}
		
		AttributeUtil attrs = new AttributeUtil(stack);
		attrs.removeAll(); // Remove all attribute modifiers to prevent them from displaying
		for (String s : lore.split(";")) {
			attrs.addLore(s);
		}
		attrs.addLore(CivColor.GoldBold+"Click For Recipe");
		stack = attrs.getStack();
		return stack;
	}
	
	public static void showCraftingHelp(Player player) {
		if (craftingHelpInventory == null) {
			craftingHelpInventory = Bukkit.getServer().createInventory(player, 5*9, "CivCraft Custom Item Recipes");

			/* Build the Category Inventory. */
			for (ConfigMaterialCategory cat : ConfigMaterialCategory.getCategories()) {
				if (cat.craftableCount == 0) {
					continue;
				}
				
				ItemStack infoRec = LoreGuiItem.build(cat.name, ItemManager.getId(Material.WRITTEN_BOOK), 0, 
						CivColor.LightBlue+cat.materials.size()+" Items", CivColor.Gold+"<Click To Open>");
						infoRec = LoreGuiItem.setAction(infoRec, "OpenInventory");
						infoRec = LoreGuiItem.setActionData(infoRec, "invType", "showGuiInv");
						infoRec = LoreGuiItem.setActionData(infoRec, "invName", cat.name+" Recipes");
						
						craftingHelpInventory.addItem(infoRec);
						
						
				Inventory inv = Bukkit.createInventory(player, LoreGuiItem.MAX_INV_SIZE, cat.name+" Recipes");
				for (ConfigMaterial mat : cat.materials.values()) {
					ItemStack stack = getInfoBookForItem(mat.id);
					if (stack != null) {
						stack = LoreGuiItem.setAction(stack, "ShowRecipe");
						inv.addItem(LoreGuiItem.asGuiItem(stack));
					}
				}
				
				/* Add Information Item */
				ItemStack info = LoreGuiItem.build("Information", ItemManager.getId(Material.SHIELD), 0,
						CivColor.White+"This GUI displays all of the material",
						CivColor.White+"categories that a person can view",
						CivColor.White+"for crafting. Clicking a category",
						CivColor.White+"will show you items you can craft."
						);
				craftingHelpInventory.setItem((9*5)-2, info);
				
				/* Add back buttons. */
				ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Categories");
				backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
				backButton = LoreGuiItem.setActionData(backButton, "invType", "showCraftingHelp");
				inv.setItem(LoreGuiItem.MAX_INV_SIZE-1, backButton);
				
				LoreGuiItemListener.guiInventories.put(inv.getName(), inv);
			}
			
			/* Add back buttons. */
			ItemStack backButton = LoreGuiItem.build("Back", ItemManager.getId(Material.MAP), 0, "Back to Categories");
			backButton = LoreGuiItem.setAction(backButton, "OpenInventory");
			backButton = LoreGuiItem.setActionData(backButton, "invType", "showGuiInv");
			backButton = LoreGuiItem.setActionData(backButton, "invName", guiInventory.getName());
			craftingHelpInventory.setItem((9*5)-1, backButton);
			
			LoreGuiItemListener.guiInventories.put(craftingHelpInventory.getName(), craftingHelpInventory);
		}
		player.openInventory(craftingHelpInventory);
	}
	
	// https://bukkit.org/threads/create-your-own-custom-head-texture.424286/
	public static ItemStack getPlayerHead(Player p) throws IOException {
		ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		SkullMeta meta = (SkullMeta) is.getItemMeta();
		
		byte[] encodedData = null;
		Resident res = CivGlobal.getResident(p);
		GameProfile gp = new GameProfile(p.getUniqueId(), p.getName());
		if (res.textureInfo == null) {
			String trimmedUUID = p.getUniqueId().toString().replace("-", "");
			URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/"+trimmedUUID);
			
			String inputLine;
			URLConnection conn = url.openConnection();
			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			inputLine = br.readLine();
			br.close();
			
			String ua1 = ("{\"id\":\""+trimmedUUID+"\",\"name\":\""+p.getName()+"\",\"properties\":[{\"name\":\"textures\",\"value\":\""); String ua2 = ("\"}]}");
			String newLine1 = inputLine.replace(ua1, "").replace(ua2, "");
			String decode1 = new String(DatatypeConverter.parseBase64Binary(newLine1));
////			CivMessage.global(decode1);
			
//			String ub1 = ("{\"timestamp\":"+System.currentTimeMillis()+",\"profileId\":\""+trimmedUUID+"\",\"profileName\":\""+p.getName()+"\",\"textures\":{\"SKIN\":{\"url\":\"");
			String ub2 = ("\"}}}");
//			String newLine2 = decode1.replace(ub1, "").replace(ub2, "");
			int tosub = 103+p.getName().length();
////			CivMessage.global(String.valueOf(tosub));
			String newLine2 = decode1.substring(tosub).replace(ub2, "").replace("\"SKIN\":{\"url\":\"", "").replaceAll("}", ""); // using String ub1 did not work correctly, so we will hope this works always
////			CivMessage.global(newLine2);
			
//			String skinURL = "http://textures.minecraft.net/texture/5e5613acda603e3dfee79e5a361b5b7fa5b1711e0a57df9e013387837fe6a";
//			encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\""+newLine2+"\"}}}").getBytes());
//			res.textureInfo = encodedData;
			
			if (decode1.contains(newLine2)) {
				if (newLine2.length() > 0) {
					if (newLine2.contains("\"CAPE\"")) {
						encodedData = "http://textures.minecraft.net/texture/456eec1c2169c8c60a7ae436abcd2dc5417d56f8adef84f11343dc1188fe138".getBytes();
						CivMessage.sendError(p, "Warning! Cannot get player head for Backpack, cape interference, setting to default.");
						CivLog.warning("Warning! Cannot get player head for Backpack, cape interference, setting to default.");
					} else {
//						String ub2 = ("\"}}}");
//						String newLine2 = decode1.replace(ub1, "").replace(ub2, "");
						encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\""+newLine2+"\"}}}").getBytes());
					}
				} else {
					encodedData = "http://textures.minecraft.net/texture/456eec1c2169c8c60a7ae436abcd2dc5417d56f8adef84f11343dc1188fe138".getBytes();
					CivMessage.sendError(p, "Warning! Cannot get player head for Backpack, texutre was null, setting to default.");
					CivLog.warning("Warning! Cannot get player head for Backpack, texture was null, setting to default.");
				}
			} else {
				encodedData = "http://textures.minecraft.net/texture/456eec1c2169c8c60a7ae436abcd2dc5417d56f8adef84f11343dc1188fe138".getBytes();
				CivMessage.sendError(p, "Warning! Cannot get player head for Backpack, Unknown Reason, setting to default.");
				CivLog.warning("Warning! Cannot get player head for Backpack, Unknown Reason, setting to default.");
			}
		} else {
			encodedData = res.textureInfo;
		}
		
		res.textureInfo = encodedData;
		gp.getProperties().put("textures", new Property("textures", new String(encodedData)));
		Field profileField = null;
		try {
			profileField = meta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(meta, gp);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e1) {
			e1.printStackTrace();
		}
		
		is.setItemMeta(meta);
		return is;
	}
	
	public static void spawnGuiBook(Player player) throws IOException {
		guiInventory = null;
		if (guiInventory == null) {
			guiInventory = Bukkit.getServer().createInventory(player, 9*3, "CivCraft Information");
			//00 01 02 03 04 05 06 07 08
			//09 10 11 12 13 14 15 16 17
			//18 19 20 21 22 23 24 25 26
			
			Resident res = CivGlobal.getResident(player);
			ResidentExperience re = CivGlobal.getResidentE(player);
			if (res != null && re != null) {
				ConfigEXPGenericLevel questlvl = CivSettings.expGenericLevels.get(re.getQuestLevel());
				ConfigEXPGenericLevel mininglvl = CivSettings.expGenericLevels.get(re.getMiningLevel());
				ConfigEXPGenericLevel fishinglvl = CivSettings.expGenericLevels.get(re.getFishingLevel());
				
				String town = "None"; if (res.hasTown()) town = res.getTown().getName();
				String civ = "None"; if (res.hasCiv()) civ = res.getCiv().getName();
				
				ItemStack playerInfo = LoreGuiItem.buildWithStack(CivColor.WhiteBold+player.getName(), getPlayerHead(player),
						CivColor.LightGreen+"Coins: "+CivColor.Yellow+res.getTreasury().getBalance(),
						CivColor.LightGreen+"Town, Civ: "+CivColor.Yellow+town+", "+civ,
						CivColor.LightGreen+"Quest Level: "+CivColor.Yellow+questlvl.level+" ("+re.getQuestEXP()+"/"+questlvl.amount+" XP)",
						CivColor.LightGreen+"Mining Level: "+CivColor.Yellow+mininglvl.level+" ("+re.getMiningEXP()+"/"+mininglvl.amount+" XP)",
						CivColor.LightGreen+"Fishing Level: "+CivColor.Yellow+fishinglvl.level+" ("+re.getFishingEXP()+"/"+fishinglvl.amount+" XP)",
						CivColor.LightGreen+"Farming Level: "+CivColor.Rose+" (InDev)",
						CivColor.LightGreen+"Essence Level: "+CivColor.Rose+" (InDev)",
						CivColor.LightGreen+"Slaughter Level: "+CivColor.Rose+" (InDev)",
						CivColor.LightGray+"« Click for Experience Info »");
				playerInfo = LoreGuiItem.setAction(playerInfo, "OpenInventory");
				playerInfo = LoreGuiItem.setActionData(playerInfo, "invType", "showExperienceHelp");
				guiInventory.setItem(0, playerInfo);
			} else {
				ItemStack playerInfo = LoreGuiItem.build("Player Info", ItemManager.getId(Material.SKULL_ITEM), 3,
						CivColor.RoseItalic+"Error, Resident Invalid?");
				guiInventory.setItem(0, playerInfo);
			}
			
			ItemStack newsInfo = LoreGuiItem.build("CivCraft Daily News", ItemManager.getId(Material.PAPER), 0, CivColor.Gold+"<Click To View>");
			newsInfo = LoreGuiItem.setAction(newsInfo, "NewspaperInventory");
			guiInventory.setItem(1, newsInfo);
			
			ItemStack craftRec = LoreGuiItem.build("Crafting Recipes", ItemManager.getId(Material.WORKBENCH), 0, CivColor.Gold+"<Click To View>");
			craftRec = LoreGuiItem.setAction(craftRec, "OpenInventory");
			craftRec = LoreGuiItem.setActionData(craftRec, "invType", "showCraftingHelp");
			guiInventory.setItem(3, craftRec);
			
			ItemStack gameInfo = LoreGuiItem.build("CivCraft Overview", ItemManager.getId(Material.WRITTEN_BOOK), 0, CivColor.Gold+"<Click To View>");
			gameInfo = LoreGuiItem.setAction(gameInfo, "OpenInventory");
			gameInfo = LoreGuiItem.setActionData(gameInfo, "invType", "showTutorialInventory");
			guiInventory.setItem(8, gameInfo);
			
			ItemStack civDip = LoreGuiItem.build("Diplomatic Relations", ItemManager.getId(Material.NAME_TAG), 0, CivColor.Gold+"<Click to View>");
			civDip = LoreGuiItem.setAction(civDip, "DiplomaticMenu");
			guiInventory.setItem(9, civDip);
			
			ItemStack civDebt = LoreGuiItem.build("Debt Listings", ItemManager.getId(Material.COAL), 0, CivColor.Gold+"<Click to View>");
			civDebt = LoreGuiItem.setAction(civDebt, "DebtMenu");
			guiInventory.setItem(10, civDebt);
			
			ItemStack perkMenu = LoreGuiItem.build("Perk Menu", ItemManager.getId(Material.BOOK_AND_QUILL), 0, CivColor.Gold+"<Click to View>");
//			perkMenu = LoreGuiItem.setAction(perkMenu, "ShowPerkPage");
			guiInventory.setItem(15, perkMenu);
			
			ItemStack buildMenu = LoreGuiItem.build("Building Menu", ItemManager.getId(Material.SLIME_BLOCK), 0, CivColor.Gold+"<Click to View>");
			buildMenu = LoreGuiItem.setAction(buildMenu, "_BuildingInventory");
			guiInventory.setItem(18, buildMenu);
			
			ItemStack townMenu = LoreGuiItem.build("Town Menu", ItemManager.getId(Material.LEATHER_HELMET), 0, CivColor.Gold+"<Click to View>");
			townMenu = LoreGuiItem.setAction(townMenu, "OpenInventory");
			townMenu = LoreGuiItem.setActionData(townMenu, "invType", "showTownMenu");
			guiInventory.setItem(19, townMenu);
			
			ItemStack civMenu = LoreGuiItem.build("Civilization Menu", ItemManager.getId(Material.GOLD_HELMET), 0, CivColor.Red+"<Click to View>", CivColor.LightGray+" « Coming Soon » ");
//			civMenu = LoreGuiItem.setAction(civMenu, "OpenInventory");
//			civMenu = LoreGuiItem.setActionData(civMenu, "invType", "showCivMenu");
			guiInventory.setItem(20, civMenu);
			
			ItemStack turorialMenu = LoreGuiItem.build("In-Game Wiki", ItemManager.getId(Material.RED_ROSE), 1, CivColor.Gold+"<Click to View>", "wiki_tag000_COMING_SOON");
//			turorialMenu = LoreGuiItem.setAction(turorialMenu, "BuildTutorialMenu");
			guiInventory.setItem(26, turorialMenu);
			
			
			LoreGuiItemListener.guiInventories.put(guiInventory.getName(), guiInventory);
			}
		player.openInventory(guiInventory);
	}
}
