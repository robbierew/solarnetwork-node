package net.solarnetwork.node.demandresponse.battery;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.domain.GeneralNodeEnergyDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

public class MockBatteryDatumDataSource extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyDatum>, SettingSpecifierProvider {

	private final String MAXCAP_DEFAULT = "10";
	private final String POWERDRAW_DEFAULT = "1";
	private final String CHARGE_DEFAULT = "10";
	private boolean flag = false;
	private boolean flag2 = false;
	private Integer sum;
	private MockBattery mb = new MockBattery(10);
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums;

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
		items.add(new BasicTextFieldSettingSpecifier("consumptionDatums.propertyFilters['UID']", "Main"));
		items.add(new BasicTextFieldSettingSpecifier("consumptionDatums.propertyFilters['groupUID']", ""));
		return items;
	}

	public void setPowerDatums(OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums) {
		this.powerDatums = powerDatums;

		sumWattage();

	}

	private void sumWattage() {
		sum = 0;
		flag = true;
		for (DatumDataSource<? extends EnergyDatum> dds : powerDatums.services()) {
			sum += dds.readCurrentDatum().getWatts();
			flag2 = true;
		}
		mb.setCharge(sum.doubleValue());
	}

	public OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> getPowerDatums() {
		return powerDatums;
	}

	@Override
	public Class<? extends GeneralNodeEnergyDatum> getDatumType() {
		return GeneralNodeEnergyDatum.class;
	}

	@Override
	public GeneralNodeEnergyDatum readCurrentDatum() {

		GeneralNodeEnergyDatum datum = new GeneralNodeEnergyDatum();
		// datum.setAvailableEnergy((long) mb.readCharge());
		// datum.setAvailableEnergyPercentage(mb.percentageCapacity());
		if (powerDatums == null) {
			datum.putStatusSampleValue("reading", false);

		} else {
			datum.putStatusSampleValue("reading", true);
			datum.putStatusSampleValue("sum", sum);
		}
		datum.putStatusSampleValue("flag", flag);
		datum.putStatusSampleValue("flag2", flag2);
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
		try {
			mb.setCharge(Double.parseDouble(charge));
		} catch (NumberFormatException e) {
			// invalid keep current value
		}
	}

}
