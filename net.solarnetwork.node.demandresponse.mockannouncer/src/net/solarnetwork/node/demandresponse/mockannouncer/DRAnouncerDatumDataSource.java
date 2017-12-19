package net.solarnetwork.node.demandresponse.mockannouncer;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyStorageDatum;

public class DRAnouncerDatumDataSource implements DatumDataSource<GeneralNodeEnergyStorageDatum> {

	private DRAnouncerSettings settings;

	@Override
	public String getUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroupUID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<? extends GeneralNodeEnergyStorageDatum> getDatumType() {
		// TODO Auto-generated method stub
		return GeneralNodeEnergyStorageDatum.class;
	}

	@Override
	public GeneralNodeEnergyStorageDatum readCurrentDatum() {
		// TODO Auto-generated method stub
		return null;
	}

	public DRAnouncerSettings getSettings() {
		return settings;
	}

	public void setSettings(DRAnouncerSettings settings) {
		this.settings = settings;

	}

}
