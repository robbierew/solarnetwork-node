package net.solarnetwork.node.datum.battery.mock;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MockBatteryDatumDataSource extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyStorageDatum>, SettingSpecifierProvider {

	private final String MAXCAP_DEFAULT = "10";
	private final String POWERDRAW_DEFAULT = "1";
	private final String CHARGE_DEFAULT = "10";

	private MockBattery mb = new MockBattery(10);

	// initaliser block to have battery loaded with default values
	{

		setMaxcap(MAXCAP_DEFAULT);
		setPowerDraw(POWERDRAW_DEFAULT);
		setCharge(CHARGE_DEFAULT);

	}

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
		return items;
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
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
		try {
			mb.setCharge(Double.parseDouble(charge));
		} catch (NumberFormatException e) {
			// invalid keep current value
		}
	}

}
