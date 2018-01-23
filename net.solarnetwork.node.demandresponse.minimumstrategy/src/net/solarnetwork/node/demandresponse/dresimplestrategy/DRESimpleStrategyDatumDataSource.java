package net.solarnetwork.node.demandresponse.dresimplestrategy;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class DRESimpleStrategyDatumDataSource extends DatumDataSourceSupport
		implements SettingSpecifierProvider, DatumDataSource<GeneralNodeEnergyStorageDatum> {

	private DRESimpleStrategy linkedInstance;
	private String batteryMode = "Idle";

	// TODO refactor how linkinstance works

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.demandresponse.dresimplestrategy";
	}

	@Override
	public String getDisplayName() {
		return "Minimum DR Engine";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		DRESimpleStrategyDatumDataSource defaults = new DRESimpleStrategyDatumDataSource();
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();
		results.add(new BasicTextFieldSettingSpecifier("batteryMode", defaults.batteryMode));
		return results;
	}

	public DRESimpleStrategy getLinkedInstance() {
		return linkedInstance;
	}

	public void setLinkedInstance(DRESimpleStrategy linkedInstance) {
		this.linkedInstance = linkedInstance;
		linkedInstance.setSettings(this);
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		try {
			getLinkedInstance().drupdate();
		} catch (RuntimeException e) {
			// exceptions don't print inside this method. For debugging purposes
			// I print the stacktrace if there is an exception
			e.printStackTrace();
		}

		// the datum will contain num devices as well as watts cost?
		GeneralNodeEnergyStorageDatum datum = new GeneralNodeEnergyStorageDatum();
		datum.putInstantaneousSampleValue("Num Devices", getLinkedInstance().getNumdrdevices());
		datum.setSourceId(getUID());
		return datum;
	}

	public String getBatteryMode() {
		return batteryMode;
	}

	public void setBatteryMode(String batteryMode) {
		this.batteryMode = batteryMode;
	}

}
