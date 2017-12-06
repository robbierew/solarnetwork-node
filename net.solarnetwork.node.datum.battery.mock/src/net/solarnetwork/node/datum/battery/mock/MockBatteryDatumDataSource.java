package net.solarnetwork.node.datum.battery.mock;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MockBatteryDatumDataSource extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyStorageDatum>, SettingSpecifierProvider {

	private final String MAXCAP_DEFAULT = "10";
	private final String POWERDRAW_DEFAULT = "1";
	private final String CHARGE_DEFAULT = "10";

	private MockBattery mb = new MockBattery(10);

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
		return items;
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		// TODO Auto-generated method stub
		return null;
	}

}
