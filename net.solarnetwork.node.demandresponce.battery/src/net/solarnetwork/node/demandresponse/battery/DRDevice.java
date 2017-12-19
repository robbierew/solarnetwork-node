package net.solarnetwork.node.demandresponse.battery;

import net.solarnetwork.node.reactor.FeedbackInstructionHandler;

public interface DRDevice extends FeedbackInstructionHandler {

	/**
	 * Examples a battery has a limit to the number of times it can charge and
	 * discharge. Set to a negative number if there is profit to using energy
	 * 
	 * @return the price to use energy from this device
	 */
	public Integer getEnergyCost();

	/**
	 * 
	 * @return the maximum power the device can run at
	 */
	public Integer getMaxPower();

	/**
	 * 
	 * @return the minimum power the device can run at
	 */
	public Integer getMinPower();

	// set to true if the device can only run at max or min and not between
	public Boolean isToggleDevice();

	// returns the current comsumption of device
	public Integer getWatts();
}
