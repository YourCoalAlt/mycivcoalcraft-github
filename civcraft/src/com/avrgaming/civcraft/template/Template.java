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
package com.avrgaming.civcraft.template;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuildableInfo;
import com.avrgaming.civcraft.exception.CivException;
import com.avrgaming.civcraft.main.CivData;
import com.avrgaming.civcraft.main.CivGlobal;
import com.avrgaming.civcraft.main.CivLog;
import com.avrgaming.civcraft.object.Resident;
import com.avrgaming.civcraft.object.Town;
import com.avrgaming.civcraft.structure.Buildable;
import com.avrgaming.civcraft.structure.Structure;
import com.avrgaming.civcraft.structure.wonders.Wonder;
import com.avrgaming.civcraft.util.BlockCoord;
import com.avrgaming.civcraft.util.ItemManager;
import com.avrgaming.civcraft.util.PlayerBlockChangeUtil;
import com.avrgaming.civcraft.util.SimpleBlock;

public class Template {
	/* Handles the processing of CivTemplates which store cubiods of blocks for later use. */
	public enum TemplateType {
		STRUCTURE,
		WONDER,
	};
	
	public SimpleBlock [][][] blocks;
	public int size_x;
	public int size_y;
	public int size_z;
	private String dir;
	private String filepath;
	
	/* Save the command block locations when we init the template, so we dont have to search for them later. */
	public ArrayList<BlockCoord> commandBlockRelativeLocations = new ArrayList<BlockCoord>();
	public LinkedList<BlockCoord> doorRelativeLocations = new LinkedList<BlockCoord>();
	public LinkedList<BlockCoord> attachableLocations = new LinkedList<BlockCoord>();
	public static HashSet<Integer> attachableTypes = new HashSet<Integer>();
	
	
	public static HashMap<String, Template> templateCache = new HashMap<String, Template>();
	
	
//	public static HashMap<String, Template> staticTemplates = new HashMap<String, Template>();
//	
	public static void init() throws IOException, CivException {
//		/* Always cache default capitol, camp, and town hall templates. */
//		CivLog.info("============= Loading Static Templates ===========");
//		int count = 0;
//		count += initStaticTemplatesDirection("north");
//		count += initStaticTemplatesDirection("south");
//		count += initStaticTemplatesDirection("east");
//		count += initStaticTemplatesDirection("west");
//		CivLog.info("Loaded "+count+" static templates.");
	}
	
	public static int initStaticTemplatesDirection(String dir) throws IOException, CivException {
		Template tpl;

		int count = 0;
		for (ConfigBuildableInfo info : CivSettings.structures.values()) {
			if (!info.has_template) {
				continue;
			}
			
			tpl = new Template();
			tpl.dir = dir;
			tpl.load_template("templates/themes/default/structures/"+info.template_base_name+"/"+info.template_base_name+"_"+tpl.dir+".def");
			count++;
		}
		return count;
	}
	
