package net.solarnetwork.node.demandresponse.minimumstrategy;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MinimumDRDatumDataSource extends DatumDataSourceSupport
		implements SettingSpecifierProvider, DatumDataSource<GeneralNodeEnergyStorageDatum> {

	private MinimumDRStrategy linkedInstance;
	private String batteryMode = "Idle";

	// TODO refactor how linkinstance works

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.demandresponse.minimumstrategy";
	}

	@Override
	public String getDisplayName() {
		return "Minimum DR Engine";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		MinimumDRDatumDataSource defaults = new MinimumDRDatumDataSource();
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();
		results.add(new BasicTextFieldSettingSpecifier("batteryMode", defaults.batteryMode));
		return results;
	}

	public MinimumDRStrategy getLinkedInstance() {
		return linkedInstance;
	}

	public void setLinkedInstance(MinimumDRStrategy linkedInstance) {
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
