package net.solarnetwork.node.datum.battery.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.MultiDatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

public class MockBatteryDatumDataSource extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyStorageDatum>, SettingSpecifierProvider {

	private final String MAXCAP_DEFAULT = "10";
	private final String POWERDRAW_DEFAULT = "1";
	private final String CHARGE_DEFAULT = "10";

	private MockBattery mb = new MockBattery(10);
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> consumptionDataSource;

	// initaliser block to have battery loaded with default values
	// {
	//
	// setMaxcap(MAXCAP_DEFAULT);
	// setPowerDraw(POWERDRAW_DEFAULT);
	// setCharge(CHARGE_DEFAULT);
	//
	// }

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.datum.battery.mock";
	}

	@Override
	public String getDisplayName() {
		return "Mock Battery";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> items = getIdentifiableSettingSpecifiers();
		items.add(new BasicTextFieldSettingSpecifier("maxcap", MAXCAP_DEFAULT));
		items.add(new BasicTextFieldSettingSpecifier("powerDraw", POWERDRAW_DEFAULT));
		items.add(new BasicTextFieldSettingSpecifier("charge", CHARGE_DEFAULT));
		items.add(new BasicTextFieldSettingSpecifier("consumptionDataSource.propertyFilters['UID']", "Main"));
		items.add(new BasicTextFieldSettingSpecifier("consumptionDataSource.propertyFilters['groupUID']", ""));
		return items;
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	private void groupWatts() {

		Integer totalwatts = 0;
		Iterable<EnergyDatum> test = getCurrentDatum(this.consumptionDataSource);
		for (EnergyDatum ed : test) {
			totalwatts += ed.getWatts();
		}
		mb.setDraw(totalwatts.doubleValue() * 1000);
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		// groupWatts();
		if (this.consumptionDataSource == null) {
			mb.setCharge(5);
		} else {
			mb.setCharge(9);
		}
		GeneralNodeEnergyStorageDatum datum = new GeneralNodeEnergyStorageDatum();
		datum.setAvailableEnergy((long) mb.readCharge());
		datum.setAvailableEnergyPercentage(mb.percentageCapacity());

		return datum;

	}

	public void setMaxcap(String maxcap) {
		try {
			mb.setMax(Double.parseDouble(maxcap));
		} catch (NumberFormatException e) {
			// invalid keep current value
		}

	}

	public void setPowerDraw(String draw) {
		try {
			mb.setDraw(Double.parseDouble(draw));
		} catch (NumberFormatException e) {
			// invalid keep current value
		}
	}

	public void setCharge(String charge) {
		// try {
		// mb.setCharge(Double.parseDouble(charge));
		// } catch (NumberFormatException e) {
		// // invalid keep current value
		// }
	}

	public void setConsumptionDataSource(
			OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> consumptionDataSource) {
		this.consumptionDataSource = consumptionDataSource;
		mb.setDraw(-5);
		// throw new RuntimeException("This method got called");
		groupWatts();
	}

	public OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> getConsumptionDataSource() {
		return consumptionDataSource;
	}

	private Iterable<EnergyDatum> getCurrentDatum(
			OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> service) {
		if (service == null) {
			return null;
		}
		Iterable<DatumDataSource<? extends EnergyDatum>> dataSources = service.services();
		List<EnergyDatum> results = new ArrayList<EnergyDatum>();
		for (DatumDataSource<? extends EnergyDatum> dataSource : dataSources) {
			if (dataSource instanceof MultiDatumDataSource<?>) {
				@SuppressWarnings("unchecked")
				Collection<? extends EnergyDatum> datums = ((MultiDatumDataSource<? extends EnergyDatum>) dataSource)
						.readMultipleDatum();
				if (datums != null) {
					for (EnergyDatum datum : datums) {
						results.add(datum);
					}
				}
			} else {
				EnergyDatum datum = dataSource.readCurrentDatum();
				if (datum != null) {
					results.add(datum);
				}
			}
		}
		return results;
	}

}
