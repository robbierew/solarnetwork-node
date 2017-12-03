package net.solarnetwork.node.hw.deson.mock.meter;

import java.util.LinkedList;
import java.util.List;

import net.solarnetwork.node.io.modbus.ModbusConnection;

/**
 * 
 * @author robert
 * 
 *         helper class with static methods to get dummy data
 *
 */
public class DesonMockData {

	// singleton mock modbus to allow for the same mock to be used in different
	// classes
	public static final ModbusConnection CONN = new ModbusMock();

	// this data was taken from the deson test data
	public static final int[] TEST_DATA_30001_80 = bytesToModbusWords(new int[] { 0x43, 0x64, 0xB3, 0x33, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 7 */0x41, 0x00, 0x28, 0xF6, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, /* 13 */0xC4, 0xE5, 0x19, 0x9A, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 19 */0x44, 0xE5,
			0x1F, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 25 */0x41, 0x90, 0xCC, 0xCD, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 31 */
			0xBF, 0x7F, 0xFC, 0xCC, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, /* 71 */0x42, 0x47, 0xCC, 0xCD, /* 73 */0x3D, 0xC6, 0xA7,
			0xF0, /* 75 */0x3D, 0x13, 0x74, 0xBC, /* 77 */0x3C, 0x13, 0x74, 0xBC, /* 79 */0x00, 0x00, 0x00, 0x00, });

	/**
	 * Convert an array of 8-bit numbers to 16-bit numbers, by combining pairs
	 * of bytes in big-endian order.
	 * 
	 * @param bytes
	 *            The bytes to combine into words.
	 * @return The array of words.
	 */
	private static final int[] bytesToModbusWords(int[] bytes) {
		// convert raw bytes into 16-bit modbus integers
		int[] ints = new int[bytes.length / 2];
		for (int i = 0, j = 0; i < bytes.length; i += 2, j += 1) {
			ints[j] = ((bytes[i] << 8) | bytes[i + 1]);
		}
		return ints;
	}

	/**
	 * 
	 * @return test data with small changes
	 */
	public static int[] getData() {
		int[] copy = new int[TEST_DATA_30001_80.length];
		System.arraycopy(TEST_DATA_30001_80, 0, copy, 0, TEST_DATA_30001_80.length);
		// add small changes to the addresses we are looking at

		// these are the important addr addresses that actualy get read
		// only these addresses get altered
		List<Integer> importantAddr = new LinkedList<Integer>();
		importantAddr.add(SDM120Data.ADDR_DATA_ACTIVE_ENERGY_IMPORT_TOTAL);
		importantAddr.add(SDM120Data.ADDR_DATA_APPARENT_POWER);
		importantAddr.add(SDM120Data.ADDR_DATA_ACTIVE_ENERGY_IMPORT_TOTAL);
		importantAddr.add(SDM120Data.ADDR_DATA_POWER_FACTOR);
		importantAddr.add(SDM120Data.ADDR_DATA_V_NEUTRAL);

		for (Integer i : importantAddr) {
			// MSBs gets less of a shift
			copy[i] = copy[i] + (int) (4 * Math.sin(Math.random() * 2 * Math.PI));

			// we read the ajancent address aswell so give it a look
			copy[i + 1] = copy[i + 1] + (int) (30 * Math.sin(Math.random() * 2 * Math.PI));
		}

		return copy;
	}

}
