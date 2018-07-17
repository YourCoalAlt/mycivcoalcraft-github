package com.avrgaming.civcraft.util;

import org.bukkit.ChatColor;

public class CivColor {
	
	public static final String Bold = ""+ChatColor.BOLD;
	public static final String Italic = ""+ChatColor.ITALIC;
	public static final String MAGIC = ""+ChatColor.MAGIC;
	public static final String STRIKETHROUGH = ""+ChatColor.STRIKETHROUGH;
	public static final String RESET = ""+ChatColor.RESET;
	public static final String UNDERLINE = ""+ChatColor.UNDERLINE;
	
	public static final String Black = "\u00A70";
	public static final String DarkBlue = "\u00A71";
	public static final String Green = "\u00A72";
	public static final String Aqua = "\u00A73";
	public static final String Red = "\u00A74";
	public static final String Purple = "\u00A75";
	public static final String Gold = "\u00A76";
	public static final String Gray = "\u00A77";
	public static final String DarkGray = "\u00A78";
	public static final String Navy = "\u00A79";
	public static final String LightGreen = "\u00A7a";
	public static final String LightBlue = "\u00A7b";
	public static final String Rose = "\u00A7c";
	public static final String LightPurple = "\u00A7d";
	public static final String Yellow = "\u00A7e";
	public static final String White = "\u00A7f";
	
	public static final String BlackBold = Black+Bold;
	public static final String DarkBlueBold = DarkBlue+Bold;
	public static final String GreenBold = Green+Bold;
	public static final String AquaBold = Aqua+Bold;
	public static final String RedBold = Red+Bold;
	public static final String PurpleBold = Purple+Bold;
	public static final String GoldBold = Gold+Bold;
	public static final String GrayBold = Gray+Bold;
	public static final String DarkGrayBold = DarkGray+Bold;
	public static final String NavyBold = Navy+Bold;
	public static final String LightGreenBold = LightGreen+Bold;
	public static final String LightBlueBold = LightBlue+Bold;
	public static final String RoseBold = Rose+Bold;
	public static final String LightPurpleBold = LightPurple+Bold;
	public static final String YellowBold = Yellow+Bold;
	public static final String WhiteBold = White+Bold;
	
	public static final String BlackItalic = Black+Italic;
	public static final String DarkBlueItalic = DarkBlue+Italic;
	public static final String GreenItalic = Green+Italic;
	public static final String AquaItalic = Aqua+Italic;
	public static final String RedItalic = Red+Italic;
	public static final String PurpleItalic = Purple+Italic;
	public static final String GoldItalic = Gold+Italic;
	public static final String GrayItalic = Gray+Italic;
	public static final String DarkGrayItalic = DarkGray+Italic;
	public static final String NavyItalic = Navy+Italic;
	public static final String LightGreenItalic = LightGreen+Italic;
	public static final String LightBlueItalic = LightBlue+Italic;
	public static final String RoseItalic = Rose+Italic;
	public static final String LightPurpleItalic = LightPurple+Italic;
	public static final String YellowItalic = Yellow+Italic;
	public static final String WhiteItalic = White+Italic;
	
	public static final String BlackBoldItalic = Black+Bold+Italic;
	public static final String DarkBlueBoldItalic = DarkBlue+Bold+Italic;
	public static final String GreenBoldItalic = Green+Bold+Italic;
	public static final String AquaBoldItalic = Aqua+Bold+Italic;
	public static final String RedBoldItalic = Red+Bold+Italic;
	public static final String PurpleBoldItalic = Purple+Bold+Italic;
	public static final String GoldBoldItalic = Gold+Bold+Italic;
	public static final String GrayBoldItalic = Gray+Bold+Italic;
	public static final String DarkGrayBoldItalic = DarkGray+Bold+Italic;
	public static final String NavyBoldItalic = Navy+Bold+Italic;
	public static final String LightGreenBoldItalic = LightGreen+Bold+Italic;
	public static final String LightBlueBoldItalic = LightBlue+Bold+Italic;
	public static final String RoseBoldItalic = Rose+Bold+Italic;
	public static final String LightPurpleBoldItalic = LightPurple+Bold+Italic;
	public static final String YellowBoldItalic = Yellow+Bold+Italic;
	public static final String WhiteBoldItalic = White+Bold+Italic;
	
