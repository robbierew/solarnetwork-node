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
		List<DRDevice> drdevices = new ArrayList<DRDevice>();
		Map<DRDevice, FeedbackInstructionHandler> instructionMap = new HashMap<DRDevice, FeedbackInstructionHandler>();
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
					drdevices.add(instance);
					instructionMap.put(instance, handler);

				} else if (test != null) {
					System.out.println(test.getClass().toString());
				}
				System.out.println("asked for instance");
			}
			System.out.println("My first debug print statment");
			// drdevices.add(handler);

		}

		// calculating how much energy is from the grid and from other sources
		// TODO store the read method values
		System.out.println("energy calc");
		Integer energyConsumption = 0;
		Integer energyProduction = 0;
		for (DRDevice d : drdevices) {
			if (d instanceof DRChargeableDevice) {
				DRChargeableDevice dc = (DRChargeableDevice) d;
				if (dc.isDischarging()) {
					energyProduction += dc.getWatts();
				} else {
					energyConsumption += dc.getWatts();
				}
			} else {
				energyConsumption += d.getWatts();
			}
		}
		System.out.println("price calc");
		// TODO check non negative
		// calculating the price of energy as a affine combination of the grid
		// cost and battery costs
		Integer gridEnergy = energyConsumption - energyProduction;
		Double newPrice = settings.getEnergyCost() * (double) gridEnergy / energyConsumption;
		for (DRDevice d : drdevices) {
			if (d instanceof DRChargeableDevice) {
				DRChargeableDevice dc = (DRChargeableDevice) d;
				if (dc.isDischarging()) {
					newPrice += d.getEnergyCost() * d.getWatts() / energyConsumption;
				}

			}
		}

		System.out.println("sort calc");
		Object[][] costArray = new Object[drdevices.size()][2];
		for (int i = 0; i < drdevices.size(); i++) {
			DRDevice d = drdevices.get(i);
			costArray[i][0] = (d.getEnergyCost() + newPrice) * d.getWatts();
			costArray[i][1] = d;
		}

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
		System.out.println("if statment part");
		// Determine if we need demand response
		Double updatedCost = totalCost;

		System.out.println("debug" + totalCost + " " + settings.getDrtargetCost());

		if (totalCost > settings.getDrtargetCost()) {
			System.out.println("decrease section");
			for (int i = drdevices.size() - 1; i >= 0; i--) {
				DRDevice d = (DRDevice) costArray[i][1];

				// check if we can reduce consumption
				if (d.getWatts() > d.getMinPower()) {

					// check if we are discharging battery
					if (d instanceof DRChargeableDevice) {
						DRChargeableDevice dr = (DRChargeableDevice) d;
						if (dr.isDischarging()) {

							// reducing the battery would cause more energy from
							// the grid
							// if grid energy costs more keep the battery going
							if (settings.getEnergyCost() > d.getEnergyCost()) {
								continue;
							}
						}

						// TODO add logic here to decide to discharge

					}

					// if we are here we need to reduce
					Double reduceAmount = totalCost - settings.getDrtargetCost();
					Double energyReduction = reduceAmount / (d.getEnergyCost() + settings.getEnergyCost());
					energyReduction = (d.getWatts() - energyReduction > d.getMinPower()) ? energyReduction
							: d.getWatts() - d.getMinPower();

					System.out.println("reduceAmount" + energyReduction);

					InstructionHandler handler = instructionMap.get(d);

					sendShedInstruction(handler, energyReduction);

				}
			}
		} else if (totalCost < settings.getDrtargetCost()) {
			System.out.println("increase section");
			for (int i = 0; i < drdevices.size(); i++) {
				DRDevice d = (DRDevice) costArray[i][1];

				if (d.getWatts() < d.getMaxPower()) {
					if (d instanceof DRChargeableDevice) {
						DRChargeableDevice dr = (DRChargeableDevice) d;
						if (dr.isDischarging()) {

							// TODO somehow decide whether to turn off battery,
							// charge battery or discharge more
						}
					}

					// if we are here it is okay to increase usage
					Double increaseAmount = settings.getDrtargetCost() - totalCost;
					Double energyIncrease = increaseAmount / (d.getEnergyCost() + settings.getEnergyCost());
					energyIncrease = Math.min(energyIncrease + d.getWatts(), d.getMaxPower()); // (d.getWatts()+energyIncrease
																								// <
																								// d.getMaxPower())
																								// ?
																								// energyIncrease
					System.out.println("about to increase");
					InstructionHandler handler = instructionMap.get(d);
					setWattageInstruction(handler, energyIncrease);

				}

			}
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

	private void setWattageInstruction(InstructionHandler handler, Double energyLevel) {
		BasicInstruction instr = new BasicInstruction(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER, new Date(),
				Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
		instr.addParameter("watts", energyLevel.toString());
		handler.processInstruction(instr);
	}

	private void sendShedInstruction(InstructionHandler handler, Double shedamount) {
		BasicInstruction instr = new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
				Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null);
		instr.addParameter(settings.getUID(), shedamount.toString());
		handler.processInstruction(instr);
	}

	// TODO give this method a massive refactoring
	@Deprecated
	protected void drupdateOld() {
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
