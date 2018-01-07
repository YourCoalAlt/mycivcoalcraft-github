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
package com.avrgaming.civcraft.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.avrgaming.civcraft.exception.InvalidBlockLocation;
import com.avrgaming.civcraft.util.BlockSnapshot;
import com.avrgaming.civcraft.util.ItemManager;

public class CivData {
	
	//Global Data Bytes
	public static final int DATA_0 = 0;
	public static final int DATA_1 = 1;
	public static final int DATA_2 = 2;
	public static final int DATA_3 = 3;
	public static final int DATA_4 = 4;
	public static final int DATA_5 = 5;
	public static final int DATA_6 = 6;
	public static final int DATA_7 = 7;
	public static final int DATA_8 = 8;
	public static final int DATA_9 = 9;
	public static final int DATA_10 = 10;
	public static final int DATA_11 = 11;
	public static final int DATA_12 = 12;
	public static final int DATA_13 = 13;
	public static final int DATA_14 = 14;
	public static final int DATA_15 = 15;
	
	//Global Blocks
	public static final int AIR = 0;
	public static final int STONE = 1;
		public static final int GRANITE = 1;
		public static final int POLISHED_GRANITE = 2;
		public static final int DIORITE = 3;
		public static final int POLISHED_DIORITE = 4;
		public static final int ANDESITE = 5;
		public static final int POLISHED_ANDESITE = 6;
	public static final int GRASS = 2;
	public static final int DIRT = 3;
		public static final int COARSE_DIRT = 1;
		public static final int PODZOL = 2;
	public static final int COBBLESTONE = 4;
	public static final int PLANK = 5;
	public static final int SAPLING = 6;
	public static final int BEDROCK = 7;
	public static final int WATER_RUNNING = 8;
	public static final int WATER_STILL = 9;
	public static final int LAVA_RUNNING = 10;
	public static final int LAVA_STILL = 11;
	public static final int SAND = 12;
		public static final int RED_SAND = 1;
	public static final int GRAVEL = 13;
	public static final int GOLD_ORE = 14;
	public static final int IRON_ORE = 15;
	public static final int COAL_ORE = 16;
	public static final int LOG = 17;
	public static final int LEAF = 18;
	public static final int SPONGE = 19;
	public static final int GLASS = 20;
	public static final int LAPIS_ORE = 21;
	public static final int LAPIS_BLOCK = 22;
	public static final int DISPENSER = 23;
	public static final int SANDSTONE = 24;
	public static final int NOTEBLOCK = 25;
	public static final int BED_BLOCK = 26;
	public static final int RAIL_POWERED = 27;
	public static final int RAIL_DETECTOR = 28;
	public static final int STICKY_PISTON = 29;
	public static final int COBWEB = 30;
	public static final int TALL_GRASS = 31;
	public static final int DEAD_BUSH = 32;
	public static final int PISTON = 33;
	public static final int WOOL = 35;
	public static final int DANDELION = 37;
	public static final int OTHER_FLOWERS = 38;
	public static final int BROWN_MUSHROOM = 39;
	public static final int RED_MUSHROOM = 40;
	public static final int GOLD_BLOCK = 41;
	public static final int IRON_BLOCK = 42;
	
	public static final int SLABS = 44;
	public static final int BRICK = 45;
	public static final int TNT = 46;
	public static final int BOOKSHELF = 47;
	public static final int MOSS_STONE = 48;
	public static final int OBSIDIAN = 49;
	public static final int TORCH = 50;
	public static final int FIRE = 51;
	public static final int SPAWNER = 52;
	public static final int OAK_STAIRS = 53;
	public static final int CHEST = 54;
	public static final int REDSTONE_WIRE = 55;
	public static final int DIAMOND_ORE = 56;
	public static final int DIAMOND_BLOCK = 57;
	public static final int WORKBENCH = 58;
	public static final int WHEAT_CROP = 59;
	public static final int FARMLAND = 60;
	public static final int FURNACE = 61;
	