	/*
	 * Takes an input from a yaml and converts 'Essentials' style color codes into 
	 * in game color codes.
	 * XXX this is slow, so try not to do this at runtime. Just when configs load.
	 */
	public static String colorize(String input) {
		String output = input;
		
		output = output.replaceAll("<red>", Red);
		output = output.replaceAll("<rose>", Rose);
		output = output.replaceAll("<gold>", Gold);
		output = output.replaceAll("<yellow>", Yellow);
		output = output.replaceAll("<green>", Green);
		output = output.replaceAll("<lightgreen>", LightGreen);
		output = output.replaceAll("<lightblue>", LightBlue);
		output = output.replaceAll("<blue>", Aqua);
		output = output.replaceAll("<darkblue>", DarkBlue);
		output = output.replaceAll("<navy>", Navy);
		output = output.replaceAll("<lightpurple>", LightPurple);
		output = output.replaceAll("<purple>", Purple);
		output = output.replaceAll("<white>", White);
		output = output.replaceAll("<lightgray>", Gray);
		output = output.replaceAll("<gray>", DarkGray);
		output = output.replaceAll("<black>", Black);
		
		output = output.replaceAll("<redb>", RedBold);
		output = output.replaceAll("<roseb>", RoseBold);
		output = output.replaceAll("<goldb>", GoldBold);
		output = output.replaceAll("<yellowb>", YellowBold);
		output = output.replaceAll("<greenb>", GreenBold);
		output = output.replaceAll("<lightgreenb>", LightGreenBold);
		output = output.replaceAll("<lightblueb>", LightBlueBold);
		output = output.replaceAll("<blueb>", AquaBold);
		output = output.replaceAll("<darkblueb>", DarkBlueBold);
		output = output.replaceAll("<navyb>", NavyBold);
		output = output.replaceAll("<lightpurpleb>", LightPurpleBold);
		output = output.replaceAll("<purpleb>", PurpleBold);
		output = output.replaceAll("<whiteb>", WhiteBold);
		output = output.replaceAll("<lightgrayb>", GrayBold);
		output = output.replaceAll("<grayb>", DarkGrayBold);
		output = output.replaceAll("<blackb>", BlackBold);
		
		output = output.replaceAll("<redi>", RedItalic);
		output = output.replaceAll("<rosei>", RoseItalic);
		output = output.replaceAll("<goldi>", GoldItalic);
		output = output.replaceAll("<yellowi>", YellowItalic);
		output = output.replaceAll("<greeni>", GreenItalic);
		output = output.replaceAll("<lightgreeni>", LightGreenItalic);
		output = output.replaceAll("<lightbluei>", LightBlueItalic);
		output = output.replaceAll("<bluei>", AquaItalic);
		output = output.replaceAll("<darkbluei>", DarkBlueItalic);
		output = output.replaceAll("<navyi>", NavyItalic);
		output = output.replaceAll("<lightpurplei>", LightPurpleItalic);
		output = output.replaceAll("<purplei>", PurpleItalic);
		output = output.replaceAll("<whitei>", WhiteItalic);
		output = output.replaceAll("<lightgrayi>", GrayItalic);
		output = output.replaceAll("<grayi>", DarkGrayItalic);
		output = output.replaceAll("<blacki>", BlackItalic);
		
		output = output.replaceAll("<b>", ""+ChatColor.BOLD);
		output = output.replaceAll("<u>", ""+ChatColor.UNDERLINE);
		output = output.replaceAll("<i>", ""+ChatColor.ITALIC);
		output = output.replaceAll("<magic>", ""+ChatColor.MAGIC);
		output = output.replaceAll("<s>", ""+ChatColor.STRIKETHROUGH);
		output = output.replaceAll("<r>", ""+ChatColor.RESET);
		
		return output;
	}
	
