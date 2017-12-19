package net.solarnetwork.node.demandresponse.battery;

import java.util.Collection;
import java.util.Date;

import net.solarnetwork.node.job.SimpleManagedTriggerAndJobDetail;
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
public class DRBattery extends SimpleManagedTriggerAndJobDetail implements DRDevice {
	private DRBatterySettings settings;
	private Collection<InstructionHandler> instructionHandlers;

	@Override
	public boolean handlesTopic(String topic) {
		// instruction to shed load by a certain amount
		if (topic.equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			return true;
		}
		// insrtuction to set load to a certain percentage of max
		if (topic.equals(InstructionHandler.TOPIC_DEMAND_BALANCE)) {
			return true;
		}

		// to be used for telling the battery whether charge or discharge
		if (topic.equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
			return true;
		}
		return false;
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		InstructionStatus status = processInstructionWithFeedback(instruction);
		return status.getAcknowledgedInstructionState();

	}

	@Override
	public InstructionStatus processInstructionWithFeedback(Instruction instruction) {
		InstructionState state;

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			String param = instruction.getParameterValue("shedAmount");
			if (param != null) {
				try {
					double value = Double.parseDouble(param);
					MockBattery battery = settings.getMockBattery();
					double draw = battery.readDraw() - value;
					if (value == 0.0) {
						settings.setDrawOverride(false);
					}
					if (draw >= 0.0) {
						battery.setDraw(0.0);
						settings.setDrawOverride(true);
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

	private double batteryEnergyCost() {
		return settings.getCost() / (settings.getCycles() * settings.getBatteryMaxCharge() * 2);
	}

	@Override
	public Integer getEnergyCost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getMaxPower() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getMinPower() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean isToggleDevice() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Integer getWatts() {
		// TODO Auto-generated method stub
		return null;
	}

}
