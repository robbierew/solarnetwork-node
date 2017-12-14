package net.solarnetwork.node.testing.instructionTests;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeEnergyDatum;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.reactor.support.BasicInstruction;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.support.DatumDataSourceSupport;

public class InstructionTest extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyDatum>, SettingSpecifierProvider, InstructionHandler {

	private Collection<InstructionHandler> instructionHandlers;
	private Boolean gotinstruction = false;

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return "net.solarnetwork.node.testing.instructionTests";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Instruction Tests";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		// TODO Auto-generated method stub
		return getIdentifiableSettingSpecifiers();
	}

	@Override
	public Class<? extends GeneralNodeEnergyDatum> getDatumType() {
		// TODO Auto-generated method stub
		return GeneralNodeEnergyDatum.class;
	}

	@Override
	public GeneralNodeEnergyDatum readCurrentDatum() {
		GeneralNodeEnergyDatum datum = new GeneralNodeEnergyDatum();
		datum.setSourceId(getUID());
		if (instructionHandlers != null) {
			datum.setWatts(10);
			datum.putStatusSampleValue("Count", instructionHandlers.size());

		} else {
			datum.setWatts(0);
			// instructionHandlers.iterator().next().
		}
		for (InstructionHandler i : instructionHandlers) {
			i.processInstruction(new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
					Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null));

		}
		datum.putStatusSampleValue("Got instruction?", gotinstruction);
		// InstructionUtils.handleInstruction(instructionHandlers, new
		// BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD,
		// new Date(), Instruction.LOCAL_INSTRUCTION_ID,
		// Instruction.LOCAL_INSTRUCTION_ID, null));
		return datum;
	}

	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}

	@Override
	public boolean handlesTopic(String topic) {
		// TODO Auto-generated method stub
		return topic.equals(InstructionHandler.TOPIC_SHED_LOAD);
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		gotinstruction = true;
		return InstructionState.Declined;
	}

}
