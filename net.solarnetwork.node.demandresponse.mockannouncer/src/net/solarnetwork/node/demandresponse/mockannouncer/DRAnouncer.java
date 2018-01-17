package net.solarnetwork.node.demandresponse.mockannouncer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.support.BasicInstruction;
import net.solarnetwork.util.OptionalServiceCollection;

/**
 * Expirimental class looking into how a demand responce system my look like.
 * Method names and API use will probably change in refactoring into a propper
 * implementation.
 * 
 * 
 * I want the strategy part to be changeable, I will look into being able to
 * select from a list of strategies and have their parameters dynamicly pop up
 * onto the settings page. I have seen something similar done with
 * demandbalancer
 * 
 * 
 * @author robert
 *
 */
public class DRAnouncer {
	private DRAnouncerSettings settings;
	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> poweredDevices;
	private Collection<FeedbackInstructionHandler> feedbackInstructionHandlers;
	private Collection<InstructionHandler> instructionHandlers;

	// status variables for the datum source
	private Integer numdrdevices = 0;

	public DRAnouncerSettings getSettings() {
		return settings;
	}

	// configured in OSGI
	public void setSettings(DRAnouncerSettings settings) {
		this.settings = settings;

		// TODO
		// I don't like this coupling factor this somehow
		// potentaly look into getting drupdate called from a cron expression
		// rather than when a setting changes
		settings.setLinkedInstance(this);
	}