	public static final int SIGN = 63;
	public static final int WOOD_DOOR = 64;
	public static final int LADDER = 65;
	public static final int RAIL = 66;
	public static final int COBBLESTONE_STAIRS = 67;
	public static final int WALL_SIGN = 68;
	public static final int LEVER = 69;
	public static final int STONE_PLATE = 70;
	public static final int IRON_DOOR = 71;
	public static final int WOOD_PLATE = 72;
	public static final int REDSTONE_ORE = 73;
	public static final int REDSTONE_ORE_GLOW = 74;
	public static final int REDSTONE_TORCH_OFF = 75;
	public static final int REDSTONE_TORCH_ON = 76;
	public static final int STONE_BUTTON = 77;
	
	public static final int ICE = 79;
	public static final int SNOW_BLOCK = 80;
	public static final int CACTUS = 81;
	public static final int CLAY_BLOCK = 82;
	public static final int SUGARCANE_BLOCK = 83;
	public static final int FENCE = 85;
	public static final int PUMPKIN = 86;
	public static final int NETHERRACK = 87;
	public static final int GLOWSTONE = 89;
	
	public static final int REDSTONE_REPEATER_OFF = 93;
	public static final int REDSTONE_REPEATER_ON = 94;
	public static final int STAINED_GLASS = 95;
	public static final int TRAPDOOR = 96;
	public static final int STONE_BRICK = 98;
	
	public static final int GLASS_PANE = 102;
	public static final int MELON = 103;
	public static final int PUMPKIN_STEM = 104;
	public static final int MELON_STEM = 105;
	public static final int VINE = 106;
	
	public static final int LILY_PAD = 111;
	public static final int NETHER_BRICK = 112;
	public static final int NETHER_FENCE = 113;
	public static final int NETHERWART_CROP = 115;
	public static final int ENCHANTMENT_TABLE = 116;
	
	public static final int END_PORTAL_FRAME = 120;
	
	public static final int REDSTONE_LAMP = 123;
	
	public static final int COCOA_CROP = 127;
	public static final int EMERALD_ORE = 129;
	public static final int ENDER_CHEST = 130;
	public static final int TRIPWIRE_HOOK = 131;
	public static final int TRIPWIRE = 132;
	public static final int EMERALD_BLOCK = 133;
	
	public static final int COMMAND_BLOCK = 137;
	public static final int BEACON = 138;
	public static final int COBBLESTONE_WALL = 139;
	public static final int FLOWER_POT = 140;
	public static final int CARROT_CROP = 141;
	public static final int POTATO_CROP = 142;
	public static final int WOOD_BUTTON = 143;
	
	public static final int ANVIL = 145;
	public static final int TRAPPED_CHEST = 146;
	public static final int GOLD_PLATE = 147;
	public static final int IRON_PLATE = 148;
	public static final int COMPARATOR_OFF = 149;
	public static final int COMPARATOR_ON = 150;
	
	public static final int REDSTONE_BLOCK = 152;
	public static final int QUARTZ_ORE = 153;
	public static final int HOPPER = 154;
	public static final int QUARTZ_BLOCK = 155;
	public static final int RAIL_ACTIVATOR = 157;
	public static final int DROPPER = 158;
	public static final int STAINED_CLAY = 159;
	public static final int STAINED_GLASS_PANE = 160;
	public static final int LEAF2 = 161;
	public static final int LOG2 = 162;
	
	public static final int SLIME_BLOCK = 165;
	public static final int BARRIER = 166;
	public static final int IRON_TRAPDOOR = 167;
	public static final int PRISMARINE = 168;
	public static final int SEA_LANTERN = 169;
	public static final int HAY_BALE = 170;
	public static final int CARPET = 171;
	public static final int HARDENED_CLAY = 172;
	public static final int COAL_BLOCK = 173;
	public static final int PACKED_ICE = 174;
	public static final int DOUBLE_FLOWER = 175;
	
	public static final int SPRUCE_GATE = 183;
	public static final int BIRCH_GATE = 184;
	public static final int JUNGLE_GATE = 185;
	public static final int ACACIA_GATE = 186;
	public static final int DARK_OAK_GATE = 187;
	public static final int SPRUCE_FENCE = 188;
	public static final int BIRCH_FENCE = 189;
	public static final int JUNGLE_FENCE = 190;
	public static final int ACACIA_FENCE = 191;
	public static final int DARK_OAK_FENCE = 192;
	public static final int SPRUCE_DOOR = 193;
	public static final int BIRCH_DOOR = 194;
	public static final int JUNGLE_DOOR = 195;
	public static final int ACACIA_DOOR = 196;
	public static final int DARK_OAK_DOOR = 197;
	public static final int END_ROD = 198;
	public static final int CHORUS_PLANT = 199;
	public static final int CHORUS_FLOWER = 200;
	
