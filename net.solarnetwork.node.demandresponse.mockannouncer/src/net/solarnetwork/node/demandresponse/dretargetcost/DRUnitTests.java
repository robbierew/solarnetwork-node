package net.solarnetwork.node.demandresponse.dretargetcost;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import net.solarnetwork.node.demandresponse.battery.DRBattery;
import net.solarnetwork.node.demandresponse.battery.DRBatterySettings;
import net.solarnetwork.node.demandresponse.battery.MockBattery;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;

public class DRUnitTests {

	private DRETargetCost dra;
	private DRETargetCostDatumDataSource settings;

	@Before
	public void initMethod() {
		dra = new DRETargetCost();
		settings = new DRETargetCostDatumDataSource();
		dra.setSettings(settings);
		settings.setUid("DREngine");
	}

	@Test
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
		batterySettings.setBatteryCycles(1000);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(100);

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(drb);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();
		System.out.println("testing" + drb.getEnergyCost());
		assertFalse(drb.isDischarging());
		assertEquals(drb.getMaxPower(), drb.getWatts());

	}

	@Test
	// One device too expensive to run and has to be turned off
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
	// we are under budget increase demand
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
	// we are under budget slightly, increase a small amount of demand
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

	@Test
	// slightly over budget decrease demand a little bit
	public void drdevicePartialDropResponse() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(1);
		device.setWatts(8);
		device.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(8);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 4, device.getWatts());
	}

	@Test
	// we are over budget and can decrease demand but will be still over budget
	// (nothing can be done to go in budget)
	public void dropButStillOverTarget() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(2);
		device.setWatts(10);
		device.setDREngineName("DREngine");
		device.setMinPower(5);

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(8);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 5, device.getWatts());
	}

	@Test
	// (note it is expected behavior from my implementation to reduce demand of
	// the most expensive devices first) We are over budget but can be solved by
	// reducing demand of one device.
	public void twoDRDevicesApplyOneReduce() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(2);
		device.setWatts(10);
		device.setDREngineName("DREngine");

		DRDeviceMock device2 = new DRDeviceMock();
		device2.setEnergyCost(1);
		device2.setWatts(10);
		device2.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);
		handlers.add(device2);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(20);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();
		assertEquals((Object) 0, device.getWatts());
		assertEquals((Object) 10, device2.getWatts());
	}

	@Test
	// (note it is expected behavior from my implementation to reduce demand of
	// the most expensive devices first) In this test both devices need to
	// reduce demand to stop being over budget.
	public void twoDRDevicesApplyTwoReduce() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(2);
		device.setWatts(10);
		device.setDREngineName("DREngine");
		device.setMinPower(8);

		DRDeviceMock device2 = new DRDeviceMock();
		device2.setEnergyCost(1);
		device2.setWatts(10);
		device2.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);
		handlers.add(device2);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(40);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();
		assertEquals((Object) 8, device.getWatts()); // (8*3 = 24)
		assertEquals((Object) 8, device2.getWatts());// (8*2 = 16) total 40
	}

	@Test
	// (note it is expected behavior of my implementation to increase demand of
	// the cheapest device first) We are under budget but can only increase
	// demand to one device.
	public void twoDRDevicesApplyOneIncrease() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(2);
		device.setWatts(1);
		device.setDREngineName("DREngine");

		DRDeviceMock device2 = new DRDeviceMock();
		device2.setEnergyCost(1);
		device2.setWatts(1);
		device2.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);
		handlers.add(device2);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(19);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();
		assertEquals((Object) 1, device.getWatts()); // (1*3 = 3)
		assertEquals((Object) 8, device2.getWatts());// (8*2 = 16) total 19
	}

	@Test
	// (note it is expected behavior of my implementation to increase demand of
	// the cheapest device first) increase demand of two devices.
	public void twoDRDevicesApplyTwoIncrease() {
		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(2);
		device.setWatts(1);
		device.setDREngineName("DREngine");

		DRDeviceMock device2 = new DRDeviceMock();
		device2.setEnergyCost(1);
		device2.setWatts(1);
		device2.setDREngineName("DREngine");

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);
		handlers.add(device2);

		settings.setEnergyCost(1);
		settings.setDrtargetCost(48);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();
		assertEquals((Object) 9, device.getWatts()); // (9*3 = 27)
		assertEquals((Object) 10, device2.getWatts());// (10*2 = 20) total 47
	}

	@Test
	// Energy is too expensive but battery cheap use it instead
	public void powerDeviceByBattery() {
		DRBattery drb = new DRBattery();
		DRBatterySettings batterySettings = new DRBatterySettings();
		MockBattery battery = new MockBattery();
		batterySettings.setMockBattery(battery);
		drb.setDRBatterySettings(batterySettings);

		batterySettings.setBatteryCharge(10.0);
		batterySettings.setBatteryCost(1);
		batterySettings.setDrEngineName("DREngine");
		batterySettings.setBatteryCycles(1000);

		DRDeviceMock device = new DRDeviceMock();
		device.setEnergyCost(1);
		device.setWatts(10);
		device.setDREngineName("DREngine");
		device.setMinPower(8);

		Collection<FeedbackInstructionHandler> handlers = new ArrayList<FeedbackInstructionHandler>();
		handlers.add(device);
		handlers.add(drb);

		settings.setEnergyCost(10);
		settings.setDrtargetCost(10);

		dra.setFeedbackInstructionHandlers(handlers);

		dra.drupdate();

		assertEquals((Object) 8, device.getWatts());
		assertEquals((Object) 8, drb.getWatts());

	}
}
