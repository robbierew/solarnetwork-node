package net.solarnetwork.node.demandresponse.dretargetcost;

import java.util.Map;

/**
 * Helper class to make use of demand response. It is expected that ones uses
 * the constants and methods provided in this class to handler demand response.
 * 
 * Please have a good read of the documentation in this class before using
 * demand response. For examples of classes using this form of demand response
 * see the following packages
 * {@link net.solarnetwork.node.demandresponse.mockbattery}
 * {@link net.solarnetwork.node.demandresponse.dretargetcost}
 * 
 * There are two main concepts for demand response the DR device and the DR
 * engine. The device is ment to be physical hardware while the engine is an
 * algorithm providing instructions to the device.
 * 
 * DR Instructions use the pre-existing Instructions interface. Demand response
 * requires two way comication meaning DR Devices must implement the
 * FeedbackInstructionHandler interface and must handle the
 * DRPARAMS_INSTRUCTION.
 * 
 * There is no set standard for how a DR Engine gets a hold of the DR Device the
 * strategy I have been using is to configure in OSGI a reference list of
 * FeedbackInstructionHandlers.
 * 
 * A DR Device should only accept demand response instructions from one DREngine
 * this is done by checking that the source id of the DR Engine is present in
 * every demand response instruction parameters. The value of the parameter does
 * not have to be anything (except for InstructionHandler.TOPIC_SHED_LOAD) it
 * just must be non null. By convention I have been using empty string.
 * 
 * The precedure for demand response is as follows. The DR Engine sends to its
 * devices a DRPARAMS_INSTRUCTION those devices respond by sending back a map of
 * paramters. The map is of datatype <String,?> due to legacy implimentation of
 * FeedbackInstructionHandler. However the parameters should all come as
 * <String,String>. The following paramters are expected in the responing map in
 * order for a device to claim it supports DRPARAMS_INSTRUCTION.
 * 
 * WATTS_PARAM = The current power in watts the device is operating at. This
 * value should always be positive.
 * 
 * MAXWATTS_PARAM = The maximum amount of power the device can run at.
 * 
 * MINWATTS_PARAM = The minimum amount of power the device can run at. This
 * parameter does not represent a physical constraint of the device. Instead
 * just tells the DR Engine that this device is too important to be powered
 * below this specific level.
 * 
 * ENERGY_DEPRECIATION = The cost of running this device per watt hour. This
 * cost can reflect a resource consumption or simply deprecation from use.
 * 
 * DRREADY_PARAM = setting this parameter to "true" tells the DREngine that it
 * can expect all of these above parameters in this map. Set it to "false" when
 * the device is not ready to handle demand response at that time.
 * 
 * CHARGEABLE_PARAM = Set this to "true" if your device support demand response
 * of charging and discharging other set to "false"
 * 
 * If CHARGEABLE_PARAM was set to true the next parameter is required otherwise
 * this one can be ignored
 * 
 * DISCHARGING_PARAM = Set to "true" if the device is discharging otherwise
 * "false". Not being powered is seen as not discharging.
 * 
 * NOTE do not assume that any of these parameters are constant eg
 * MAXWATTS_PARAM. You should query for these parameters every time you want to
 * calculate a demand response.
 * 
 * 
 * To send a demand response you have the choice of sending
 * InstructionHandler.TOPIC_SHED_LOAD or
 * InstructionHandler.TOPIC_SET_CONTROL_PARAMETER.
 * 
 * When sending a TOPIC_SHED_LOAD instruction you map the DR Engine's sourceID
 * to the shedamount as per the convention already in place by this instruction.
 * The DR Device should reduce its consumption by the shedamount so long as it
 * does not conflict with its minmum wattage. It is up to your implimention on
 * how to handle this instruction when the parameters are invalid. You may
 * choose to make assumptions and error correct or decline the instruction in
 * your instruction status.
 * 
 * When sending a TOPIC_SET_CONTROL_PARAMETER instruction remember to include
 * the source ID of the DR Engine in the parameters. The parameters that are
 * allowed to be changed with this instruction are WATTS_PARAM and if
 * CHARGEABLE_PARAM was true then DISCHARGING_PARAM can be configured in this
 * instruction. You are allow to configure multiple paramters in a single
 * instruction. map the parameter you want changed to the value you want set.
 * 
 * @author robert
 *
 */
public class DRSupportTools {

	public static final String DRPARAMS_INSTRUCTION = "getDRDeviceInstance";

	public static final String WATTS_PARAM = "watts";
	public static final String MAXWATTS_PARAM = "maxwatts";
	public static final String MINWATTS_PARAM = "minwatts";
	public static final String ENERGY_DEPRECIATION = "energycost";
	public static final String DRREADY_PARAM = "drready";
	public static final String CHARGEABLE_PARAM = "chargeable";
	public static final String DISCHARGING_PARAM = "isDischarging";
	public static final String SOURCEID_PARAM = "sourceID";

	public static String readSourceID(Map<String, ?> params) {
		try {
			return (String) params.get(SOURCEID_PARAM);
		} catch (ClassCastException e) {
			return null;
		}

	}

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
	public static Integer readEnergyDepreciationCost(Map<String, ?> params) {
		return readValue(ENERGY_DEPRECIATION, params);
	}

	/**
	 * Checks whether the parameters specify whether the device can handle
	 * demand size response.
	 * 
	 * @param params
	 * @return
	 */
	public static boolean isDRCapable(Map<String, ?> params) {
		return readBoolean(DRREADY_PARAM, params);
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
