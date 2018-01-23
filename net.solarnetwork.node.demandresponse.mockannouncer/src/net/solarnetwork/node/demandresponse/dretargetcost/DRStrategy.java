package net.solarnetwork.node.demandresponse.dretargetcost;
import java.util.List;

import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
public interface DRStrategy {
	/**
	 * Take a list of FeedbackInstructions. The implementation of this class should be able to ignore any InstructionHandlers that are not supported by this strategy without error.
	 * @param handlers
	 */
	void setHandlers(List<FeedbackInstructionHandler> handlers);
	
	/**
	 * Call this method to make the DRStrategy perform an appropriate demand response to the handlers
	 */
	void drupdate();
}
