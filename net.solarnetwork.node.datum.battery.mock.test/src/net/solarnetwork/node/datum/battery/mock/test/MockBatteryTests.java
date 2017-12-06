package net.solarnetwork.node.datum.battery.mock.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import net.solarnetwork.node.datum.battery.mock.MockBattery;

public class MockBatteryTests {

	private long forcedTime;
	private final long oneHour = 3600000L;
	private final long twoHours = 7200000L;
	private MockBattery mb;

	// improved controllability of mockbattery by being able to control passage
	// of time
	private class TestBattery extends MockBattery {

		public TestBattery(double maxcapacity) {
			super(maxcapacity);

		}

		@Override
		public long readTime() {
			return forcedTime;
		}

	}

	@Before
	public void initTime() {
		forcedTime = 0L;
		mb = new TestBattery(10);
	}

	@Test
	public void testNegativeContructor() {
		try {
			new TestBattery(-1);
			fail();
		} catch (IllegalArgumentException e) {

		}

	}

	@Test
	public void testEmptyingBattery() {
		mb.setCharge(1);
		mb.setDraw(1);
		forcedTime = oneHour;
		assertEquals(0.0, (Object) mb.readCharge());
		forcedTime = twoHours;
		// cannot drain any more
		assertEquals(0.0, (Object) mb.readCharge());
	}

	@Test
	public void testDrawOnEmptyBattery() {
		mb.setCharge(0);
		mb.setDraw(1);
		assertEquals(0.0, (Object) mb.readDraw());
		mb.setDraw(-1);
		assertEquals(-1.0, (Object) mb.readDraw());
	}

	@Test
	public void testFullyChargingBattery() {
		mb.setCharge(9);
		mb.setDraw(-1);
		forcedTime = oneHour;
		assertEquals(10.0, (Object) mb.readCharge());
		forcedTime = twoHours;
		assertEquals(10.0, (Object) mb.readCharge());

	}

	@Test
	public void testDrawOnFullBattery() {
		mb.setCharge(10);
		mb.setDraw(-1);
		// can't charge full battery
		assertEquals(0.0, (Object) mb.readDraw());
		mb.setDraw(1);
		assertEquals(1.0, (Object) mb.readDraw());
	}

	@Test
	public void testChangingDrawLevelsBetweenReads() {
		mb.setCharge(10);
		mb.setDraw(1);
		forcedTime = oneHour;
		mb.setDraw(2);
		forcedTime = twoHours;
		assertEquals(7.0, (Object) mb.readCharge());
	}
}
