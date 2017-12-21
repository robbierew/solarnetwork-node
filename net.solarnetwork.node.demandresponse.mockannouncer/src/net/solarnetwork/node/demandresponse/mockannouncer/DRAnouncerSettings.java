package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

public class DRAnouncerSettings extends DatumDataSourceSupport implements SettingSpecifierProvider {

	private Integer energyCost = 10;
	private DRAnouncer linkedInstance;

	// TODO refactor how linkinstance works

	// we will use this value as a means to calebrate the DR a lower value means
	// more likly to turn things off. The goal is to get the cost of powering
	// devices as close to this value without going over.
	private Integer drtargetCost = 10;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return "net.solarnetwork.node.demandresponse.mockannouncer";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "DR Engine";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();
		DRAnouncerSettings defaults = new DRAnouncerSettings();
		results.add(new BasicTextFieldSettingSpecifier("energyCost", defaults.energyCost.toString()));
		results.add(new BasicTextFieldSettingSpecifier("drtargetCost", defaults.drtargetCost.toString()));
		results.add(new BasicTextFieldSettingSpecifier("poweredDevices.propertyFilters['UID']", "Main"));
		results.add(new BasicTextFieldSettingSpecifier("poweredDevices.propertyFilters['groupUID']", ""));

		return results;
	}

	public void setPoweredDevices(OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums) {
		this.poweredDevices = powerDatums;
		if (linkedInstance != null) {
			// linkedInstance.drupdate();
		}

	}

	public OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> getPoweredDevices() {
		return poweredDevices;
	}

	public Integer getEnergyCost() {
		return energyCost;
	}

	public void setEnergyCost(Integer energyCost) {
		this.energyCost = energyCost;
		if (linkedInstance != null) {
			// linkedInstance.drupdate();
		}

	}

	public Integer getDrtargetCost() {
		return drtargetCost;
	}

	public void setDrtargetCost(Integer drtargetCost) {
		this.drtargetCost = drtargetCost;
		if (linkedInstance != null) {
			// linkedInstance.drupdate();
		}

	}

	protected DRAnouncer getLinkedInstance() {
		return linkedInstance;
	}

	protected void setLinkedInstance(DRAnouncer linkedInstance) {
		this.linkedInstance = linkedInstance;
	}

}