	public static final int BEETROOT_CROP = 207;
	
	public static final int MAGMA_BLOCK = 213;
	public static final int NETHERWART_BLOCK = 214;
	public static final int RED_NETHER_BRICK = 215;
	
	//Global Items
	public static final int IRON_SHOVEL = 256;
	public static final int IRON_PICKAXE = 257;
	public static final int IRON_AXE = 258;
	public static final int FLINT_AND_STEEL = 259;
	public static final int APPLE = 260;
	public static final int BOW = 261;
	public static final int ARROW = 262;
	public static final int COAL = 263;
	public static final int DIAMOND = 264;
	public static final int IRON_INGOT = 265;
	public static final int GOLD_INGOT = 266;
	public static final int IRON_SWORD = 267;
	public static final int WOOD_SWORD = 268;
	public static final int WOOD_SHOVEL = 269;
	public static final int WOOD_PICKAXE = 270;
	public static final int WOOD_AXE = 271;
	public static final int STONE_SWORD = 272;
	public static final int STONE_SHOVEL = 273;
	public static final int STONE_PICKAXE = 274;
	public static final int STONE_AXE = 275;
	public static final int DIAMOND_SWORD = 276;
	public static final int DIAMOND_SHOVEL = 277;
	public static final int DIAMOND_PICKAXE = 278;
	public static final int DIAMOND_AXE = 279;
	public static final int STICK = 280;
	public static final int BOWL = 281;
	
	public static final int GOLD_SWORD = 283;
	public static final int GOLD_SHOVEL = 284;
	public static final int GOLD_PICKAXE = 285;
	public static final int GOLD_AXE = 286;
	public static final int STRING = 287;
	public static final int FEATHER = 288;
	public static final int GUNPOWDER = 289;
	public static final int WOOD_HOE = 290;
	public static final int STONE_HOE = 291;
	public static final int IRON_HOE = 292;
	public static final int DIAMOND_HOE = 293;
	public static final int GOLD_HOE = 294;
	public static final int WHEAT_SEED = 295;
	public static final int WHEAT_ITEM = 296;
	public static final int BREAD = 297;
	public static final int LEATHER_HELMET = 298;
	public static final int LEATHER_CHESTPLATE = 299;
	public static final int LEATHER_LEGGINGS = 300;
	public static final int LEATHER_BOOTS = 301;
	public static final int CHAIN_HELMET = 302;
	public static final int CHAIN_CHESTPLATE = 303;
	public static final int CHAIN_LEGGINGS = 304;
	public static final int CHAIN_BOOTS = 305;
	public static final int IRON_HELMET = 306;
	public static final int IRON_CHESTPLATE = 307;
	public static final int IRON_LEGGINGS = 308;
	public static final int IRON_BOOTS = 309;
	public static final int DIAMOND_HELMET = 310;
	public static final int DIAMOND_CHESTPLATE = 311;
	public static final int DIAMOND_LEGGINGS = 312;
	public static final int DIAMOND_BOOTS = 313;
	public static final int GOLD_HELMET = 314;
	public static final int GOLD_CHESTPLATE = 315;
	public static final int GOLD_LEGGINGS = 316;
	public static final int GOLD_BOOTS = 317;
	
	public static final int GOLDEN_APPLE = 322;
	public static final int EMPTY_BUCKET = 325;
	public static final int WATER_BUCKET = 326;
	public static final int REDSTONE_DUST = 331;
	
	public static final int LEATHER = 334;
	
	public static final int CLAY = 337;
	public static final int SUGARCANE = 338;
	public static final int PAPER = 339;
	
	public static final int FISHING_ROD = 346;
	public static final int RAW_FISH = 349;
	public static final int COOKED_FISH = 350;
	public static final int DYE = 351;
	
	public static final int FILLED_MAP = 358;
	
	public static final int ROTTEN_FLESH = 367;
	public static final int ENDER_PEARL = 368;
	
	public static final int GOLD_NUGGET = 371;
	public static final int NETHERWART_ITEM = 372;
	
