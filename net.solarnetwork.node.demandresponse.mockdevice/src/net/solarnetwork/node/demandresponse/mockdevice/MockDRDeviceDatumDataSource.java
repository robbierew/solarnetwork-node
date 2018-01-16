package net.solarnetwork.node.demandresponse.mockdevice;

import java.util.Date;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeACEnergyDatum;

public class MockDRDeviceDatumDataSource implements DatumDataSource<GeneralNodeACEnergyDatum> {

	private MockDRDeviceSettings settings;

	@Override
	public String getUID() {
		// TODO Auto-generated method stub
		return settings.getUID();
	}

	@Override
	public String getGroupUID() {
		// TODO Auto-generated method stub
		return settings.getGroupUID();
	}

	@Override
	public Class<? extends GeneralNodeACEnergyDatum> getDatumType() {
		// TODO Auto-generated method stub
		return GeneralNodeACEnergyDatum.class;
	}

	@Override
	public GeneralNodeACEnergyDatum readCurrentDatum() {
		GeneralNodeACEnergyDatum datum = new GeneralNodeACEnergyDatum();
		datum.setCreated(new Date());
		datum.setSourceId(settings.getUID());
		datum.setWatts(settings.getWatts());

		return datum;
	}

	public void setSettings(MockDRDeviceSettings settings) {
		this.settings = settings;
	}

	public MockDRDeviceSettings getSettings() {
		return settings;
	}

}
