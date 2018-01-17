package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.Map;

/**
 * Helper class to make use of demand response.
 * 
 * @author robert
 *
 */
public class DRSupportTools {

	public static final String DRPARAMS_INSTRUCTION = "getDRDeviceInstance";

	public static final String WATTS_PARAM = "watts";
	public static final String MAXWATTS_PARAM = "maxwatts";
	public static final String MINWATTS_PARAM = "minwatts";
	public static final String ENERGYCOST_PARAM = "energycost";
	public static final String DRCAPABLE_PARAM = "drcapable";
	public static final String CHARGEABLE_PARAM = "chargeable";
	public static final String DISCHARGING_PARAM = "isDischarging";

	/**
	 * Reads the wattage reading from the parameters. If there is no wattage
	 * parameter the method returns null.
	 * 
	 * @param params
	 *            The parameters of the device
	 * @return The wattage reading of the device
	 */
	public static Integer readWatts(Map<String, ?> params) {
		return readValue(WATTS_PARAM, params);
	}

	/**
	 * Reads the max watts reading from the parameters. If no wattage reading is
	 * present the method returns null.
	 * 
	 * A max wattage reading is the maximum amount of watts the device can
	 * operate on.
	 * 
	 * @param params
	 *            The parameters of the device
	 * @return The wattage reading of the device
	 */
	public static Integer readMaxWatts(Map<String, ?> params) {
		return readValue(MAXWATTS_PARAM, params);
	}

	/**
	 * Reads the min watts reading from the paramters. If no wattage reading is
	 * present the method returns null.
	 * 
	 * A min wattage reading is the minimum amount of the watts the device can
	 * operate on.
	 * 
	 * @param params
	 * @return
	 */
	public static Integer readMinWatts(Map<String, ?> params) {
		return readValue(MINWATTS_PARAM, params);
	}

	/**
	 * Reads the energy cost from the parameters. If no energy cost reading is
	 * present the method returns null.
	 * 
	 * The energy cost is the price per watt to operate the device.
	 * 
	 * @param params
	 * @return
	 */
	public static Integer readEnergyCost(Map<String, ?> params) {
		return readValue(ENERGYCOST_PARAM, params);
	}

	/**
	 * Checks whether the parameters specify whether the device can handle
	 * demand size response.
	 * 
	 * @param params
	 * @return
	 */
	public static boolean isDRCapable(Map<String, ?> params) {
		return readBoolean("drcapable", params);
	}

	/**
	 * Checks the parameters to see if the type of device supports charging for
	 * example a battery
	 * 
	 * @param params
	 * @return
	 */
	public static boolean isChargeable(Map<String, ?> params) {
		return readBoolean(CHARGEABLE_PARAM, params);
	}

	/**
	 * Checks the paramters to see if the device is discharging. If the device
	 * does not support charging see {@link isChargeable} this method returns
	 * false. Should only be called after verifying the device is chargeable
	 * 
	 * TODO due to implimentation of readBoolean method in this class the
	 * behaviour of false on non charging is not true
	 * 
	 * @param params
	 * @return
	 */
	public static boolean isDischarging(Map<String, ?> params) {
		return readBoolean(DISCHARGING_PARAM, params);
	}

	// TODO fix behaviour
	private static boolean readBoolean(String paramname, Map<String, ?> params) {
		// cast to string then check for boolean
		// just realised this does not check for nulls or non strings
		return Boolean.parseBoolean((String) params.get(paramname));
	}

	private static Integer readValue(String paramname, Map<String, ?> params) {
		// set variable to null to avoid uninitalised warnings
		Integer result = null;
		try {
			result = (int) Double.parseDouble((String) params.get(paramname));
		} finally {
			// Do nothing, in the case there was an exception the returned value
			// will be null
		}
		return result;
	}
}
