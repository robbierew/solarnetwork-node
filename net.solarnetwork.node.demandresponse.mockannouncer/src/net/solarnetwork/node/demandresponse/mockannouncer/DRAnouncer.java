package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.demandresponse.battery.DRChargeableDevice;
import net.solarnetwork.node.demandresponse.battery.DRDevice;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.support.BasicInstruction;
import net.solarnetwork.util.OptionalServiceCollection;

public class DRAnouncer {
	private DRAnouncerSettings settings;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;
	private Collection<FeedbackInstructionHandler> feedbackInstructionHandlers;
	private Collection<InstructionHandler> instructionHandlers;

	public DRAnouncerSettings getSettings() {
		return settings;
	}

	public void setSettings(DRAnouncerSettings settings) {
		this.settings = settings;
		settings.setLinkedInstance(this);
	}

	// TODO give this method a massive refactoring
	protected void drupdate() {
		Integer sum = 0;
		// Iterable<DatumDataSource<? extends EnergyDatum>> datumIterable =
		// settings.getPoweredDevices().services();
		//
		// for (DatumDataSource<? extends EnergyDatum> d : datumIterable) {
		//
		// EnergyDatum datum = d.readCurrentDatum();
		//
		// // not all readings have a datum or a wattage reading which is why
		// // one must check for nulls
		// if (datum != null) {
		// Integer reading = datum.getWatts();
		// if (reading != null) {
		// sum += reading;
		// }
		// }
		// }

		// Integer currentCost = sum * settings.getEnergyCost();
		System.out.println("There are " + feedbackInstructionHandlers.size() + "fbih");
		List<InstructionHandler> drdevices = new ArrayList<InstructionHandler>();
		for (FeedbackInstructionHandler handler : feedbackInstructionHandlers) {

			if (handler.handlesTopic("getDRDeviceInstance")) {
				BasicInstruction instr = new BasicInstruction("getDRDeviceInstance", new Date(),
						Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
				instr.addParameter(settings.getUID(), "");
				System.out.println("before cast");
				DRDevice instance;

				Object test = handler.processInstructionWithFeedback(instr).getResultParameters();
				if (test != null) {
					test = handler.processInstructionWithFeedback(instr).getResultParameters().get("instance");
				}

				if (test instanceof DRDevice) {
					System.out.println("got instance");
					if (test instanceof DRChargeableDevice) {
						System.out.println("is also chargealbe");
					}
					instance = (DRDevice) test;

				} else if (test != null) {
					System.out.println(test.getClass().toString());
				}
				System.out.println("asked for instance");
				if (/** instance != null **/
				true) {

					instr = new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
							Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
					instr.addParameter(settings.getUID(), (new Double(10.0 * Math.random())).toString());
					handler.processInstruction(instr);
				}
			}
			System.out.println("My first debug print statment");
			drdevices.add(handler);

		}
		// for now lets just see if this works
		// settings.getEnergyCost() < sum
		// if (true) {
		// for (InstructionHandler handler : drdevices) {
		// BasicInstruction instr = new
		// BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
		// Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID,
		// null);
		// instr.addParameter(settings.getUID(), "10.0");
		// handler.processInstruction(instr);
		// }
		// }

	}

	@Deprecated
	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	@Deprecated
	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}

	public Collection<FeedbackInstructionHandler> getFeedbackInstructionHandlers() {
		return feedbackInstructionHandlers;
	}

	public void setFeedbackInstructionHandlers(Collection<FeedbackInstructionHandler> feedbackInstructionHandlers) {
		this.feedbackInstructionHandlers = feedbackInstructionHandlers;
	}
}
