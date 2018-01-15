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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.GeneralNodeACEnergyDatum;
import net.solarnetwork.node.reactor.FeedbackInstructionHandler;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.InstructionStatus;
import net.solarnetwork.node.reactor.InstructionStatus.InstructionState;
import net.solarnetwork.node.reactor.support.BasicInstructionStatus;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;

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
public class MockDRDevice extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeACEnergyDatum>, SettingSpecifierProvider, FeedbackInstructionHandler {

	// default values
	private String sourceId = "Mock DR Device";
	private Integer minwatts = 0;
	private Integer maxwatts = 10;
	private Integer energycost = 1;
	private Integer watts = 0;

	private final AtomicReference<GeneralNodeACEnergyDatum> lastsample = new AtomicReference<GeneralNodeACEnergyDatum>();

	@Override
	public Class<? extends GeneralNodeACEnergyDatum> getDatumType() {
		return GeneralNodeACEnergyDatum.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.solarnetwork.node.DatumDataSource#readCurrentDatum()
	 * 
	 * Returns a {@link GeneralNodeACEnergyDatum} the data in the datum is the
	 * state of the simulated circuit.
	 * 
	 * @return A {@link GeneralNodeACEnergyDatum}
	 * 
	 */
	@Override
	public GeneralNodeACEnergyDatum readCurrentDatum() {
		GeneralNodeACEnergyDatum prev = this.lastsample.get();
		GeneralNodeACEnergyDatum datum = new GeneralNodeACEnergyDatum();
		datum.setCreated(new Date());
		datum.setSourceId(sourceId);
		datum.setWatts(watts);
		this.lastsample.compareAndSet(prev, datum);

		return datum;
	}

	// Method get used by the settings page
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Override
	public String getSettingUID() {
		return "net.solarnetwork.node.demandresponse.mockdevice";
	}

	@Override
	public String getDisplayName() {
		return "Mock DR Device";
	}

	// Puts the user configurable settings on the settings page
	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		MockDRDevice defaults = new MockDRDevice();
		List<SettingSpecifier> results = getIdentifiableSettingSpecifiers();

		// user enters text
		results.add(new BasicTextFieldSettingSpecifier("sourceId", defaults.sourceId));
		results.add(new BasicTextFieldSettingSpecifier("minwatts", defaults.minwatts.toString()));
		results.add(new BasicTextFieldSettingSpecifier("maxwatts", defaults.maxwatts.toString()));
		results.add(new BasicTextFieldSettingSpecifier("energycost", defaults.energycost.toString()));
		return results;
	}

	public Integer getMinwatts() {
		return minwatts;
	}

	public void setMinwatts(Integer minwatts) {
		if (watts < minwatts) {
			watts = minwatts;
		}
		this.minwatts = minwatts;
	}

	public Integer getMaxwatts() {
		return maxwatts;
	}

	public void setMaxwatts(Integer maxwatts) {
		if (watts > maxwatts) {
			watts = maxwatts;
		}
		this.maxwatts = maxwatts;
	}

	public Integer getEnergycost() {
		return energycost;
	}

	public void setEnergycost(Integer energycost) {
		this.energycost = energycost;
	}

	@Override
	public boolean handlesTopic(String topic) {
		// TODO Auto-generated method stub
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
			// for now lets just get this working
			state = InstructionState.Completed;
			Map<String, Object> map = new Hashtable<String, Object>(1);
			map.put("drcapable", "true");
			map.put("watts", watts.toString());
			map.put("energycost", energycost.toString());
			map.put("minwatts", minwatts.toString());
			map.put("maxwatts", maxwatts.toString());
			InstructionStatus status = new BasicInstructionStatus(instruction.getId(), state, new Date(), null, map);
			return status;
			// DEBUG TODO
			// settings.setBatteryCharge(5.0);
		}

		if (instruction.getTopic().equals(InstructionHandler.TOPIC_SHED_LOAD)) {
			String param = instruction.getParameterValue(this.sourceId);
			if (param != null) {
				try {
					double value = Double.parseDouble(param) + 0.5;// 0.5 for
																	// rounding
					watts = ((int) value > watts) ? 0 : watts - (int) value;
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
						watts = 0;
					} else if (value > maxwatts) {
						watts = maxwatts;
					} else {
						watts = (int) value;
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
}
