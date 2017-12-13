package net.solarnetwork.node.datum.battery.mock;

public interface DRAnnouncer {
	public void subscribe(DRHandler handler);

	public void unsucscribe(DRHandler handler);

}
