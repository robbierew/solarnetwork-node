package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.support.BasicInstruction;
import net.solarnetwork.util.OptionalServiceCollection;

public class DRAnouncer {
	private DRAnouncerSettings settings;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;
	private Collection<InstructionHandler> instructionHandlers;

	public DRAnouncerSettings getSettings() {
		return settings;
	}

	public void setSettings(DRAnouncerSettings settings) {
		this.settings = settings;
		settings.setLinkedInstance(this);
	}

	protected void drupdate() {
		Integer sum = 0;
		Iterable<DatumDataSource<? extends EnergyDatum>> datumIterable = settings.getPoweredDevices().services();

		for (DatumDataSource<? extends EnergyDatum> d : datumIterable) {

			EnergyDatum datum = d.readCurrentDatum();

			// not all readings have a datum or a wattage reading which is why
			// one must check for nulls
			if (datum != null) {
				Integer reading = datum.getWatts();
				if (reading != null) {
					sum += reading;
				}
			}
		}

		// Integer currentCost = sum * settings.getEnergyCost();
		List<InstructionHandler> drdevices = new ArrayList<InstructionHandler>();
		for (InstructionHandler handler : instructionHandlers) {
			if (handler.handlesTopic(InstructionHandler.TOPIC_SHED_LOAD)) {
				drdevices.add(handler);
			}
		}
		// for now lets just see if this works
		// settings.getEnergyCost() < sum
		if (true) {
			for (InstructionHandler handler : instructionHandlers) {
				BasicInstruction instr = new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
						Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
				instr.addParameter("shedAmount", "10.0");
				handler.processInstruction(instr);
			}
		}

	}

	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}
}
