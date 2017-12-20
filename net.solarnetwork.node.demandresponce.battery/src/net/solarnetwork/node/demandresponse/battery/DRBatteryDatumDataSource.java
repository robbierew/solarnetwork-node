package net.solarnetwork.node.demandresponse.battery;

import java.util.Date;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;

/**
 * DatumDataSource for the Demand response battery. When datums are read
 * calculates the current battery charge.
 * 
 * @author robert
 *
 */
public class DRBatteryDatumDataSource implements DatumDataSource<GeneralNodeEnergyStorageDatum> {

	private DRBatterySettings settings;

	@Override
	public String getUID() {
		return settings.getUID();
	}

	@Override
	public String getGroupUID() {
		return settings.getGroupUID();
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		// check we have access to configured settings
		if (settings != null) {

			// create new datum
			GeneralNodeEnergyStorageDatum datum = new GeneralNodeEnergyStorageDatum();
			datum.setCreated(new Date());
			datum.setSourceId(getUID());

			// populate the datum with values from the battery
			MockBattery mb = settings.getMockBattery();
			datum.setAvailableEnergy((long) mb.readCharge());
			datum.setAvailableEnergyPercentage(mb.percentageCapacity());

			return datum;

		} else {

			return null;

		}

	}

	/**
	 * Looks at the datums from other plugins and reads their wattage reading.
	 * This is the powerdraw the battery will have
	 */
	@Deprecated
	private void calcBatteryDraw() {

		Double sum = 0.0;
		Iterable<DatumDataSource<? extends EnergyDatum>> datumIterable = settings.getPoweredDevices().services();

		for (DatumDataSource<? extends EnergyDatum> d : datumIterable) {

			EnergyDatum datum = d.readCurrentDatum();

			// not all readings have a datum or a wattage reading which is why
			// one must check for nulls
			if (datum != null) {
				Integer reading = datum.getWatts();
				if (reading != null) {
					sum += reading.doubleValue();
				}
			}
		}

		// update the batteries powerdraw
		settings.getMockBattery().setDraw(sum);

	}

	public void setDRBatterySettings(DRBatterySettings settings) {
		this.settings = settings;
	}

	public DRBatterySettings getDRBatterySettings() {
		return settings;
	}

}