	public static void initAttachableTypes() {
		attachableTypes.add(CivData.SAPLING);
		attachableTypes.add(CivData.BED_BLOCK);
		attachableTypes.add(CivData.RAIL_POWERED);
		attachableTypes.add(CivData.RAIL_DETECTOR);
		attachableTypes.add(CivData.TALL_GRASS);
		attachableTypes.add(CivData.DEAD_BUSH);
		attachableTypes.add(CivData.DANDELION);
		attachableTypes.add(CivData.OTHER_FLOWERS);
		attachableTypes.add(CivData.BROWN_MUSHROOM);
		attachableTypes.add(CivData.RED_MUSHROOM);
		attachableTypes.add(CivData.TNT);
		attachableTypes.add(CivData.TORCH);
		attachableTypes.add(CivData.REDSTONE_WIRE);
		attachableTypes.add(CivData.WHEAT_CROP);
		attachableTypes.add(CivData.SIGN);
		attachableTypes.add(CivData.WOOD_DOOR);
		attachableTypes.add(CivData.LADDER);
		attachableTypes.add(CivData.RAIL);
		attachableTypes.add(CivData.WALL_SIGN);
		attachableTypes.add(CivData.LEVER);
		attachableTypes.add(CivData.STONE_PLATE);
		attachableTypes.add(CivData.IRON_DOOR);
		attachableTypes.add(CivData.WOOD_PLATE);
		attachableTypes.add(CivData.REDSTONE_TORCH_OFF);
		attachableTypes.add(CivData.REDSTONE_TORCH_ON);
		attachableTypes.add(CivData.STONE_BUTTON);
		attachableTypes.add(CivData.CACTUS);
		attachableTypes.add(CivData.SUGARCANE_BLOCK);
		attachableTypes.add(CivData.REPEATER_OFF);
		attachableTypes.add(CivData.REPEATER_ON);
		attachableTypes.add(CivData.TRAPDOOR);
		attachableTypes.add(CivData.PUMPKIN_STEM);
		attachableTypes.add(CivData.MELON_STEM);
		attachableTypes.add(CivData.VINE);
		attachableTypes.add(CivData.LILY_PAD);
		attachableTypes.add(CivData.NETHERWART_CROP);
		attachableTypes.add(CivData.COCOA_CROP);
		attachableTypes.add(CivData.TRIPWIRE_HOOK);
		attachableTypes.add(CivData.FLOWER_POT);
		attachableTypes.add(CivData.CARROT_CROP);
		attachableTypes.add(CivData.POTATO_CROP);
		attachableTypes.add(CivData.WOOD_BUTTON);
		attachableTypes.add(CivData.ANVIL);
		attachableTypes.add(CivData.GOLD_PLATE);
		attachableTypes.add(CivData.IRON_PLATE);
		attachableTypes.add(CivData.COMPARATOR_OFF);
		attachableTypes.add(CivData.COMPARATOR_ON);
		attachableTypes.add(CivData.RAIL_ACTIVATOR);
		attachableTypes.add(CivData.CARPET);
		attachableTypes.add(CivData.DOUBLE_FLOWER);
		attachableTypes.add(CivData.SPRUCE_DOOR);
		attachableTypes.add(CivData.BIRCH_DOOR);
		attachableTypes.add(CivData.JUNGLE_DOOR);
		attachableTypes.add(CivData.ACACIA_DOOR);
		attachableTypes.add(CivData.DARK_OAK_DOOR);
		attachableTypes.add(CivData.BEETROOT_CROP);
	}
	
	public static boolean isAttachable(int blockID) {
		if (attachableTypes.contains(blockID)) {
			return true;
		}
		return false;
	}
	
	public static boolean isAttachableMaterial(Material mat) {
		if (attachableTypes.contains(ItemManager.getId(mat))) {
			return true;
		}
		return false;
	}
	
	public Template() {
	}
	
	/*public CivTemplate(Location center, String name, Type type) throws TownyException, IOException {
		initTemplate(center, name, type);
	}*/
	
	public static String getTemplateFilePath(Location playerLocationForDirection, Buildable buildable, String theme) {
		TemplateType type = TemplateType.STRUCTURE;
		
		if (buildable instanceof Structure) {
			type = TemplateType.STRUCTURE;
		} else if (buildable instanceof Wonder) {
			type = TemplateType.WONDER;
		}
		
		String dir = Template.parseDirection(playerLocationForDirection);
		dir = Template.invertDirection(dir);
				
		return Template.getTemplateFilePath(buildable.getTemplateBaseName(), dir, type, theme);
	}
	
	public static String getTemplateFilePath(String template_file, String direction, TemplateType type, String theme) {
		String typeStr = "";
		if (type == TemplateType.STRUCTURE) {
			typeStr = "structures";
		}
		if (type == TemplateType.WONDER) {
			typeStr = "wonders";
		}
		
		template_file = template_file.replaceAll(" ", "_");
		
		if (direction.equals("")) {
			return ("templates/themes/"+theme+"/"+typeStr+"/"+template_file+"/"+template_file+".def").toLowerCase();
		}
		
		return ("templates/themes/"+theme+"/"+typeStr+"/"+template_file+"/"+template_file+"_"+direction+".def").toLowerCase();
	}
	
	public void buildConstructionScaffolding(Location center, Player player) 
	{
		//this.buildScaffolding(center);
		
		Block block = center.getBlock();
		ItemManager.setTypeIdAndData(block, ItemManager.getId(Material.CHEST), 0, false);
	}
	