	// TODO give this method a massive refactoring
	protected void drupdate() {

		// TODO remove
		System.out.println("There are " + feedbackInstructionHandlers.size() + "fbih");
		List<FeedbackInstructionHandler> drdevices = new ArrayList<FeedbackInstructionHandler>();

		// the reason the mapping should be in String String is because perhapes
		// in the future it could be JSON
		Map<FeedbackInstructionHandler, Map<String, ?>> instructionMap = new HashMap<FeedbackInstructionHandler, Map<String, ?>>();

		for (FeedbackInstructionHandler handler : feedbackInstructionHandlers) {

			// TODO change the instruction from a string to a static referance
			if (handler.handlesTopic("getDRDeviceInstance")) {

				BasicInstruction instr = new BasicInstruction("getDRDeviceInstance", new Date(),
						Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);

				// The devices want to know where the instruction came from for
				// verification
				instr.addParameter(settings.getUID(), "");

				Map<String, ?> test = handler.processInstructionWithFeedback(instr).getResultParameters();
				if (test != null) {
					test = handler.processInstructionWithFeedback(instr).getResultParameters();
					if (DRSupportTools.isDRCapable(test)) {
						System.out.println("got instance");

						if (DRSupportTools.isChargeable(test)) {
							System.out.println("is also chargealbe");
						}

						drdevices.add(handler);
						instructionMap.put(handler, test);

					}
				}

			}

		}

		numdrdevices = drdevices.size();

		// TODO print statement
		System.out.println("energy calc");

		// energyConsumption is energy used when not discharging
		Integer energyConsumption = 0;

		// energyProduction is energy used when discharging
		Integer energyProduction = 0;
		for (FeedbackInstructionHandler d : drdevices) {
			Map<String, ?> params = instructionMap.get(d);

			Integer wattValue = DRSupportTools.readWatts(params);

			if (DRSupportTools.isChargeable(params)) {

				if (DRSupportTools.isDischarging(params)) {
					energyProduction += wattValue;
				} else {
					energyConsumption += wattValue;
				}
			} else {
				energyConsumption += wattValue;
			}
		}

		// TODO remove print statement
		System.out.println("price calc");

		// TODO check non negative (a case when this can happen is when batterys
		// discharge more than demand)
		// calculating the price of energy as a affine combination of the grid.
		// eg if 50% energy from grid at $2 and 50% from battery at $1 energy is
		// priced at $1.50
		// cost and battery costs
		Integer gridEnergy = energyConsumption - energyProduction;
		Double newPrice = settings.getEnergyCost() * (double) gridEnergy / energyConsumption;

		// TODO check if there is an more nicer way to do this
		// when energyConusmption is 0
		if (newPrice.isNaN()) {
			newPrice = 1.0;
		}

		for (FeedbackInstructionHandler d : drdevices) {
			Map<String, ?> params = instructionMap.get(d);

			if (DRSupportTools.isChargeable(params)) {

				Integer wattValue = DRSupportTools.readWatts(params);
				Integer costValue = DRSupportTools.readEnergyCost(params);

				if (DRSupportTools.isDischarging(params)) {
					// TODO somehow update this price when battery changes
					newPrice += costValue * wattValue / energyConsumption;
				}

			}
		}

		// TODO remove print statement
		System.out.println("sort calc");

		// need an object array cause I want to relate double with drdevice
		// the reason im not using mapping is multiple drdevices could have the
		// same double and I need to be able to sort the first column
		Object[][] costArray = new Object[drdevices.size()][2];
		for (int i = 0; i < drdevices.size(); i++) {
			FeedbackInstructionHandler d = drdevices.get(i);
			Map<String, ?> params = instructionMap.get(d);

			Integer wattValue = DRSupportTools.readWatts(params);
			Integer costValue = DRSupportTools.readEnergyCost(params);

			// TODO double check you have done your maths correctly to factor in
			// the convention of price being in KWh but using watts
			costArray[i][0] = (costValue + newPrice) * wattValue;
			costArray[i][1] = d;
		}

		// simple sum to find the cost of running all these devices
		Double totalCost = 0.0;
		for (int i = 0; i < costArray.length; i++) {
			totalCost += (Double) costArray[i][0];
		}

		// sorts the first columb which are doubles and keeps the relationship
		// between cost and DRDevice
		Arrays.sort(costArray, new Comparator<Object[]>() {

			@Override
			public int compare(Object[] o1, Object[] o2) {
				Double d1 = (Double) o1[0];
				Double d2 = (Double) o2[0];
				return d1.compareTo(d2);
			}

		});

		// TODO remove print statement
		System.out.println("if statment part");
		// Determine if we need demand response

		// I don't think we need this variable
		Double updatedCost = totalCost;

		// TODO remove debug statement
		System.out.println("debug" + totalCost + " " + settings.getDrtargetCost());

		// TODO refactor this so much
		Boolean shouldDischarge = null;

		// if this is true we need to reduce demand to get costs down
		if (totalCost > settings.getDrtargetCost()) {
			System.out.println("decrease section");

			// going from largest cost to smallest cost (this is just a design
			// decision I made)
			for (int i = drdevices.size() - 1; i >= 0; i--) {
				FeedbackInstructionHandler d = (FeedbackInstructionHandler) costArray[i][1];
				Map<String, ?> params = instructionMap.get(d);

				Integer wattValue = DRSupportTools.readWatts(params);
				Integer minValue = DRSupportTools.readMinWatts(params);
				Integer energyCost = DRSupportTools.readEnergyCost(params);

				// check if we can reduce consumption
				if (wattValue > minValue) {

					// check if we are discharging battery
					if (DRSupportTools.isChargeable(params)) {

						// if (settings.getEnergyCost() > dr.getEnergyCost()) {
						// if (dr.isDischarging()) {
						//
						// // reducing the battery would cause more energy
						// // from
						// // the grid
						// // if grid energy costs more keep the battery
						// // going
						//
						// continue;
						//
						// } else {
						// if (dr.getCharge() > 0) {
						// // Problem im having here is that the code
						// // below is for shreding
						// // but I need to increase just for discharge
						// }
						// }
						// }

						// TODO add logic here to decide to discharge

					}

					// if we are here we need to reduce
					Double reduceAmount = totalCost - settings.getDrtargetCost();
					Double energyReduction = reduceAmount / (energyCost + settings.getEnergyCost());
					Double appliedenergyReduction = (wattValue - energyReduction > minValue) ? energyReduction
							: wattValue - minValue;

					System.out.println("reduceAmount" + appliedenergyReduction);

					// Im annoyed by this instruction because it is only reduce
					// and not gain
					sendShedInstruction(d, appliedenergyReduction);

					// we were able to increase to match demand no need for more
					// devices to have DR
					if (energyReduction.equals(appliedenergyReduction)) {
						break;
					} else {
						// update the cost for the next devices to calcuate with
						// TODO ensure that if we have changed batterys this is
						// still effective
						// One idea is to have a method to recalcuate the entire
						// cost all over again
						totalCost -= appliedenergyReduction * (energyCost + settings.getEnergyCost());
					}

				}
			}

			// if true we need to increase demand
		} else if (totalCost < settings.getDrtargetCost()) {
			// TODO remove print statement
			System.out.println("increase section");
			System.out.println("drd " + drdevices.size());
			// this time we start with the cheapest devices (my reasoning is
			// that these devices most likely have room to power on more)
			for (int i = 0; i < drdevices.size(); i++) {
				FeedbackInstructionHandler d = (FeedbackInstructionHandler) costArray[i][1];
				Map<String, ?> params = instructionMap.get(d);

				Integer wattValue = DRSupportTools.readWatts(params);
				Integer maxValue = DRSupportTools.readMaxWatts(params);
				Integer energyCost = DRSupportTools.readEnergyCost(params);

				if (wattValue < maxValue) {
					if ("true".equals(params.get("chargeable"))) {

						if ("true".equals(params.get("isDischarging"))) {

							// TODO somehow decide whether to turn off battery,
							// charge battery or discharge more
						} else {
							shouldDischarge = false;
						}
					}

					// if we are here it is okay to increase usage
					Double increaseAmount = settings.getDrtargetCost() - totalCost;

					System.out.println("increaseAmount " + increaseAmount);

					Double energyIncrease = increaseAmount / (energyCost + settings.getEnergyCost());
					energyIncrease += wattValue;

					System.out.println("energy Increase " + energyIncrease);

					Double appliedenergyIncrease = Math.min(energyIncrease, maxValue);
					Double energydelta = appliedenergyIncrease - wattValue;

					// TODO remove print statement
					System.out.println("about to increase " + appliedenergyIncrease);

					setWattageInstruction(d, appliedenergyIncrease, shouldDischarge);

					// we were able to increase to match demand no need for more
					// devices to have DR
					if (energyIncrease.equals(appliedenergyIncrease)) {
						break;
					} else {
						// update the cost for the next devices to calcuate with
						totalCost += energydelta * (energyCost + settings.getEnergyCost());
					}

				}

			}
		}

	}

