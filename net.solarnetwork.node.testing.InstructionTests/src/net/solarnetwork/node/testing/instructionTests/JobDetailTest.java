package net.solarnetwork.node.testing.instructionTests;

import net.solarnetwork.node.job.SimpleManagedTriggerAndJobDetail;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;

public class JobDetailTest extends SimpleManagedTriggerAndJobDetail implements InstructionHandler {

	private InstructionTest instructionSource;

	public InstructionTest getInstructionSource() {
		return instructionSource;
	}

	public void setInstructionSource(InstructionTest instructionSource) {
		this.instructionSource = instructionSource;
	}

	@Override
	public boolean handlesTopic(String topic) {
		// TODO Auto-generated method stub
		return instructionSource.handlesTopic(topic);
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		// TODO Auto-generated method stub
		return instructionSource.processInstruction(instruction);
	}

}
