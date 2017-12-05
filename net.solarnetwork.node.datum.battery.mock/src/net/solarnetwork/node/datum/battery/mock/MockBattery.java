package net.solarnetwork.node.datum.battery.mock;

public class MockBattery {
	private double maxcapacity;
	private double charge;
	private double draw;
	private double maxdraw;
	private long lastsample;

	public MockBattery(double maxcapacity, double maxdraw) {
		if (maxcapacity < 0 || maxdraw < 0) {
			throw new IllegalArgumentException();
		}
		this.lastsample = readTime();
		this.maxcapacity = maxcapacity;
		this.maxdraw = maxdraw;
		setCharge(0);
		setDraw(0);
	}

	public void setCharge(double charge) {
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
		return (float) (readCharge() / this.maxcapacity);
	}

	public void setDraw(double draw) {
		readCharge();
		if (Math.abs(draw) <= this.maxdraw) {
			this.draw = draw;
		}
	}

	public long readTime() {
		return System.currentTimeMillis();
	}
}
