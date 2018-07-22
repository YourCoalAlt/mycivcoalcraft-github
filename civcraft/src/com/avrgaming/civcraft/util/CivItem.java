package com.avrgaming.civcraft.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivMessage;
import com.avrgaming.civcraft.object.Resident;

/* The ItemManager class is going to be used to wrap itemstack operations that have now
 * been deprecated by Bukkit. If bukkit ever actually takes these methods away from us,
 * we'll just have to use NMS or be a little creative. Doing it on spot (here) will be 
 * better than having fragile code scattered everywhere. 
 * 
 * Additionally it gives us an opportunity to unit test certain item operations that we
 * want to use with our new custom item stacks. */

public class CivItem {
	
	// New
	
	public static ItemStack newStack(Material type, int amount, short data) {
		return new ItemStack(type, amount, data);
	}
	
	public static ItemStack newStack(Material type, int amount, int data) {
		return new ItemStack(type, amount, (short)data);
	}
	
	public static ItemStack newStack(Material type, int data, boolean tag) {
		if (tag) return newStack(type, 1, data);
		else return newStack(type, data, 0);
	}
	
	public static ItemStack newStack(Material type, int amount) {
		return newStack(type, amount, 0);
	}
	
	public static ItemStack newStack(Material type) {
		return newStack(type, 1, 0);
	}
	
	public static ItemStack airStack() {
		return newStack(Material.AIR, 1, 0);
	}
	
	public static Enchantment getEnchantByName(String name) {
		return Enchantment.getByName(name);
	}
	
	public static String getEnchantName(Enchantment ench) {
		return ench.getName();
	}
	
	// Temp for this version
	
	@SuppressWarnings("deprecation")
	public static ItemStack newStack(int id, int amount, int data) {
		return new ItemStack(id, amount, (short)data);
	}
	
	public static ItemStack newStack(int id, int data) {
		return newStack(id, 1, data);
	}
	
	public static ItemStack newStack(int id, int data, boolean tag) {
		if (tag) return newStack(id, 1, data);
		else return newStack(id, data, 0);
	}
	
	public static ItemStack newStack(int id) {
		return newStack(id, 1, 0);
	}
	
	// Old
	
	@SuppressWarnings("deprecation")
	public static MaterialData getMaterialData(int type_id, int data) {
		return new MaterialData(type_id, (byte)data);
	}
	
	@SuppressWarnings("deprecation")
	public static int getId(Material material) {
		return material.getId();
	}
	
	@SuppressWarnings("deprecation")
	public static int getId(ItemStack stack) {
		return stack.getTypeId();
	}
	
	@SuppressWarnings("deprecation")
	public static int getId(Block block) {
		return block.getTypeId();
	}
	
	public static int getId(BlockSnapshot nextBlock) {
		return nextBlock.getTypeId();
	}
	
	@SuppressWarnings("deprecation")
	public static void setTypeId(Block block, int typeId) {
		block.setTypeId(typeId);
	}
	
	@SuppressWarnings("deprecation")
	public static void setTypeId(BlockState block, int typeId) {
		block.setTypeId(typeId);
	}
	
	public static void setTypeId(BlockSnapshot block, int typeId) {
		block.setTypeId(typeId);
	}
	
	@SuppressWarnings("deprecation")
	public static byte getData(Block block) {
		return block.getData();
	}
	
	public static short getData(ItemStack stack) {
		return stack.getDurability();
	}
	
	@SuppressWarnings("deprecation")
	public static byte getData(MaterialData data) {
		return data.getData();
	}

	@SuppressWarnings("deprecation")
	public static byte getData(BlockState state) {
		return state.getRawData();
	}
	
	@SuppressWarnings("deprecation")
	public static void setData(Block block, int data) {
		block.setData((byte)data);
	}

	@SuppressWarnings("deprecation")
	public static void setData(Block block, int data, boolean update) {
		block.setData((byte) data, update);
	}
	
	@SuppressWarnings("deprecation")
	public static Material getMaterial(int material) {
		return Material.getMaterial(material);
	}
	
	public static Material getMaterial(String material) {
		return Material.getMaterial(material);
	}
	
	@SuppressWarnings("deprecation")
	public static int getBlockTypeId(ChunkSnapshot snapshot, int x, int y, int z) {
		return snapshot.getBlockTypeId(x, y, z);
	}
	
	@SuppressWarnings("deprecation")
	public static int getBlockData(ChunkSnapshot snapshot, int x, int y, int z) {
		return snapshot.getBlockData(x, y, z);
	}
	
	@SuppressWarnings("deprecation")
	public static void sendBlockChange(Player player, Location loc, int type, int data) {
		player.sendBlockChange(loc, type, (byte)data);
	}
	
	@SuppressWarnings("deprecation")
	public static int getBlockTypeIdAt(World world, int x, int y, int z) {
		return world.getBlockTypeIdAt(x, y, z);
	}