	public static final int EMPTY_BOTTLE = 374;
	
	public static final int SPAWN_EGG = 383;
	
	public static final int BOOK_AND_QUILL = 386;
	public static final int PUBLISHED_BOOK = 387;
	public static final int EMERALD = 388;
	public static final int ITEMFRAME = 389;
	public static final int CARROT_ITEM = 391;
	public static final int POTATO_ITEM = 392;
	
	public static final int EMPTY_MAP = 395;
	
	public static final int SKULL = 397;
	
	public static final int QUARTZ = 406;
	
	public static final int POPPED_CHORUS_FRUIT = 433;
	public static final int BEETROOT_ITEM = 434;
	public static final int BEETROOT_SEED = 435;
	
	public static final int DRAGON_BREATH = 437;
	
	public static final int ARROW_SPECTRAL = 439;
	
	public static final int SHIELD = 442;
	public static final int ELYTRA = 443;
	
	public static final int TOTEM_OF_UNDYING = 449;
	public static final int SHULKER_SHELL = 450;
	
	public static final int IRON_NUGGET = 452;
	
	
	//Gloal Other Stuff
	public static final String BOOK_UNDERLINE = "�n";
 	public static final String BOOK_BOLD = "�l";
 	public static final String BOOK_ITALIC = "�o";
 	public static final String BOOK_NORMAL = "�r";
	
	public static final byte DATA_SIGN_EAST = 0x5;
	public static final int DATA_SIGN_WEST = 0x4;
	public static final int DATA_SIGN_NORTH = 0x2;
	public static final int DATA_SIGN_SOUTH = 0x3;
	
	public static final byte CHEST_NORTH = 0x2;
	public static final byte CHEST_SOUTH = 0x3;
	public static final byte CHEST_WEST = 0x4;
	public static final byte CHEST_EAST = 0x5;
	
	public static final byte SIGNPOST_NORTH = 0x8;
	public static final byte SIGNPOST_SOUTH = 0x0;
	public static final byte SIGNPOST_WEST = 0x4;
	public static final byte SIGNPOST_EAST = 0x12;
	
	public static final short MUNDANE_POTION_DATA = 8192;
	public static final short MUNDANE_POTION_EXT_DATA = 64;
	public static final short THICK_POTION_DATA = 32;
	
