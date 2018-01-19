package net.solarnetwork.node.demandresponse.battery;

import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

/**
 * DatumDataSource for the Demand response battery. When datums are read
 * calculates the current battery charge.
 * 
 * @author robert
 *
 */
public class DRBatteryDatumDataSource extends DatumDataSourceSupport
		implements SettingSpecifierProvider, DatumDataSource<GeneralNodeEnergyStorageDatum> {

	private Double maxCharge = 10.0;
	private Double charge = 10.0;
	private MockBattery mockbattery;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;
	private Integer cost = 1000;
	private Integer cycles = 10000;

	private String drEngineName = "DREngine";

	private Double maxDraw = 1.0;

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		// check we have access to configured settings

		// create new datum
		GeneralNodeEnergyStorageDatum datum = new GeneralNodeEnergyStorageDatum();
		datum.setCreated(new Date());
		datum.setSourceId(getUID());

		// populate the datum with values from the battery
		MockBattery mb = getMockBattery();
		datum.setAvailableEnergy((long) mb.readCharge());
		datum.setAvailableEnergyPercentage(mb.capacityFraction());
		if (mb.readDraw() == 0) {
			datum.putStatusSampleValue("Mode", "Idle");
		} else if (mb.readDraw() > 0) {
			datum.putStatusSampleValue("Mode", "Discharging");
		} else {
			datum.putStatusSampleValue("Mode", "Charging");
		}

		return datum;

	}

	// /**
	// * Looks at the datums from other plugins and reads their wattage reading.
	// * This is the powerdraw the battery will have
	// */
	// @Deprecated
	// private void calcBatteryDraw() {
	//
	// Double sum = 0.0;
	// Iterable<DatumDataSource<? extends EnergyDatum>> datumIterable =
	// settings.getPoweredDevices().services();
	//
	// for (DatumDataSource<? extends EnergyDatum> d : datumIterable) {
	//
	// EnergyDatum datum = d.readCurrentDatum();
	//
	// // not all readings have a datum or a wattage reading which is why
	// // one must check for nulls
	// if (datum != null) {
	// Integer reading = datum.getWatts();
	// if (reading != null) {
	// sum += reading.doubleValue();
	// }
	// }
	// }
	//
	// // update the batteries powerdraw
	// settings.getMockBattery().setDraw(sum);
	//
	// }

	public String getDrEngineName() {
		return drEngineName;
	}

	public void setDrEngineName(String drEngineName) {
		this.drEngineName = drEngineName;
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.demandresponse.battery";
	}

	@Override
	public String getDisplayName() {
		return "DR Battery";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = super.getIdentifiableSettingSpecifiers();
		DRBatteryDatumDataSource defaults = new DRBatteryDatumDataSource();
		results.add(new BasicTextFieldSettingSpecifier("batteryMaxCharge", defaults.maxCharge.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryMaxDraw", defaults.maxDraw.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCharge", defaults.charge.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCost", defaults.cost.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCycles", defaults.cycles.toString()));
		results.add(new BasicTextFieldSettingSpecifier("drEngineName", defaults.drEngineName));
		return results;
	}

	public void setMockBattery(MockBattery mockbattery) {
		this.mockbattery = mockbattery;
	}

	public MockBattery getMockBattery() {
		return this.mockbattery;
	}

	public void setBatteryMaxCharge(Double charge) {
		if (charge != null) {
			mockbattery.setMax(charge);
		}
		this.maxCharge = charge;
	}

	public Double getBatteryMaxCharge() {
		return maxCharge;
	}

	public Double getMaxDraw() {
		return maxDraw;
	}

	public void setMaxDraw(Double maxDraw) {
		this.maxDraw = maxDraw;
		if (mockbattery.readDraw() > maxDraw) {
			mockbattery.setDraw(maxDraw);
		}
	}

	public void setBatteryCharge(Double charge) {
		if (charge != null) {
			mockbattery.setCharge(charge);
		}
		this.charge = charge;
	}

	// note this returns the charge value the user set in settings page not the
	// current charge
	// of the battery
	public Double getBatteryCharge() {
		return this.charge;
	}

	public Integer getBatteryCost() {
		return cost;
	}

	public void setBatteryCost(Integer cost) {
		this.cost = cost;
	}

	public Integer getBatteryCycles() {
		return cycles;
	}

	public void setBatteryCycles(Integer cycles) {
		this.cycles = cycles;
	}

}
