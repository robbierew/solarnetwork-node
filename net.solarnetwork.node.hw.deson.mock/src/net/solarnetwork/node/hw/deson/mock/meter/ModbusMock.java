package net.solarnetwork.node.hw.deson.mock.meter;

import java.io.IOException;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Map;

import net.solarnetwork.node.io.modbus.ModbusConnection;

/**
 * 
 * @author robert
 * 
 *         Mock modbusconnection that returns dummy data, dummy data is received
 *         from DesonMockData
 *
 */
public class ModbusMock implements ModbusConnection {

	private final int unitId = 1;

	@Override
	public String toString() {

		return "Modbus Mock";
	}

	@Override
	public final int getUnitId() {
		return unitId;
	}

	@Override
	public void open() throws IOException {

	}

	@Override
	public void close() {

	}

	/**
	 * tbh I don't know what this is for
	 */
	@Override
	public BitSet readDiscreetValues(Integer[] addresses, int count) {
		return new BitSet(5);
	}

	@Override
	public Boolean writeDiscreetValues(Integer[] addresses, BitSet bits) {
		return true;
	}

	/**
	 * maps the dummy values
	 */
	@Override
	public Map<Integer, Integer> readInputValues(Integer[] addresses, int count) {
		Map<Integer, Integer> data = new Hashtable<Integer, Integer>();
		int addr = 0;
		for (int i : DesonMockData.getData()) {
			data.put(addr, i);
			addr++;
		}
		return data;
	}

	@Override
	public byte[] readBytes(Integer address, int count) {
		byte[] result = new byte[count];
		for (int i = 0; i < count; i++) {
			result[i] = (byte) DesonMockData.getData()[address + i];
		}
		return result;
	}

	@Override
	public String readString(Integer address, int count, boolean trim, String charsetName) {
		return "This is a read String of data";
	}

	@Override
	public int[] readInts(Integer address, int count) {
		int[] result = new int[count];
		for (int i = 0; i < count; i++) {
			result[i] = DesonMockData.getData()[address + i];
		}
		return result;
	}

	@Override
	public short[] readSignedShorts(Integer address, int count) {
		short[] result = new short[count];
		for (int i = 0; i < count; i++) {
			result[i] = (short) DesonMockData.getData()[address + i];
		}
		return result;

	}

	@Override
	public Integer[] readValues(Integer address, int count) {
		Integer[] result = new Integer[count];
		for (int i = 0; i < count; i++) {
			result[i] = DesonMockData.getData()[address + i];
		}
		return result;
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

}