	public void buildPreviewScaffolding(Location center, Player player) {
		Resident resident = CivGlobal.getResident(player);
		resident.undoPreview();
		
		// Top
		for (int z = 0; z < this.size_z; z++) {
			for (int x = 0; x < this.size_x; x++) {
				Block b = center.getBlock().getRelative(x, this.size_y-1, z);
				ItemManager.sendBlockChange(player, b.getLocation(), CivData.BARRIER, 0);
				resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			}
		}
		
		// Bottom
		for (int z = 0; z < this.size_z; z++) {
			for (int x = 0; x < this.size_x; x++) {
				Block b = center.getBlock().getRelative(x, 0, z);
				ItemManager.sendBlockChange(player, b.getLocation(), CivData.BARRIER, 0);
				resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			}
		}
		
		// Building Parallel to player
		for (int y = 0; y < this.size_y; y++) {
			for (int z = 0; z < this.size_z; z++) {
			Block b1 = center.getBlock().getRelative(0, y, z);
			ItemManager.sendBlockChange(player, b1.getLocation(), CivData.BARRIER, 0);
			resident.previewUndo.put(new BlockCoord(b1.getLocation()), new SimpleBlock(ItemManager.getId(b1), ItemManager.getData(b1)));
			
			Block b2 = center.getBlock().getRelative(this.size_x-1, y, z);
			ItemManager.sendBlockChange(player, b2.getLocation(), CivData.BARRIER, 0);
			resident.previewUndo.put(new BlockCoord(b2.getLocation()), new SimpleBlock(ItemManager.getId(b2), ItemManager.getData(b2)));
			}
		}
		
		// Building Perpendicular to player
		for (int y = 0; y < this.size_y; y++) {
			for (int x = 0; x < this.size_x; x++) {
			Block b1 = center.getBlock().getRelative(x, y, 0);
			ItemManager.sendBlockChange(player, b1.getLocation(), CivData.BARRIER, 0);
			resident.previewUndo.put(new BlockCoord(b1.getLocation()), new SimpleBlock(ItemManager.getId(b1), ItemManager.getData(b1)));
			
			Block b2 = center.getBlock().getRelative(x, y, this.size_z-1);
			ItemManager.sendBlockChange(player, b2.getLocation(), CivData.BARRIER, 0);
			resident.previewUndo.put(new BlockCoord(b2.getLocation()), new SimpleBlock(ItemManager.getId(b2), ItemManager.getData(b2)));
			}
		}
		
		// Corner Pillars
		for (int y = 0; y < this.size_y; y++) {
			Block b = center.getBlock().getRelative(0, y, 0);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
		
			b = center.getBlock().getRelative(this.size_x-1, y, this.size_z-1);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
	
			b = center.getBlock().getRelative(this.size_x-1, y, 0);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));

