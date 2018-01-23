package net.solarnetwork.node.demandresponse.mockbattery;

public interface DRChargeableDevice extends DRDevice {
	Integer getCharge();

	Integer getMaxCharge();

	Boolean isDischarging();

}
