package net.solarnetwork.node.demandresponse.minimumstrategy;

import java.util.List;

import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MinimumDRAnouncerSettings extends DatumDataSourceSupport implements SettingSpecifierProvider {

	private MinimumDRStrategy linkedInstance;

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
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();

		return results;
	}

	protected MinimumDRStrategy getLinkedInstance() {
		return linkedInstance;
	}

	protected void setLinkedInstance(MinimumDRStrategy linkedInstance) {
		this.linkedInstance = linkedInstance;
	}

}