	public static String getDisplayName(int id, int data) {
		if (id == AIR) return "Air";
		if (id == STONE && data == DATA_0) return "Stone";
		if (id == STONE && data == GRANITE) return "Granite";
		if (id == STONE && data == POLISHED_GRANITE) return "Polished Granite";
		if (id == STONE && data == DIORITE) return "Diorite";
		if (id == STONE && data == POLISHED_DIORITE) return "Polished Diorite";
		if (id == STONE && data == ANDESITE) return "Andesite";
		if (id == STONE && data == POLISHED_ANDESITE) return "Polished Andesite";
		if (id == GRASS) return "Grass Block";
		if (id == DIRT && data == DATA_0) return "Dirt";
		if (id == DIRT && data == COARSE_DIRT) return "Coarse Dirt";
		if (id == DIRT && data == PODZOL) return "Podzol";
		if (id == COBBLESTONE) return "Cobblestone";
		if (id == PLANK && data == DATA_0) return "Oak Plank";
		if (id == PLANK && data == DATA_1) return "Spruce Plank";
		if (id == PLANK && data == DATA_2) return "Birch Plank";
		if (id == PLANK && data == DATA_3) return "Jungle Plank";
		if (id == PLANK && data == DATA_4) return "Acacia Plank";
		if (id == PLANK && data == DATA_5) return "Dark Oak Plank";
		if (id == SAPLING && data == DATA_0) return "Oak Sapling";
		if (id == SAPLING && data == DATA_1) return "Spruce Sapling";
		if (id == SAPLING && data == DATA_2) return "Birch Sapling";
		if (id == SAPLING && data == DATA_3) return "Jungle Sapling";
		if (id == SAPLING && data == DATA_4) return "Acacia Sapling";
		if (id == SAPLING && data == DATA_5) return "Dark Oak Sapling";
		if (id == BEDROCK) return "Bedrock";
		if (id == WATER_RUNNING) return "Flowing Water";
		if (id == WATER_STILL) return "Water";
		if (id == LAVA_RUNNING) return "Flowing Lava";
		if (id == LAVA_STILL) return "Lava";
		if (id == SAND && data == DATA_0) return "Sand";
		if (id == SAND && data == RED_SAND) return "Red Sand";
		if (id == GRAVEL) return "Gravel";
		if (id == GOLD_ORE) return "Gold Ore";
		if (id == IRON_ORE) return "Iron Ore";
		if (id == COAL_ORE) return "Coal Ore";
		if (id == LOG && data == DATA_0) return "Oak Log";
		if (id == LOG && data == DATA_1) return "Spruce Log";
		if (id == LOG && data == DATA_2) return "Birch Log";
		if (id == LOG && data == DATA_3) return "Jungle Log";
		if (id == LEAF && data == DATA_0) return "Oak Leaves";
		if (id == LEAF && data == DATA_1) return "Spruce Leaves";
		if (id == LEAF && data == DATA_2) return "Birch Leaves";
		if (id == LEAF && data == DATA_3) return "Jungle Leaves";
		if (id == SPONGE && data == DATA_0) return "Dry Sponge";
		if (id == SPONGE && data == DATA_1) return "Wet Sponge";
		if (id == GLASS) return "Glass";
		if (id == LAPIS_ORE) return "Lapis Ore";
		if (id == LAPIS_BLOCK) return "Lapis Block";
		if (id == DISPENSER) return "Dispenser";
		if (id == SANDSTONE && data == DATA_0) return "Sandstone";
		if (id == SANDSTONE && data == DATA_1) return "Chiseled Sandstone";
		if (id == SANDSTONE && data == DATA_2) return "Smooth Sandstone";
		if (id == NOTEBLOCK) return "Note Block";
		
		if (id == RAIL_POWERED) return "Powered Rail";
		if (id == RAIL_DETECTOR) return "Detector Rail";
		if (id == STICKY_PISTON) return "Sticky Piston";
		if (id == COBWEB) return "Cobweb";
		if (id == TALL_GRASS && data == DATA_0) return "Shrub";
		if (id == TALL_GRASS && data == DATA_1) return "Grass";
		if (id == TALL_GRASS && data == DATA_2) return "Fern";
		if (id == DEAD_BUSH) return "Dead Bush";
		if (id == PISTON) return "Piston";
		
		if (id == WOOL && data == DATA_0) return "White Wool";
		if (id == WOOL && data == DATA_1) return "Orange Wool";
		if (id == WOOL && data == DATA_2) return "Magenta Wool";
		if (id == WOOL && data == DATA_3) return "Light Blue Wool";
		if (id == WOOL && data == DATA_4) return "Yellow Wool";
		if (id == WOOL && data == DATA_5) return "Lime Wool";
		if (id == WOOL && data == DATA_6) return "Pink Wool";
		if (id == WOOL && data == DATA_7) return "Gray Wool";
		if (id == WOOL && data == DATA_8) return "Light Gray Wool";
		if (id == WOOL && data == DATA_9) return "Cyan Wool";
		if (id == WOOL && data == DATA_10) return "Purple Wool";
		if (id == WOOL && data == DATA_11) return "Blue Wool";
		if (id == WOOL && data == DATA_12) return "Brown Wool";
		if (id == WOOL && data == DATA_13) return "Green Wool";
		if (id == WOOL && data == DATA_14) return "Red Wool";
		if (id == WOOL && data == DATA_15) return "Black Wool";
		
		if (id == DANDELION) return "Dandelion";
		if (id == OTHER_FLOWERS && data == DATA_0) return "Poppy";
		if (id == OTHER_FLOWERS && data == DATA_1) return "Blue Orchid";
		if (id == OTHER_FLOWERS && data == DATA_2) return "Allium";
		if (id == OTHER_FLOWERS && data == DATA_3) return "Azure Bluet";
		if (id == OTHER_FLOWERS && data == DATA_4) return "Red Tulip";
		if (id == OTHER_FLOWERS && data == DATA_5) return "Orange Tulip";
		if (id == OTHER_FLOWERS && data == DATA_6) return "White Tulip";
		if (id == OTHER_FLOWERS && data == DATA_7) return "Pink Tulip";
		if (id == OTHER_FLOWERS && data == DATA_8) return "Oxeye Daisy";
		if (id == BROWN_MUSHROOM) return "Brown Mushroom";
		if (id == RED_MUSHROOM) return "Red Mushroom";
		if (id == GOLD_BLOCK) return "Gold Block";
		if (id == IRON_BLOCK) return "Iron Block";
		
		if (id == SLABS && data == DATA_0) return "Stone Slab";
		if (id == SLABS && data == DATA_1) return "Sandstone Slab";
		if (id == SLABS && data == DATA_3) return "Cobblestone Slab";
		if (id == SLABS && data == DATA_4) return "Brick Slab";
		if (id == SLABS && data == DATA_5) return "Stone Brick Slab";
		if (id == SLABS && data == DATA_6) return "Nether Brick Slab";
		if (id == SLABS && data == DATA_7) return "Quartz Slab";
		if (id == BOOKSHELF) return "Bookshelf";
		if (id == MOSS_STONE) return "Moss Stone";
		if (id == OBSIDIAN) return "Obsidian";
		if (id == TORCH) return "Torch";
		
		if (id == SPAWNER) return "Spawner (byte"+data+")";
		if (id == OAK_STAIRS) return "Oak Stair";
		if (id == CHEST) return "Chest";
		if (id == DIAMOND_ORE) return "Diamond Ore";
		if (id == DIAMOND_BLOCK) return "Diamond Block";
		if (id == WORKBENCH) return "Crafting Table";
		
		if (id == FARMLAND) return "Farmland";
		if (id == FURNACE) return "Furnace";
		
		if (id == LADDER) return "Ladder";
		if (id == RAIL) return "Rail";
		if (id == COBBLESTONE_STAIRS) return "Cobblestone Stairs";
		
		if (id == LEVER) return "Lever";
		if (id == STONE_PLATE) return "Stone Pressure Plate";
		if (id == WOOD_PLATE) return "Wood Pressure Plate";
		if (id == REDSTONE_ORE) return "Redstone Ore";
		if (id == STONE_BUTTON) return "Stone Button";
		if (id == ICE) return "Ice";
		if (id == SNOW_BLOCK) return "Snow Block";
		if (id == CACTUS) return "Cactus";
		if (id == CLAY_BLOCK) return "Clay Block";
		if (id == FENCE) return "Oak Fence";
		if (id == PUMPKIN) return "Pumpkin";
		if (id == NETHERRACK) return "Netherrack";
		
		if (id == STAINED_GLASS && data == DATA_0) return "White Stained Glass";
		if (id == STAINED_GLASS && data == DATA_1) return "Orange Stained Glass";
		if (id == STAINED_GLASS && data == DATA_2) return "Magenta Stained Glass";
		if (id == STAINED_GLASS && data == DATA_3) return "Light Blue Stained Glass";
		if (id == STAINED_GLASS && data == DATA_4) return "Yellow Stained Glass";
		if (id == STAINED_GLASS && data == DATA_5) return "Lime Stained Glass";
		if (id == STAINED_GLASS && data == DATA_6) return "Pink Stained Glass";
		if (id == STAINED_GLASS && data == DATA_7) return "Gray Stained Glass";
		if (id == STAINED_GLASS && data == DATA_8) return "Light Gray Stained Glass";
		if (id == STAINED_GLASS && data == DATA_9) return "Cyan Stained Glass";
		if (id == STAINED_GLASS && data == DATA_10) return "Purple Stained Glass";
		if (id == STAINED_GLASS && data == DATA_11) return "Blue Stained Glass";
		if (id == STAINED_GLASS && data == DATA_12) return "Brown Stained Glass";
		if (id == STAINED_GLASS && data == DATA_13) return "Green Stained Glass";
		if (id == STAINED_GLASS && data == DATA_14) return "Red Stained Glass";
		if (id == STAINED_GLASS && data == DATA_15) return "Black Stained Glass";
		
		if (id == STAINED_CLAY && data == DATA_0) return "White Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_1) return "Orange Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_2) return "Magenta Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_3) return "Light Blue Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_4) return "Yellow Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_5) return "Lime Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_6) return "Pink Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_7) return "Gray Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_8) return "Light Gray Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_9) return "Cyan Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_10) return "Purple Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_11) return "Blue Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_12) return "Brown Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_13) return "Green Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_14) return "Red Hardened Clay";
		if (id == STAINED_CLAY && data == DATA_15) return "Black Hardened Clay";
		
		if (id == FLINT_AND_STEEL) return "Flint and Steel";
		if (id == APPLE) return "Apple";
		if (id == BOW) return "Minecraft Bow";
		if (id == ARROW) return "Arrow";
		if (id == COAL && data == DATA_0) return "Coal";
		if (id == COAL && data == DATA_1) return "Charcoal";
		if (id == DIAMOND) return "Diamond";
		if (id == IRON_INGOT) return "Iron Ingot";
		if (id == GOLD_INGOT) return "Gold Ingot";
		
		if (id == STICK) return "Stick";
		if (id == BOWL) return "Bowl";
		
		if (id == STRING) return "String";
		if (id == FEATHER) return "Feather";
		if (id == GUNPOWDER) return "Gunpowder";
		
		if (id == WHEAT_SEED) return "Wheat Seed";
		if (id == WHEAT_ITEM) return "Wheat";
		if (id == BREAD) return "Bread";
		
		
		if (id == DYE && data == DATA_4) return "Lapis Lazuli";
		
		if (id == REDSTONE_DUST) return "Redstone Dust";
		
		if (id == LEATHER) return "Leather";
		if (id == SUGARCANE) return "Sugar Cane";
		if (id == CLAY) return "Clay";
		
		if (id == RAW_FISH && data == DATA_0) return "Raw Fish";
		if (id == RAW_FISH && data == DATA_1) return "Raw Salmon";
		if (id == RAW_FISH && data == DATA_2) return "Clownfish";
		if (id == RAW_FISH && data == DATA_3) return "Pufferfish";
		
		if (id == COOKED_FISH && data == DATA_0) return "Cooked Fish";
		if (id == COOKED_FISH && data == DATA_1) return "Cooked Salmon";
		
		if (id == EMERALD) return "Emerald";
		
		Material m = ItemManager.getMaterial(id);
		return "["+id+","+data+", Mat: "+m.name()+"]";
	}
	
	public static String getNumeral(int i) {
		if (i == 1) return "I";
		if (i == 2) return "II";
		if (i == 3) return "III";
		if (i == 4) return "IV";
		if (i == 5) return "V";
		if (i == 6) return "VI";
		if (i == 7) return "VII";
		if (i == 8) return "VIII";
		if (i == 9) return "IX";
		if (i == 10) return "X";
		return null;
	}
	
	public static boolean canGrowFromStem(BlockSnapshot bs) {
		int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		boolean hasAir = false;
		for (int i = 0; i < 4; i++) {
			BlockSnapshot nextBs;
			try {
				nextBs = bs.getRelative(offset[i][0], 0, offset[i][1]);
			} catch (InvalidBlockLocation e) {
				// The block is on the edge of this farm plot. it _could_ grow but lets not say it can to be safe.
				return false;
			}
			
			if (nextBs.getTypeId() == CivData.AIR) {
				hasAir = true;
			}
			
			if ((nextBs.getTypeId() == CivData.MELON && bs.getTypeId() == CivData.MELON_STEM) ||
					(nextBs.getTypeId() == CivData.PUMPKIN && bs.getTypeId() == CivData.PUMPKIN_STEM)) {
				return false;
			}
		}
		return hasAir;
	}
	
	public static boolean canGrowMushroom(BlockState blockState) {
		int[][] offset = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
		boolean hasAir = false;
		for (int i = 0; i < 4; i++) {
			Block nextBlock = blockState.getBlock().getRelative(offset[i][0], 0, offset[i][1]);
			if (ItemManager.getId(nextBlock) == CivData.AIR) {
				hasAir = true;
			}
		}
		return hasAir;
	}
	
