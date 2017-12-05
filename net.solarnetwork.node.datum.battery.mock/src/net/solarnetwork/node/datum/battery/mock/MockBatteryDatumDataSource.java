package net.solarnetwork.node.datum.battery.mock;

import java.util.List;

import org.springframework.context.MessageSource;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;

public class MockBatteryDatumDataSource
		implements DatumDataSource<GeneralNodeEnergyStorageDatum>, SettingSpecifierProvider {

	@Override
	public String getUID() {
		// TODO Auto-generated method stub
		return "TODO";
	}

	@Override
	public String getGroupUID() {
		// TODO Auto-generated method stub
		return "TODOGROUP";
	}

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageSource getMessageSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		// TODO Auto-generated method stub
		return null;
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
