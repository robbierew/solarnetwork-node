package net.solarnetwork.node.testing.DRTests;

import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class DRTesting extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyDatum>, SettingSpecifierProvider {

	@Override
	public Class<? extends GeneralNodeEnergyDatum> getDatumType() {
		return GeneralNodeEnergyDatum.class;
	}

	@Override
	public GeneralNodeEnergyDatum readCurrentDatum() {
		GeneralNodeEnergyDatum datum = new GeneralNodeEnergyDatum();
		datum.setCreated(new Date());
		datum.putStatusSampleValue("TestMessage", "TestValue");
		return datum;

	}

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return "net.solarnetwork.node.testing.DRTests";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "DRTesting";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = super.getIdentifiableSettingSpecifiers();
		// TODO Auto-generated method stub
		return results;
	}

}
