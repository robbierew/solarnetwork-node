package net.solarnetwork.node.demandresponse.battery;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.solarnetwork.node.job.SimpleManagedTriggerAndJobDetail;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.reactor.support.BasicInstructionStatus;

/**
 * 
 * @author robert
 *
 */
public class DRBattery extends SimpleManagedTriggerAndJobDetail
		implements DRChargeableDevice, FeedbackInstructionHandler {
	private DRBatterySettings settings;
	private Collection<InstructionHandler> instructionHandlers;

	@Override
	public boolean handlesTopic(String topic) {
		// instruction to shed load by a certain amount
		// TODO
		return true;
		// if (topic.equals(InstructionHandler.TOPIC_SHED_LOAD)) {
		// return true;
		// }
		// // insrtuction to set load to a certain percentage of max
		// if (topic.equals(InstructionHandler.TOPIC_DEMAND_BALANCE)) {
		// return true;
		// }
		//
		// // to be used for telling the battery whether charge or discharge
		// if (topic.equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
		// return true;
		// }
		// return false;
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		InstructionStatus status = processInstructionWithFeedback(instruction);
		return status.getAcknowledgedInstructionState();

	}

	@Override
	public InstructionStatus processInstructionWithFeedback(Instruction instruction) {
		InstructionState state;
		MockBattery battery = settings.getMockBattery();
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
			String param = instruction.getParameterValue(settings.getDrEngineName());
			if (param != null) {
				try {
					double value = Double.parseDouble(param);

					double draw = battery.readDraw() - value;
					if (true) {
						// TODO ensure maxdraw is met
						battery.setCharge(value);
						state = InstructionState.Completed;
					} else {
						state = InstructionState.Declined;
					}
				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}
		} else {
			state = InstructionState.Declined;
		}

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
			String param = instruction.getParameterValue("watts");
			String param2 = instruction.getParameterValue("discharge");
			if (param != null && param2 != null) {
				try {
					System.out.println("value is " + param);
					double value = Double.parseDouble(param);
					boolean discharge = Boolean.parseBoolean(param2);
					if (value < 0) {
						battery.setDraw(0.0);
					} else if (value > settings.getMaxDraw()) {
						if (discharge) {
							battery.setDraw(settings.getMaxDraw());
						} else {
							battery.setDraw(-settings.getMaxDraw());
						}

					} else {
						if (discharge) {
							battery.setDraw(value);
						} else {
							battery.setDraw(-value);
						}

					}
					state = InstructionState.Completed;
				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}
		}

		// if (state == InstructionState.Declined) {
		// battery.setCharge(4);
		// }

		InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date());

		return status;
	}

	public void setDRBatterySettings(DRBatterySettings settings) {
		this.settings = settings;
	}

	public DRBatterySettings getDRBatterySettings() {
		return settings;
	}

	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}

	@Deprecated
	private double batteryEnergyCost() {
		return settings.getBatteryCost() / (settings.getBatteryCycles() * settings.getBatteryMaxCharge() * 2);
	}

	@Override
	public Integer getEnergyCost() {
		// TODO Auto-generated method stub
		return (int) (settings.getBatteryCost().doubleValue()
				/ (settings.getBatteryCycles().doubleValue() * settings.getBatteryMaxCharge() * 2.0));

	}

	@Override
	public Integer getMaxPower() {
		// TODO fix this hack
		return (int) (double) settings.getMaxDraw();
	}

	@Override
	public Integer getMinPower() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer getWatts() {
		// TODO Auto-generated method stub
		return (int) Math.abs(settings.getMockBattery().readDraw());
	}

	@Override
	public Integer getCharge() {
		// TODO fix this hack
		return (int) settings.getMockBattery().readCharge();
	}

	@Override
	public Integer getMaxCharge() {
		// TODO fix this hack
		return (int) (double) settings.getBatteryMaxCharge();
	}

	@Override
	public String getUID() {
		return settings.getUID();
	}

	@Override
	public String getGroupUID() {
		return settings.getGroupUID();
	}

	@Override
	public Boolean isDischarging() {
		return settings.getMockBattery().readDraw() > 0;
	}

}