/*	public static boolean canSugarcaneGrow(BlockSnapshot bs) {
		int total = 1; //include our block
		BlockSnapshot nextBlock = bs;
		// Get # of sugarcanes above us
		//Using a for loop to prevent possible infinite loop
		for (int i = 0; i <= Farm.MAX_SUGARCANE_HEIGHT; i++) {
			try {
				nextBlock = bs.getRelative(0, 1, 0);
			} catch (InvalidBlockLocation e) {
				e.printStackTrace();
			}
			if (ItemManager.getId(nextBlock) == CivData.SUGARCANE_BLOCK) {
				total++;
			} else {
				break;
			}
		}
		
		nextBlock = bs;
		// Get # of sugarcanes below us
		for (int i = 0; i <= Farm.MAX_SUGARCANE_HEIGHT; i++) {
			try {
				nextBlock = nextBlock.getRelative(0, -1, 0);
			} catch (InvalidBlockLocation e) {
				e.printStackTrace();
			}
			if (ItemManager.getId(nextBlock) == CivData.SUGARCANE_BLOCK) {
				total++;
			} else {
				break;
			}
		}
		
		// Compare total+1 with max height.
		if (total < Farm.MAX_SUGARCANE_HEIGHT) {
			return true;
		}
		return false;
	}*/
	
	public static boolean canCocoaGrow(BlockSnapshot bs) {
		byte bits = (byte) (bs.getData() & 0xC);
		if (bits == 0x8)
			return false;
		return true;
	}
	
	public static byte getNextCocoaValue(BlockSnapshot bs) {
		byte bits = (byte) (bs.getData() & 0xC);
		if (bits == 0x0) return 0x4;
		else if (bits == 0x4) return 0x8;
		else return 0x8;
	}
	
	public static boolean canGrow(BlockSnapshot bs) {
		switch (bs.getTypeId()) {
		case WHEAT_CROP:
		case CARROT_CROP:
		case POTATO_CROP:
			if (bs.getData() == 0x7) return false;
			return true;
		
		case BEETROOT_CROP:
		case NETHERWART_CROP:
			if (bs.getData() == 0x3) return false;
			return true;
		
		case COCOA_CROP:
			return canCocoaGrow(bs);
		
		case MELON_STEM:
		case PUMPKIN_STEM:
			return canGrowFromStem(bs);
		
		//case REDMUSHROOM:
		//case BROWNMUSHROOM:
		//	return canGrowMushroom(blockState);
			
//		case SUGARCANE_BLOCK:	
//			return canSugarcaneGrow(bs);
		}
		return false;
	}
	
	public static boolean willInstantBreak(Material type) {
		switch (type) {
		case BED_BLOCK:
		case BROWN_MUSHROOM:
		case CROPS:
		case DEAD_BUSH:
		case DIODE:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case FIRE:
		case FLOWER_POT:
		case FLOWER_POT_ITEM:
		case GLASS:
		case GRASS:
		case LEAVES:
		case LEVER:
		case LONG_GRASS:
		case MELON_STEM:
		case NETHER_STALK:
		case NETHER_WARTS:
		case PUMPKIN_STEM:
		case REDSTONE:
		case REDSTONE_TORCH_OFF:
		case REDSTONE_TORCH_ON:
		case REDSTONE_WIRE:
		case SAPLING:
		case SKULL:
		case SKULL_ITEM:
		case SNOW:
		case SUGAR_CANE_BLOCK:
		case THIN_GLASS:
		case TNT:		
		case TORCH:
		case TRIPWIRE:
		case TRIPWIRE_HOOK:
		case VINE:
		case WATER_LILY:
		case YELLOW_FLOWER:
			return true;
		default:
			return false;
		}
	}
	
	public static byte convertSignDataToDoorDirectionData(byte data) {
		switch(data) {
		case SIGNPOST_NORTH:
			return 0x1;
		case SIGNPOST_SOUTH:
			return 0x3;
		case SIGNPOST_EAST:
			return 0x2;
		case SIGNPOST_WEST:
			return 0x0;
		}
		return 0x0;
	}
	
	public static byte convertSignDataToChestData(byte data) {
		/* Chests are 
			0x2: Facing north (for ladders and signs, attached to the north side of a block)
			0x3: Facing south
			0x4: Facing west
			0x5: Facing east
		
			Signposts are
			0x0: south
			0x4: west
			0x8: north
			0xC: east
			*/
		
		switch(data) {
		case 8:
			return CHEST_NORTH;
		case 0:
			return CHEST_SOUTH;
		case 12:
			return CHEST_EAST;
		case 4:
			return CHEST_WEST;
		}
		
		System.out.println("Warning, unknown sign post direction: "+data);
		return CHEST_SOUTH;
	}
}