	@SuppressWarnings("deprecation")
	public static int getId(BlockState newState) {
		return newState.getTypeId();
	}

	@SuppressWarnings("deprecation")
	public static short getId(EntityType entity) {
		return entity.getTypeId();
	}

	@SuppressWarnings("deprecation")
	public static void setData(MaterialData data, byte chestData) {
		data.setData(chestData);
	}

	@SuppressWarnings("deprecation")
	public static void setTypeIdAndData(Block block, int type, int data, boolean update) {
		block.setTypeIdAndData(type, (byte)data, update);
	}
	
	public static ItemStack spawnPlayerHead(Player p, String itemDisplayName) {		
		ItemStack skull = CivItem.newStack(Material.SKULL_ITEM, 1, 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		meta.setOwningPlayer(p); // TODO Test if this fix from 1.11 to 1.12 works
		meta.setDisplayName(itemDisplayName);
		skull.setItemMeta(meta);
		return skull;
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack spawnPlayerHead(String name) {		
		ItemStack skull = CivItem.newStack(Material.SKULL_ITEM, 1, 3);
		SkullMeta meta = (SkullMeta) skull.getItemMeta();
		OfflinePlayer op = Bukkit.getOfflinePlayer(name);
		meta.setOwningPlayer(op); // TODO Test if this fix from 1.11 to 1.12 works
		skull.setItemMeta(meta);
		return skull;
	}

	public static boolean removeItemFromPlayer(Player player, Material mat, int amount) {
		ItemStack m = new ItemStack(mat, amount);
		if (player.getInventory().contains(mat)) {
			player.getInventory().removeItem(m);
			player.updateInventory();
			return true;
		}
		return false;
	}
	
	// TODO arraylist?
	public static void givePlayerItem(Player p, ItemStack stack, Location dropLoc, String name, int amt, boolean msg) {
		String action = "picked up";
		stack.setAmount(1);
		for (int i = 0; i < amt; i++) {
			if (p.getInventory().firstEmpty() == -1) {
				action = "dropped";
				p.getWorld().dropItem(dropLoc, stack);
			} else {
				p.getInventory().addItem(stack);
			}
		}
		
		if (msg && amt > 0) {
			Resident res = CivGlobal.getResident(p);
			
			// Item Pickup Messages
			if (res.getItemMode().equals("none")) return;
			if (res.getItemMode().equals("all")) {
				if (name == null) {
					name = CivData.getStackName(stack);
					CivMessage.send(p, CivColor.LightGreen+"You've "+action+" "+CivColor.LightPurple+amt+" "+name);
				} else {
					CivMessage.send(p, CivColor.LightGreen+"You've "+action+" up "+CivColor.LightPurple+amt+" "+name);
				}
			} else if (name != null && res.getItemMode().equals("rare")) {
				CivMessage.send(p, CivColor.LightGreen+"You've "+action+" up "+CivColor.LightPurple+amt+" "+name);
			}
//			CivMessage.send(p, CivColor.LightGreen+"You've "+full+" "+CivColor.LightPurple+amt+" "+name);
		}
		return;
	}
	
	public static boolean dropPlayerEXP(Player p, Location dropLoc, int amt) {
		((ExperienceOrb)p.getWorld().spawn(p.getLocation(), ExperienceOrb.class)).setExperience(amt);
		return true;
	}
	
	private static JSONParser jsonParser = new JSONParser();
	
	public static Map<String, String> getPlayerPreviousNames(OfflinePlayer p) {
		Map<String, String> prevNames = new HashMap<String, String>();
		try {
			String trimmedUUID = p.getUniqueId().toString().replaceAll("-", "").replaceAll("_", "");
			URL url = new URL("https://api.mojang.com/user/profiles/"+trimmedUUID+"/names");
			
			String nameJson;
			URLConnection conn = url.openConnection(); // open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			nameJson = br.readLine();
			br.close();
			
			JSONArray parsedData = (JSONArray) jsonParser.parse(nameJson); // Use the JSON parser to parse the information into an Object.
			for (int i = 0; i < parsedData.size(); i++) {
				String s = parsedData.get(i).toString().replace("{\"name\":\"", "").replace("\",\"changedToAt\"", "").replace("\"}", "").replace("}", "");
				int date_sub = s.indexOf(":")+1;
				if (date_sub == -1 || !s.contains(":")) {
					prevNames.put("Original Name", s);
				} else {
					String date = s.substring(date_sub);
					SimpleDateFormat sdf = new SimpleDateFormat("E MMM dd yyyy H:mm z");
					prevNames.put(sdf.format(new Date(Long.valueOf(date))), s.replace(date, "").replaceAll(":", ""));
				}
			}
		} catch (IOException | ParseException e) {
			prevNames.put("Mojang's API Server is down!", "Cannot get results!");
		}
		return prevNames;
	}
	
}
