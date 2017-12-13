package net.solarnetwork.node.datum.battery.mock.test;

import net.solarnetwork.node.datum.battery.mock.DRAnnouncer;
import net.solarnetwork.node.datum.battery.mock.DRHandler;
import net.solarnetwork.node.datum.battery.mock.DRMessage;
import net.solarnetwork.node.datum.battery.mock.MockBattery;

public class DRScenario {
	// place for me to collect my thoughts

	private class MockBatteryDR extends MockBattery implements DRAnnouncer, DRHandler {

		public MockBatteryDR(double maxcapacity) {
			super(maxcapacity);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void receiveMessage(DRMessage message) {
			// TODO Auto-generated method stub

		}

		@Override
		public void subscribe(DRHandler handler) {
			// TODO Auto-generated method stub

		}

		@Override
		public void unsucscribe(DRHandler handler) {
			// TODO Auto-generated method stub

		}

	}

	private class MockDRHandler implements DRHandler {

		@Override
		public void receiveMessage(DRMessage message) {
			// TODO Auto-generated method stub

		}

	}

	private class MockDRAnnouncer implements DRAnnouncer {

		@Override
		public void subscribe(DRHandler handler) {
			// TODO Auto-generated method stub

		}

		@Override
		public void unsucscribe(DRHandler handler) {
			// TODO Auto-generated method stub

		}

	}

	public static void main(String[] args) {
		DRScenario drs = new DRScenario();
		MockBatteryDR bat = drs.new MockBatteryDR(10);
		MockDRHandler[] devices = new MockDRHandler[5];
		MockDRAnnouncer root = drs.new MockDRAnnouncer();
		for (int i = 0; i < 5; i++) {
			devices[i] = drs.new MockDRHandler();
			bat.subscribe(devices[i]);
		}
		root.subscribe(bat);

	}
}
