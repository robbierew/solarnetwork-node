package net.solarnetwork.node.demandresponse.battery;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

public class DRBatterySettings extends DatumDataSourceSupport implements SettingSpecifierProvider {

	private Double maxCharge = 10.0;
	private Double charge = 10.0;
	private MockBattery mockbattery;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;
	private Integer cost = 1000;
	private Integer cycles = 10000;

	private boolean drawOverride = false;

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
		DRBatterySettings defaults = new DRBatterySettings();
		results.add(new BasicTextFieldSettingSpecifier("poweredDevices.propertyFilters['UID']", "Main"));
		results.add(new BasicTextFieldSettingSpecifier("poweredDevices.propertyFilters['groupUID']", ""));
		results.add(new BasicTextFieldSettingSpecifier("batteryMaxCharge", defaults.maxCharge.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCharge", defaults.charge.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCost", defaults.cost.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCycles", defaults.cycles.toString()));
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
		return charge;
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

	public void setPoweredDevices(OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums) {
		this.poweredDevices = powerDatums;

	}

	public OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> getPoweredDevices() {
		return poweredDevices;
	}

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	public Integer getCycles() {
		return cycles;
	}

	public void setCycles(Integer cycles) {
		this.cycles = cycles;
	}

	// These methods are called from DRBattery to set state of DR events

	protected boolean isDrawOverride() {
		return drawOverride;
	}

	protected void setDrawOverride(boolean drawOverride) {
		this.drawOverride = drawOverride;
	}

}
