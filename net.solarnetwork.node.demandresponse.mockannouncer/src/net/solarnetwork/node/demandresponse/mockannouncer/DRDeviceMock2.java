package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.solarnetwork.node.demandresponse.battery.DRDevice;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.reactor.support.BasicInstructionStatus;

public class DRDeviceMock2 implements DRDevice, FeedbackInstructionHandler {

	private String uid = "mockDR";
	private String groupuid = "";
	private String drenginename = "";
	private Integer energyCost = 1;
	private Integer maxPower = 10;
	private Integer minPower = 0;
	private Integer watts = 0;

	@Override
	public String getUID() {
		return uid;
	}

	public void setUID(String uid) {
		this.uid = uid;
	}

	@Override
	public String getGroupUID() {
		// TODO Auto-generated method stub
		return groupuid;
	}

	public void setGroupUID(String groupuid) {
		this.groupuid = groupuid;
	}

	public String getDREngineName() {
		return drenginename;
	}

	public void setDREngineName(String drenginename) {
		this.drenginename = drenginename;
	}

	@Override
	public boolean handlesTopic(String topic) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		InstructionStatus status = processInstructionWithFeedback(instruction);
		return status.getAcknowledgedInstructionState();
	}

	@Override
	public InstructionStatus processInstructionWithFeedback(Instruction instruction) {
		InstructionState state;
		if (instruction.getTopic().equals("getDRDeviceInstance")) {
			// for now lets just get this working
			state = InstructionState.Completed;
			Map<String, Object> map = new Hashtable<String, Object>(1);
			map.put("instance", this);

			InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date(), null, map);
			return status;
			// DEBUG TODO
			// settings.setBatteryCharge(5.0);
		}

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			String param = instruction.getParameterValue(drenginename);
			if (param != null) {
				try {
					double value = Double.parseDouble(param) + 0.5;// 0.5 for
																	// rounding
					watts = ((int) value > watts) ? 0 : watts - (int) value;
					state = InstructionState.Completed;
				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}

		} else if (instruction.getTopic().equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
			String param = instruction.getParameterValue("watts");
			if (param != null) {
				try {
					double value = Double.parseDouble(param);
					if (value < 0) {
						watts = 0;
					} else if (value > maxPower) {
						watts = maxPower;
					} else {
						watts = (int) value;
					}
					state = InstructionState.Completed;
				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}
		} else {
			state = InstructionState.Declined;
		}

		InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date());

		return status;
	}

	@Override
	public Integer getEnergyCost() {
		// TODO Auto-generated method stub
		return energyCost;
	}

	public void setEnergyCost(Integer energyCost) {
		this.energyCost = energyCost;
	}

	@Override
	public Integer getMaxPower() {
		// TODO Auto-generated method stub
		return maxPower;
	}

	public void setMaxPower(Integer maxPower) {
		this.maxPower = maxPower;
	}

	@Override
	public Integer getMinPower() {
		// TODO Auto-generated method stub
		return minPower;
	}

	public void setMinPower(Integer minPower) {
		this.minPower = minPower;
	}

	@Override
	public Integer getWatts() {
		// TODO Auto-generated method stub
		return watts;
	}

	public void setWatts(Integer watts) {
		this.watts = watts;
	}

}
