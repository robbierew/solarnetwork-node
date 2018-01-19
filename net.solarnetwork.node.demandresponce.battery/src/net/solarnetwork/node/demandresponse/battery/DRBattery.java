package net.solarnetwork.node.demandresponse.battery;

import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.solarnetwork.node.demandresponse.mockannouncer.DRSupportTools;
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
public class DRBattery extends SimpleManagedTriggerAndJobDetail implements FeedbackInstructionHandler {
	private Collection<InstructionHandler> instructionHandlers;

	@Override
	public boolean handlesTopic(String topic) {
		if (topic.equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			return true;
		}
		if (topic.equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
			return true;
		}
		if (topic.equals(DRSupportTools.DRPARAMS_INSTRUCTION)) {
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
		DRBatteryDatumDataSource settings = getDRBatterySettings();
		InstructionState state;
		MockBattery battery = settings.getMockBattery();
		if (instruction.getTopic().equals(DRSupportTools.DRPARAMS_INSTRUCTION)) {
			// for now lets just get this working
			state = InstructionState.Completed;
			Map<String, Object> map = new Hashtable<String, Object>();
			map.put(DRSupportTools.DRCAPABLE_PARAM, "true");
			map.put(DRSupportTools.WATTS_PARAM, new Double(Math.abs(settings.getMockBattery().readDraw())).toString());
			map.put(DRSupportTools.CHARGEABLE_PARAM, "true");
			map.put(DRSupportTools.DISCHARGING_PARAM, new Boolean(settings.getMockBattery().readDraw() > 0).toString());

			// A Battery only has a limited number of charge and discharge
			// cycles. The price of using energy is related to the cost of the
			// battery and the number of cycles it can do and the maximum
			// capacity of a charge.
			map.put(DRSupportTools.ENERGYCOST_PARAM,
					new Integer((int) (settings.getBatteryCost().doubleValue()
							/ (settings.getBatteryCycles().doubleValue() * settings.getBatteryMaxCharge() * 2.0)))
									.toString());

			map.put(DRSupportTools.MINWATTS_PARAM, "0");
			map.put(DRSupportTools.MAXWATTS_PARAM, settings.getMaxDraw().toString());
			InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date(), null, map);
			return status;

		}

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			String param = instruction.getParameterValue(settings.getDrEngineName());
			if (param != null) {
				try {
					double value = Double.parseDouble(param);
					// TODO how will this instruction be used becasue remember
					// readDraw is negative sometimes
					double draw = battery.readDraw() - value;
					// TODO why is this here
					if (draw >= 0) {

						battery.setCharge(draw);
						state = InstructionState.Completed;
					} else {
						// if
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

		// TODO this does not follow my current conventions for charging
		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {

			// TODO update DRSupportTools to take better advantage of this stuff
			String param = instruction.getParameterValue(DRSupportTools.WATTS_PARAM);
			String param2 = instruction.getParameterValue(DRSupportTools.DISCHARGING_PARAM);

			if (param != null && param2 != null) {
				try {
					// TODO remove debug statement
					System.out.println("value is " + param);

					// TODO need to decide of datatype conventions
					double value = Double.parseDouble(param);
					boolean discharge = Boolean.parseBoolean(param2);

					if (value < 0) {
						// negative value does not make sence decline rather
						// than assume positive
						state = InstructionState.Declined;

					} else if (value > settings.getMaxDraw()) {
						// the implementation of mockbattery has a negative draw
						// as charging and positive draw as discharging
						if (discharge) {
							battery.setDraw(settings.getMaxDraw());
						} else {
							battery.setDraw(-settings.getMaxDraw());
						}
						state = InstructionState.Completed;

					} else {
						// the implementation of mockbattery has a negative draw
						// as charging and positive draw as discharging
						if (discharge) {
							battery.setDraw(value);
						} else {
							battery.setDraw(-value);
						}
						state = InstructionState.Completed;

					}

				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}
		}

		InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date());

		return status;
	}

	public DRBatteryDatumDataSource getDRBatterySettings() {
		return (DRBatteryDatumDataSource) getSettingSpecifierProvider();
	}

	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}

}
