package net.solarnetwork.node.datum.battery.mock;

public interface DRAnnouncer {
	public boolean subscribe(DRHandler handler);

	public boolean unsucscribe(DRHandler handler);

}