	public static String strip(String line) {
		for (ChatColor cc : ChatColor.values()) {
			line.replaceAll(cc.toString(), "");
		}
		return line;
	}

	public static String valueOf(String color) {
		switch (color.toLowerCase()) {
		case "black":
			return Black;
		case "darkblue":
			return DarkBlue;
		case "green":
			return Green;
		case "blue":
			return Aqua;
		case "red":
			return Red;
		case "purple":
			return Purple;
		case "gold":
			return Gold;
		case "lightgray":
			return Gray;
		case "gray":
			return DarkGray;
		case "navy":
			return Navy;
		case "lightgreen":
			return LightGreen;
		case "lightblue":
			return LightBlue;
		case "rose":
			return Rose;
		case "lightpurple":
			return LightPurple;
		case "yellow":
			return Yellow;
		case "white":
			return White;
		default:
			return White;
		}		
	}

	public static String stripTags(String input) {
		String output = input;
		
		output = output.replaceAll("<red>", "");
		output = output.replaceAll("<rose>", "");
		output = output.replaceAll("<gold>", "");
		output = output.replaceAll("<yellow>", "");
		output = output.replaceAll("<green>", "");
		output = output.replaceAll("<lightgreen>", "");
		output = output.replaceAll("<lightblue>", "");
		output = output.replaceAll("<blue>", "");
		output = output.replaceAll("<darkblue>", "");
		output = output.replaceAll("<navy>", "");
		output = output.replaceAll("<lightpurple>", "");
		output = output.replaceAll("<purple>", "");
		output = output.replaceAll("<white>", "");
		output = output.replaceAll("<lightgray>", "");
		output = output.replaceAll("<gray>", "");
		output = output.replaceAll("<black>", "");
		
		output = output.replaceAll("<redb>", "");
		output = output.replaceAll("<roseb>", "");
		output = output.replaceAll("<goldb>", "");
		output = output.replaceAll("<yellowb>", "");
		output = output.replaceAll("<greenb>", "");
		output = output.replaceAll("<lightgreenb>", "");
		output = output.replaceAll("<lightblueb>", "");
		output = output.replaceAll("<blueb>", "");
		output = output.replaceAll("<darkblueb>", "");
		output = output.replaceAll("<navyb>", "");
		output = output.replaceAll("<lightpurpleb>", "");
		output = output.replaceAll("<purpleb>", "");
		output = output.replaceAll("<whiteb>", "");
		output = output.replaceAll("<lightgrayb>", "");
		output = output.replaceAll("<grayb>", "");
		output = output.replaceAll("<blackb>", "");
		
		output = output.replaceAll("<redi>", "");
		output = output.replaceAll("<rosei>", "");
		output = output.replaceAll("<goldi>", "");
		output = output.replaceAll("<yellowi>", "");
		output = output.replaceAll("<greeni>", "");
		output = output.replaceAll("<lightgreeni>", "");
		output = output.replaceAll("<lightbluei>", "");
		output = output.replaceAll("<bluei>", "");
		output = output.replaceAll("<darkbluei>", "");
		output = output.replaceAll("<navyi>", "");
		output = output.replaceAll("<lightpurplei>", "");
		output = output.replaceAll("<purplei>", "");
		output = output.replaceAll("<whitei>", "");
		output = output.replaceAll("<lightgrayi>", "");
		output = output.replaceAll("<grayi>", "");
		output = output.replaceAll("<blacki>", "");
		
		output = output.replaceAll("<i>", "");
		output = output.replaceAll("<u>", "");
		output = output.replaceAll("<i>", "");
		output = output.replaceAll("<magic>", "");
		output = output.replaceAll("<s>", "");
		output = output.replaceAll("<r>", "");
		
		return output;
	}
}