	// Instruction to set the wattage parameter on the device it uses the
	// TOPIC_SET_CONTROL_PARAMETER insrtuction
	private void setWattageInstruction(InstructionHandler handler, Double energyLevel, Boolean shouldDischarge) {
		BasicInstruction instr = new BasicInstruction(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER, new Date(),
				Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
		instr.addParameter("watts", energyLevel.toString());

		if (shouldDischarge != null) {
			instr.addParameter("discharge", shouldDischarge.toString());
		}

		handler.processInstruction(instr);
	}

	// Instruction to reduce wattage parameter on the device. The only reason Im
	// using this instead of setWattageInstruction is because this instruction
	// already exists
	private void sendShedInstruction(InstructionHandler handler, Double shedamount) {
		BasicInstruction instr = new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
				Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);

		// the convention I saw used from other classes is that the value of the
		// shedload is from the UID for verification
		instr.addParameter(settings.getUID(), shedamount.toString());
		handler.processInstruction(instr);
	}

	public Collection<FeedbackInstructionHandler> getFeedbackInstructionHandlers() {
		return feedbackInstructionHandlers;
	}

	// configured in OSGI
	public void setFeedbackInstructionHandlers(Collection<FeedbackInstructionHandler> feedbackInstructionHandlers) {
		this.feedbackInstructionHandlers = feedbackInstructionHandlers;
	}

	public Integer getNumdrdevices() {
		return numdrdevices;
	}
}
