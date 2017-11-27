package net.solarnetwork.node.hw.deson.mock.meter;

import java.io.IOException;
import java.util.BitSet;
import java.util.Map;

import net.solarnetwork.node.LockTimeoutException;
import net.solarnetwork.node.io.modbus.JamodModbusConnection;
import net.wimpi.modbus.net.SerialConnection;

public class ModbusMock extends JamodModbusConnection {

	public ModbusMock() {
		super((SerialConnection) null, 1);
	}

	@Override
	public void open() throws IOException, LockTimeoutException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public BitSet readDiscreetValues(Integer[] addresses, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean writeDiscreetValues(Integer[] addresses, BitSet bits) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Integer> readInputValues(Integer[] addresses, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] readBytes(Integer address, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String readString(Integer address, int count, boolean trim, String charsetName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] readInts(Integer address, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short[] readSignedShorts(Integer address, int count) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer[] readValues(Integer address, int count) {
		// TODO Auto-generated method stub
		return null;
	}

}