			b = center.getBlock().getRelative(0, y, this.size_z-1);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
		}
		
		// Top Bars parallel to player
		for (int x = 0; x < this.size_x; x++) {
			Block b = center.getBlock().getRelative(x, this.size_y-1, 0);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			
			b = center.getBlock().getRelative(x, this.size_y-1, this.size_z-1);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));

		}
		
		// Top Bars Perpendicular to player
		for (int z = 0; z < this.size_z; z++) {
			Block b = center.getBlock().getRelative(0, this.size_y-1, z);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			
			b = center.getBlock().getRelative(this.size_x-1, this.size_y-1, z);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
		}
		
		// Bottom Bars parallel to player
		for (int x = 0; x < this.size_x; x++) {
			Block b = center.getBlock().getRelative(x, 0, 0);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			
			b = center.getBlock().getRelative(x, 0, this.size_z-1);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));

		}
		
		// Bottom Bars Perpendicular to player
		for (int z = 0; z < this.size_z; z++) {
			Block b = center.getBlock().getRelative(0, 0, z);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
			
			b = center.getBlock().getRelative(this.size_x-1, 0, z);
			ItemManager.sendBlockChange(player, b.getLocation(), CivData.BEDROCK, 0);
			resident.previewUndo.put(new BlockCoord(b.getLocation()), new SimpleBlock(ItemManager.getId(b), ItemManager.getData(b)));
		}
	}
	
	public void buildScaffolding(Location center) {
		for (int y = 0; y < this.size_y; y++) {
			Block b = center.getBlock().getRelative(0, y, 0);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
		
			b = center.getBlock().getRelative(this.size_x-1, y, this.size_z-1);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
				
			b = center.getBlock().getRelative(this.size_x-1, y, 0);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
						
			b = center.getBlock().getRelative(0, y, this.size_z-1);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
		}
		
		for (int x = 0; x < this.size_x; x++) {
			Block b = center.getBlock().getRelative(x, this.size_y-1, 0);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
			
			b = center.getBlock().getRelative(x, this.size_y-1, this.size_z-1);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
		}
		
		for (int z = 0; z < this.size_z; z++) {
			Block b = center.getBlock().getRelative(0, this.size_y-1, z);
			ItemManager.setTypeId(b, CivData.BEDROCK);		

			b = center.getBlock().getRelative(this.size_x-1, this.size_y-1, z);
			ItemManager.setTypeId(b, CivData.BEDROCK);		
		}
		
		for (int z = 0; z < this.size_z; z++) {
			for (int x = 0; x < this.size_x; x++) {
				Block b = center.getBlock().getRelative(x, 0, z);
				ItemManager.setTypeId(b, CivData.BEDROCK);		
			}
		}
		
	}
	
	public void removeScaffolding(Location center) {
		for (int y = 0; y < this.size_y; y++) {
			Block b = center.getBlock().getRelative(0, y, 0);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);
			}
		
			b = center.getBlock().getRelative(this.size_x-1, y, this.size_z-1);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);
			}
			
			b = center.getBlock().getRelative(this.size_x-1, y, 0);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);

			}
			
			b = center.getBlock().getRelative(0, y, this.size_z-1);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);

			}
		}
		
		for (int x = 0; x < this.size_x; x++) {
			Block b = center.getBlock().getRelative(x, this.size_y-1, 0);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);
			}

			b = center.getBlock().getRelative(x, this.size_y-1, this.size_z-1);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);			
			}

		}
		
		for (int z = 0; z < this.size_z; z++) {
			Block b = center.getBlock().getRelative(0, this.size_y-1, z);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);			
			}

			b = center.getBlock().getRelative(this.size_x-1, this.size_y-1, z);
			if (ItemManager.getId(b) == CivData.BEDROCK) {
				ItemManager.setTypeId(b, CivData.AIR);
				ItemManager.setData(b, 0, true);			
			}

		}
		
	}
	
	public void saveUndoTemplate(String string, String subdir, Location center) throws CivException, IOException {
		
		String filepath = "templates/undo/"+subdir;
		File undo_tpl_file = new File(filepath);
		undo_tpl_file.mkdirs();
		
		FileWriter writer = new FileWriter(undo_tpl_file.getAbsolutePath()+"/"+string);
		
		//TODO Extend this to save paintings?
		writer.write(this.size_x+";"+this.size_y+";"+this.size_z+"\n");
		for (int x = 0; x < this.size_x; x++) {
			for (int y = 0; y < this.size_y; y++) {
				for (int z = 0; z < this.size_z; z++) {
					Block b = center.getBlock().getRelative(x, y, z);
					
					if (ItemManager.getId(b) == CivData.WALL_SIGN || ItemManager.getId(b) == CivData.SIGN) {
						if (b.getState() instanceof Sign) {
							Sign sign = (Sign)b.getState();
							
							String signText = "";
							for (String line : sign.getLines()) {
								signText += line+ ",";
							}
							writer.write(x+":"+y+":"+z+","+ItemManager.getId(b)+":"+ItemManager.getData(b)+","+signText+"\n");
						} 
					} else {
						writer.write(x+":"+y+":"+z+","+ItemManager.getId(b)+":"+ItemManager.getData(b)+"\n");
					}


				}
			}
		}
		writer.close();
		
		for (int x = 0; x < this.size_x; x++) {
			for (int y = 0; y < this.size_y; y++) {
				for (int z = 0; z < this.size_z; z++) {
					// Must set to air in a different loop, since setting to air can break attachables.
					Block b = center.getBlock().getRelative(x, y, z);

					ItemManager.setTypeId(b, CivData.AIR); 
					ItemManager.setData(b, 0x0);
				}
			}
		}		
	}
	
	public void initUndoTemplate(String structureHash, String subdir) throws IOException, CivException {
		String filepath = "templates/undo/"+subdir+"/"+structureHash;

		File templateFile = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		
		// Read first line and get size.
		String line = null;
		line = reader.readLine();
		if (line == null) {
			reader.close();
			throw new CivException("Invalid template file:"+filepath);
		}
		
		String split[] = line.split(";");
		size_x = Integer.valueOf(split[0]); 
		size_y = Integer.valueOf(split[1]);
		size_z = Integer.valueOf(split[2]);
		getTemplateBlocks(reader, size_x, size_y, size_z);
		reader.close();		
	}
	
	/*
	* This function will save a copy of the template currently building
	* into the town's temp directory. It does this so that we:
	* 1) Dont have to remember the template's direction when we resume
	* 2) Can change the master template without messing up any builds in progress
	* 3) So we can pick a random template and "resume" the correct one. (e.g. cottages)
	*/
	public String getTemplateCopy(String masterTemplatePath, String string, Town town) {
		String copyTemplatePath = "templates/inprogress/"+town.getName();
		File inprogress_tpl_file = new File(copyTemplatePath);
		inprogress_tpl_file.mkdirs();
		
		// Copy File...
		File master_tpl_file = new File(masterTemplatePath);
		inprogress_tpl_file = new File(copyTemplatePath+"/"+string);
		
		try {
			Files.copy(master_tpl_file.toPath(), inprogress_tpl_file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.out.println("Failure to copy file!");
			e.printStackTrace();
			return null;
		}
		
		return copyTemplatePath+"/"+string;
	}
	
	public void setDirection(Location center) throws CivException {
		// Get the direction we are facing.
		dir = parseDirection(center);
		dir = invertDirection(dir); //We want the direction the building should be facing, not the player.

		if (dir == null) {
			throw new CivException("Unknown direction.");
		}
	}
	
	public static String getDirection(Location center) {
		String dir = parseDirection(center);
		dir = invertDirection(dir);
		return dir;
	}
	
	public void resumeTemplate(String templatePath, Buildable buildable) throws IOException, CivException {
		this.setFilepath(templatePath);
		load_template(templatePath);
		buildable.setTotalBlockCount(size_x*size_y*size_z);
	}
	
	public void initTemplate(Location center, ConfigBuildableInfo info, String theme) throws CivException, IOException {
		this.setDirection(center);
		String templatePath = Template.getTemplateFilePath(info.template_base_name, dir, TemplateType.STRUCTURE, theme);
		this.setFilepath(templatePath);
		load_template(templatePath);
	}
	
	public void initTemplate(Location center, Buildable buildable, String theme) throws IOException, CivException {
		
		this.setDirection(center);
		
		if (!buildable.hasTemplate()) {
			/*
			* Certain structures are built procedurally such as walls and roads.
			* They do not have a direction and do not have a template.
			*/
			dir = "";
		}
		
		
		// Find the template file.
		String templatePath = Template.getTemplateFilePath(center, buildable, theme);
		this.setFilepath(templatePath);
		load_template(templatePath);
		buildable.setTotalBlockCount(size_x*size_y*size_z);
	}
	
	public void initTemplate(Location center, Buildable buildable) throws CivException, IOException {
		initTemplate(center, buildable, "default");
	}
	
	public static Template getTemplate(String filepath, Location dirLoc) throws IOException, CivException {
		/* Attempt to get template statically. */
		Template tpl = templateCache.get(filepath);
		if (tpl == null) {
			/* No template found in cache. Load it. */
			tpl = new Template();
			tpl.load_template(filepath);
		}
		
		if (dirLoc != null) {
			tpl.setDirection(dirLoc);
		}
		return tpl;
	}
	
	public void load_template(String filepath) throws IOException, CivException {
		File templateFile = new File(filepath);
		BufferedReader reader = new BufferedReader(new FileReader(templateFile));
		
		// Read first line and get size.
		String line = null;
		line = reader.readLine();
		if (line == null) {
			reader.close();
			throw new CivException("Invalid template file:"+filepath);
		}
		
		String split[] = line.split(";");
		size_x = Integer.valueOf(split[0]); 
		size_y = Integer.valueOf(split[1]);
		size_z = Integer.valueOf(split[2]);
		getTemplateBlocks(reader, size_x, size_y, size_z);
		this.filepath = filepath;
		reader.close();
	}
		
	private void getTemplateBlocks(BufferedReader reader, int regionX, int regionY, int regionZ) throws NumberFormatException, IOException {
		
		String line;
		SimpleBlock blocks[][][] = new SimpleBlock[regionX][regionY][regionZ];
		
		// Read blocks from file.
		while ((line=reader.readLine()) != null) {
			String locTypeSplit[] = line.split(",");
			String location = locTypeSplit[0];
			String type = locTypeSplit[1];
			
			//Parse location
			String locationSplit[] = location.split(":");
			int blockX, blockY, blockZ;
			blockX = Integer.valueOf(locationSplit[0]);
			blockY = Integer.valueOf(locationSplit[1]);
			blockZ = Integer.valueOf(locationSplit[2]);
			
			// Parse type
			String typeSplit[] = type.split(":");
			int blockId, blockData;
			blockId = Integer.valueOf(typeSplit[0]);
			blockData = Integer.valueOf(typeSplit[1]);
			
			SimpleBlock block = new SimpleBlock(blockId, blockData);
			
			if (blockId == CivData.WOOD_DOOR || blockId == CivData.IRON_DOOR) {
				this.doorRelativeLocations.add(new BlockCoord("", blockX, blockY, blockZ));
			}
			
			// look for signs.
			if (blockId == CivData.WALL_SIGN || blockId == CivData.SIGN) {
				if (locTypeSplit.length > 2) {
					
					// The first character on special signs needs to be a /.
					if (locTypeSplit[2] != null && !locTypeSplit[2].equals("") && locTypeSplit[2].charAt(0) == '/') {
						block.specialType = SimpleBlock.Type.COMMAND;
						
						// Got a command, save it.
						block.command = locTypeSplit[2];
						
						// Save any key values we find.
						if (locTypeSplit.length > 3) {
							for (int i = 3; i < locTypeSplit.length; i++) {
								if (locTypeSplit[i] == null || locTypeSplit[i].equals("")) {
									continue;
								}
								
								String[] keyvalue = locTypeSplit[i].split(":");
								if (keyvalue.length < 2) {
									CivLog.warning("Invalid keyvalue:"+locTypeSplit[i]+" in template:"+this.filepath);
									continue;
								}
								block.keyvalues.put(keyvalue[0].trim(), keyvalue[1].trim());
							}
						}
						
						/* This block coord does not point to a location in a world, just a template. */
						this.commandBlockRelativeLocations.add(new BlockCoord("", blockX, blockY, blockZ));
						
					} else {
						block.specialType = SimpleBlock.Type.LITERAL;

						// Literal sign, copy the sign into the simple block
						try {
							block.message[0] = locTypeSplit[2];
						} catch (ArrayIndexOutOfBoundsException e) {
							block.message[0] = "";
						}
						try {
							block.message[1] = locTypeSplit[3];
						} catch (ArrayIndexOutOfBoundsException e) {
							block.message[1] = "";
						}
						
						try {
							block.message[2] = locTypeSplit[4];

						} catch (ArrayIndexOutOfBoundsException e) {
							block.message[2] = "";
						}
						
						try {
							block.message[3] = locTypeSplit[5];
						} catch (ArrayIndexOutOfBoundsException e) {
							block.message[3] = "";
						}
					}
				}
			}
			
			if (isAttachable(blockId)) {
				this.attachableLocations.add(new BlockCoord("", blockX, blockY, blockZ));
			}
			
			blocks[blockX][blockY][blockZ] = block;
		}
		
		this.blocks = blocks;
		return;
	}
	
	public static String parseDirection(Location loc){
		double rotation = (loc.getYaw() - 90) % 360;
		if (rotation < 0) rotation += 360.0;
		if (0 <= rotation && rotation < 22.5) return "east"; //S > E
		else if (22.5 <= rotation && rotation < 67.5) return "east"; //SW > SE
		else if (67.5 <= rotation && rotation < 112.5) return "south"; //W > E
		else if (112.5 <= rotation && rotation < 157.5) return "west"; //NW > SW
		else if (157.5 <= rotation && rotation < 202.5) return "west"; //N > W
		else if (202.5 <= rotation && rotation < 247.5) return "west"; //NE > NW
		else if (247.5 <= rotation && rotation < 292.5) return "north"; //E > N
		else if (292.5 <= rotation && rotation < 337.5) return "east"; //SE > NE
		else if (337.5 <= rotation && rotation < 360.0) return "east"; //S > E
		else return null;
	 }
	
	public static Integer faceVillager(int d) {
		if (d == 15 || d == 0 || d == 1) return 0;
		if (d == 2) return 45;
		if (d == 3 || d == 4 || d == 5) return 90;
		if (d == 6) return 135;
		if (d == 7 || d == 8 || d == 9) return 180;
		if (d == 10) return -135;
		if (d == 11 || d == 12 || d == 13) return -90;
		if (d == 14) return -45;
		return 0;
	}
	
	public static String invertDirection(String dir) {
		if (dir.equalsIgnoreCase("east")) return "west";
		if (dir.equalsIgnoreCase("west")) return "east";
		if (dir.equalsIgnoreCase("north")) return "south";
		if (dir.equalsIgnoreCase("south")) return "north";
		return null;
	}

	public void deleteUndoTemplate(String string, String subdir) throws CivException, IOException {
		String filepath = "templates/undo/"+subdir+"/"+string;
		File templateFile = new File(filepath);
		templateFile.delete();
	}

	public void deleteInProgessTemplate(String string, Town town) {
		String filepath = "templates/inprogress/"+town.getName()+"/"+string;
		File templateFile = new File(filepath);
		templateFile.delete();		
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	
	public void previewEntireTemplate(Template tpl, Block cornerBlock, Player player) {
		//HashMap<Chunk, Chunk> chunkUpdates = new HashMap<Chunk, Chunk>();
	//	NMSHandler nms = new NMSHandler();
		PlayerBlockChangeUtil util = new PlayerBlockChangeUtil();
		for (int x = 0; x < tpl.size_x; x++) {
			for (int y = 0; y < tpl.size_y; y++) {
				for (int z = 0; z < tpl.size_z; z++) {
					Block b = cornerBlock.getRelative(x, y, z);
						//b.setTypeIdAndData(tpl.blocks[x][y][z].getType(), (byte)tpl.blocks[x][y][z].getData(), false);
						try {
							util.addUpdateBlock("", new BlockCoord(b), tpl.blocks[x][y][z].getType(), tpl.blocks[x][y][z].getData());
							
//							nms.setBlockFast(b.getWorld(), b.getX(), b.getY(), b.getZ(), tpl.blocks[x][y][z].getType(), 
//								(byte)tpl.blocks[x][y][z].getData());
						} catch (Exception e) {
							e.printStackTrace();
							//throw new CivException("Couldn't build undo template unknown error:"+e.getMessage());
						}
				}
			}
		}
		
		util.sendUpdate(player.getName());
	}
	
	public void buildUndoTemplate(Template tpl, Block centerBlock) {
		HashMap<Chunk, Chunk> chunkUpdates = new HashMap<Chunk, Chunk>();
		
		for (int x = 0; x < tpl.size_x; x++) {
			for (int y = 0; y < tpl.size_y; y++) {
				for (int z = 0; z < tpl.size_z; z++) {
					Block b = centerBlock.getRelative(x, y, z);
					
					SimpleBlock sb = tpl.blocks[x][y][z];
					if (CivSettings.restrictedUndoBlocks.contains(sb.getMaterial())) {
						continue;
					}
					
						ItemManager.setTypeIdAndData(b, tpl.blocks[x][y][z].getType(), (byte)tpl.blocks[x][y][z].getData(), false);
//						try {
//							nms.setBlockFast(b.getWorld(), b.getX(), b.getY(), b.getZ(), tpl.blocks[x][y][z].getType(), 
//								(byte)tpl.blocks[x][y][z].getData());
//						} catch (Exception e) {
//							e.printStackTrace();
//							//throw new CivException("Couldn't build undo template unknown error:"+e.getMessage());
//						}
						
						chunkUpdates.put(b.getChunk(), b.getChunk());
						
						if (ItemManager.getId(b) == CivData.WALL_SIGN || ItemManager.getId(b) == CivData.SIGN) {
							Sign s2 = (Sign)b.getState();
							s2.setLine(0, tpl.blocks[x][y][z].message[0]);
							s2.setLine(1, tpl.blocks[x][y][z].message[1]);
							s2.setLine(2, tpl.blocks[x][y][z].message[2]);
							s2.setLine(3, tpl.blocks[x][y][z].message[3]);
							s2.update();
						}
				}
			}
		}
	}
	
	public String dir() {
		return dir;
	}
	
}
