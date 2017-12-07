package net.solarnetwork.node.datum.battery.mock;

import java.util.HashMap;
import java.util.Map;

public class DRAnnouncerDirectory {
	private Map<String, DRAnnouncer> draMap;
	private static DRAnnouncerDirectory root;

	public static DRAnnouncerDirectory getRootDRAnnouncerDirectory() {
		if (root == null) {
			root = new DRAnnouncerDirectory();
		}
		return root;
	}

	public DRAnnouncerDirectory() {
		draMap = new HashMap<String, DRAnnouncer>();
	}

	public DRAnnouncer getDRAnnouncer(String name) {
		return draMap.get(name);
	}

	public void addDRAnnouncer(DRAnnouncer dra, String name) {
		draMap.put(name, dra);
	}

}
