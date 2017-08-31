package com.avrgaming.civcraft.items.components;

import com.avrgaming.civcraft.config.CivSettings;
import com.avrgaming.civcraft.config.ConfigBuff;
import com.avrgaming.civcraft.config.ConfigTradeGood;
import com.avrgaming.civcraft.util.CivColor;
import com.avrgaming.civcraft.util.ItemManager;

import gpl.AttributeUtil;

public class TradeResource extends ItemComponent {

	@Override
	public void onPrepareCreate(AttributeUtil attrs) {
		ConfigTradeGood good = null;
		for (ConfigTradeGood goods : CivSettings.goods.values()) {
			if (ItemManager.getId(attrs.getStack().getType()) == goods.material &&
					ItemManager.getData(attrs.getStack().getData()) == goods.material_data) {
				good = goods;
			}
		}
		
		if (good != null) {
			attrs.addLore(CivColor.PurpleBold+"Trade Resource");
			attrs.addLore(CivColor.LightGreenBold+"Coins/Hour: "+CivColor.Yellow+good.value);
			
			String[] split = getBonusDisplayString(good).split(";");
			for (String str : split) {
				attrs.addLore(CivColor.Yellow+str);
			}
		} else {
			attrs.addLore(CivColor.PurpleBold+"Trade Resource");
			attrs.addLore(CivColor.RedBold+"Error Creating, Goodie Null");
			attrs.addLore(CivColor.GoldBold+"Debug Name: "+attrs.getName());
		}
	}
	
	public String getBonusDisplayString(ConfigTradeGood good) {
		String out = "";
		for (ConfigBuff cBuff : good.buffs.values()) {
			out += CivColor.LightBlue+CivColor.UNDERLINE+cBuff.name;
			out += ";";
			out += CivColor.WhiteItalic+cBuff.description;
			out += ";";
		}
		return out;		
	}
}
