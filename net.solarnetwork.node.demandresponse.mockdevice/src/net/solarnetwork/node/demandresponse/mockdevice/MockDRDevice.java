/* ==================================================================
 * MockMeterDataSource.java - 10/06/2015 1:28:07 pm
 * 
 * Copyright 2017 SolarNetwork.net Dev Team
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ==================================================================
 */

package net.solarnetwork.node.demandresponse.mockdevice;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.job.SimpleManagedTriggerAndJobDetail;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.reactor.support.BasicInstructionStatus;
import net.solarnetwork.node.settings.SettingSpecifierProvider;

/**
 * Mock plugin to be the source of values for a GeneralNodeACEnergyDatum, this
 * mock tries to simulate a AC circuit containing a resister and inductor in
 * series.
 * 
 * <p>
 * This class implements {@link SettingSpecifierProvider} and
 * {@link DatumDataSource}
 * </p>
 * 
 * @author robert
 * @version 1.0
 */
public class MockDRDevice extends SimpleManagedTriggerAndJobDetail implements FeedbackInstructionHandler {

	private MockDRDeviceSettings settings;

	// refactoring notes for myself due to the way OSGI is I need to break up
	// this class into smaller classes and then configure them in OSGI

	@Override
	public boolean handlesTopic(String topic) {
		// TODO have it actualy check
		return true;
	}

	@Override
	public InstructionState processInstruction(Instruction instruction) {
		InstructionStatus status = processInstructionWithFeedback(instruction);
		return status.getAcknowledgedInstructionState();
	}

	@Override
	public InstructionStatus processInstructionWithFeedback(Instruction instruction) {
		InstructionState state;
		if (instruction.getTopic().equals("getDRDeviceInstance")) {
			Map<String, Object> map = new Hashtable<String, Object>();
			if (instruction.getParameterValue(settings.getDrsource()) == null) {
				state = InstructionState.Declined;
			} else {
				// for now lets just get this working
				state = InstructionState.Completed;

				map.put("drcapable", "true");
				map.put("watts", settings.getWatts().toString());
				map.put("energycost", settings.getEnergycost().toString());
				map.put("minwatts", settings.getMinwatts().toString());
				map.put("maxwatts", settings.getMaxwatts().toString());
			}

			InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date(), null, map);
			return status;

		}

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			String param = instruction.getParameterValue(settings.getDrsource());
			if (param != null) {
				try {
					double value = Double.parseDouble(param) + 0.5;// 0.5 for
																	// rounding
					settings.setWatts(((int) value > settings.getWatts()) ? 0 : settings.getWatts() - (int) value);
					state = InstructionState.Completed;
				} catch (NumberFormatException e) {
					state = InstructionState.Declined;
				}

			} else {
				state = InstructionState.Declined;
			}

		} else if (instruction.getTopic().equals(InstructionHandler.TOPIC_SET_CONTROL_PARAMETER)) {
			String param = instruction.getParameterValue("watts");
			if (param != null) {
				try {
					double value = Double.parseDouble(param);
					if (value < 0) {
						settings.setWatts(0);

					} else if (value > settings.getMaxwatts()) {
						settings.setWatts(settings.getMaxwatts());

					} else {
						settings.setWatts((int) value);
					}
					state = InstructionState.Completed;
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

	public void setSettings(MockDRDeviceSettings settings) {
		this.settings = settings;
	}

	public MockDRDeviceSettings getSettings() {
		return settings;
	}

}
