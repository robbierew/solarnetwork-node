package net.solarnetwork.node.demandresponse.battery;

public interface DRChargeableDevice extends DRDevice {
	Integer getCharge();

	Integer getMaxCharge();

	Boolean isDischarging();

}
