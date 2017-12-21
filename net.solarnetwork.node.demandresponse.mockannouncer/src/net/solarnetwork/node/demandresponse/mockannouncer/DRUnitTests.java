package net.solarnetwork.node.demandresponse.mockannouncer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import net.solarnetwork.node.demandresponse.battery.DRBattery;
import net.solarnetwork.node.demandresponse.battery.DRBatterySettings;
import net.solarnetwork.node.demandresponse.battery.MockBattery;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;

public class DRUnitTests {

	private DRAnouncer dra;
	private DRAnouncerSettings settings;

	@Before
	public void initMethod() {
		dra = new DRAnouncer();
		settings = new DRAnouncerSettings();
		dra.setSettings(settings);
		settings.setUid("DREngine");
	}

	@Test
	@Ignore
	// we can afford to charge a battery charge it
	public void testBatteryChargeTest() {
		DRBattery drb = new DRBattery();
		DRBatterySettings batterySettings = new DRBatterySettings();
		MockBattery battery = new MockBattery();
		batterySettings.setMockBattery(battery);
		drb.setDRBatterySettings(batterySettings);

		batterySettings.setBatteryCharge(0.0);
		batterySettings.setBatteryCost(1);
		batterySettings.setDrEngineName("DREngine");
		batterySettings.setBatteryCycles(100);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(100);

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(drb);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertFalse(drb.isDischarging());
		assertEquals((Object) battery.readDraw(), batterySettings.getMaxDraw());

	}

	@Test
	public void drdeviceReduceResponse() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(100000);
		device.setWatts(10);
		device.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(1);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 0, device.getWatts());
	}

	@Test
	public void drdeviceGainResponce() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(0);// device is free to use if you pay for its
								// energy source
		device.setWatts(1);
		device.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(10);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 10, device.getWatts());

	}

	@Test
	public void drdevicePartialGainResponce() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(1);
		device.setWatts(1);
		device.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(8);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 4, device.getWatts());

	}
}
