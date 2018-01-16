package net.solarnetwork.node.demandresponse.mockdevice;

import java.util.List;

import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MockDRDeviceSettings extends DatumDataSourceSupport implements SettingSpecifierProvider {

	// default values
	private String sourceId = "Mock DR Device";
	private Integer minwatts = 0;
	private Integer maxwatts = 10;
	private Integer energycost = 1;
	private Integer watts = 0;

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.demandresponse.mockdevice";
	}

	@Override
	public String getDisplayName() {
		return "Mock DR Device";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		MockDRDeviceSettings defaults = new MockDRDeviceSettings();
		List<SettingSpecifier> results = super.getIdentifiableSettingSpecifiers();

		// user enters text
		results.add(new BasicTextFieldSettingSpecifier("sourceId", defaults.sourceId));
		results.add(new BasicTextFieldSettingSpecifier("minwatts", defaults.minwatts.toString()));
		results.add(new BasicTextFieldSettingSpecifier("maxwatts", defaults.maxwatts.toString()));
		results.add(new BasicTextFieldSettingSpecifier("energycost", defaults.energycost.toString()));
		return results;
	}

	public Integer getMinwatts() {
		return minwatts;
	}

	public void setMinwatts(Integer minwatts) {
		if (watts < minwatts) {
			watts = minwatts;
		}
		this.minwatts = minwatts;
	}

	public Integer getMaxwatts() {
		return maxwatts;
	}

	public void setMaxwatts(Integer maxwatts) {
		if (watts > maxwatts) {
			watts = maxwatts;
		}
		this.maxwatts = maxwatts;
	}

	public Integer getEnergycost() {
		return energycost;
	}

	public void setEnergycost(Integer energycost) {
		this.energycost = energycost;
	}

	// protected as this is not set via the settings page but instead via demand
	// response
	protected void setWatts(Integer watts) {
		this.watts = watts;
	}

	public Integer getWatts() {
		return watts;
	}
}
