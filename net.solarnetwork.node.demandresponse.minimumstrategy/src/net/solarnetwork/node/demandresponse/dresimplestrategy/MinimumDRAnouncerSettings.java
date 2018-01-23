package net.solarnetwork.node.demandresponse.dresimplestrategy;

import java.util.List;

import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class MinimumDRAnouncerSettings extends DatumDataSourceSupport implements SettingSpecifierProvider {

	private DRESimpleStrategy linkedInstance;

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

	protected DRESimpleStrategy getLinkedInstance() {
		return linkedInstance;
	}

	protected void setLinkedInstance(DRESimpleStrategy linkedInstance) {
		this.linkedInstance = linkedInstance;
	}

}
