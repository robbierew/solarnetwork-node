package net.solarnetwork.node.demandresponse.battery;

public class MockBattery {
	private Double maxcapacity = null;
	private double charge;
	private double draw;
	private long lastsample;

	public MockBattery(double maxcapacity) {
		if (maxcapacity < 0) {
			throw new IllegalArgumentException();
		}
		this.maxcapacity = maxcapacity;
		setCharge(0);
		setDraw(0);
	}

	public MockBattery() {

		// TODO change how this works
		this(10.0);
	}

	public void setMax(double maxcapacity) {
		// you cannot have no or negative capacity keep current value if
		// argument is invalid
		if (maxcapacity > 0) {
			this.maxcapacity = maxcapacity;
		}
	}

	public void setCharge(double charge) {
		this.lastsample = readTime();
		// can't have negative charge,if that happens we keep the current value
		if (charge >= 0) {
			this.charge = Math.min(charge, this.maxcapacity);

		}

	}

	private double deltaTimeHours() {
		long oldtime = this.lastsample;
		long currenttime = readTime();
		this.lastsample = currenttime;
		double delta = currenttime - oldtime;
		delta = delta / 1000 / 60 / 60;
		return delta;
	}

	public double readCharge() {
		if (this.maxcapacity == null) {
			throw new RuntimeException();
		}
		double delta = deltaTimeHours();
		double newcharge = this.charge - this.draw * delta;
		if (newcharge < 0) {
			newcharge = 0;
		}
		if (newcharge > this.maxcapacity) {
			newcharge = this.maxcapacity;
		}
		setCharge(newcharge);
		return newcharge;
	}

	public double readDraw() {
		if (this.maxcapacity == null) {
			throw new RuntimeException();
		}
		if (this.draw < 0 && this.charge == this.maxcapacity) {
			// can't charge what is already fully charged
			return 0;
		} else if (this.draw > 0 && this.charge == 0) {
			// can't draw from empty battery
			return 0;
		} else {
			return this.draw;
		}
	}

	public float percentageCapacity() {
		if (this.maxcapacity == null) {
			throw new RuntimeException();
		}
		return (float) (readCharge() / this.maxcapacity);
	}

	public void setDraw(double draw) {
		readCharge();
		this.draw = draw;
	}

	public long readTime() {
		return System.currentTimeMillis();
	}
}
