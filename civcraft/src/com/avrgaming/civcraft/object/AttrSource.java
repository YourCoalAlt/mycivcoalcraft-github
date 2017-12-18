package com.avrgaming.civcraft.object;

import java.text.DecimalFormat;
import java.util.HashMap;

public class AttrSource {
	
	// Contains a list of sources and the total.
	public HashMap<String, Double> sources;
	public double total;
	AttrSource rate;
	
	public AttrSource(HashMap<String, Double> sources, double total, AttrSource rate) {
		this.sources = sources;
		this.total = total;
		this.rate = rate;
	}
	
	public String getSourceDisplayString(String sourceColor, String valueColor) {
		String out = "";
		DecimalFormat df = new DecimalFormat();
		//out.add(CivMessage.buildSmallTitle("Sources"));
		for (String source : sources.keySet()) {
			out += sourceColor+source+": "+valueColor+df.format(sources.get(source));
			out += ";";
		}
		return out;
	}
	
	public String getRateDisplayString(String sourceColor, String valueColor) {
		String out = "";
		DecimalFormat df = new DecimalFormat();
		
		if (rate != null) {			
			//out.add(CivMessage.buildSmallTitle("Rates"));
			for (String source : rate.sources.keySet()) {
				out += sourceColor+source+": "+valueColor+df.format(rate.sources.get(source)*100)+"%";
				out += ";";
			}
		}
		return out;
	}
	
	public String getTotalDisplayString(String sourceColor, String valueColor) {
		String out = "";
		DecimalFormat df = new DecimalFormat();
		//out.add(CivMessage.buildSmallTitle("Totals"));
		//out.add(sourceColor+"Total: "+valueColor+df.format(this.total)+sourceColor);
		out += valueColor+df.format(this.total)+sourceColor;
		out += ";";
		return out;
	}
}
