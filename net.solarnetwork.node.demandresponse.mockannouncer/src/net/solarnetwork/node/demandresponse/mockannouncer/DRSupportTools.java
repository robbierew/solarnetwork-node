package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.Map;

public class DRSupportTools {
	public static Integer readWatts(Map<String,?> params) {
		return readValue("watts",params);
	}
	
	public static Integer readMaxWatts(Map<String,?> params) {
		return readValue("maxwatts",params);
	}
	
	public static Integer readMinWatts(Map<String,?> params) {
		return readValue("minwatts",params);
	}
	
	public static Integer readEnergyCost(Map<String,?> params) {
		return readValue("energycost",params);
	}
	
	public static boolean isDRCapable(Map<String,?> params) {
		return readBoolean("drcapable",params);
	}
	
	public static boolean isChargeable(Map<String,?> params) {
		return readBoolean("chargeable",params);
	}
	
	public static boolean isDischarging(Map<String,?> params) {
		return readBoolean("isDischarging",params);
	}
	
	private static boolean readBoolean(String paramname,Map<String,?> params) {
		return Boolean.parseBoolean((String)params.get(paramname));
	}
	
	private static Integer readValue(String paramname,Map<String,?> params) {
		Integer result = null;
		try {
			result = (int)(double)Double.parseDouble((String)params.get(paramname));
		} finally{
			//Do nothing
		}
		return result;
	}
}
