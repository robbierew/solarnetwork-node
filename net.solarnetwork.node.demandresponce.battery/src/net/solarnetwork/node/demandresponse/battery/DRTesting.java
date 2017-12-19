package net.solarnetwork.node.demandresponse.battery;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import net.solarnetwork.node.DatumDataSource;
import net.solarnetwork.node.domain.EnergyDatum;
import net.solarnetwork.node.domain.GeneralNodeEnergyDatum;
import net.solarnetwork.node.reactor.Instruction;
import net.solarnetwork.node.reactor.InstructionHandler;
import net.solarnetwork.node.reactor.support.BasicInstruction;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;
import net.solarnetwork.node.settings.support.BasicTextFieldSettingSpecifier;
import net.solarnetwork.node.support.DatumDataSourceSupport;
import net.solarnetwork.util.OptionalServiceCollection;

public class DRTesting extends DatumDataSourceSupport
		implements DatumDataSource<GeneralNodeEnergyDatum>, SettingSpecifierProvider {

	private OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums = null;
	private Boolean magicflag = false;

	private Double maxCharge = 10.0;
	private Double charge = 10.0;

	private MockBattery mb = new MockBattery(maxCharge);
	private Collection<InstructionHandler> instructionHandlers;

	@Override
	public Class<? extends GeneralNodeEnergyDatum> getDatumType() {
		return GeneralNodeEnergyDatum.class;
	}

	@Override
	public GeneralNodeEnergyDatum readCurrentDatum() {
		if (mb == null) {
			mb = new MockBattery(10);
			mb.setCharge(10);
			mb.setDraw(0);
		}

		GeneralNodeEnergyDatum datum = new GeneralNodeEnergyDatum();
		datum.setCreated(new Date());
		datum.setSourceId(getUID());
		datum.putStatusSampleValue("TestMessage", "TestValue");
		if (powerDatums == null) {
			datum.putStatusSampleValue("Got info?", "no");
		} else {
			makeStats(datum);
		}
		for (InstructionHandler i : instructionHandlers) {
			i.processInstruction(new BasicInstruction(InstructionHandler.TOPIC_SHED_LOAD, new Date(),
					Instruction.LOCAL_INSTRUCTION_ID, Instruction.LOCAL_INSTRUCTION_ID, null));

		}
		datum.putStatusSampleValue("Magic", magicflag);
		datum.putInstantaneousSampleValue("Battery Power", mb.readCharge());
		datum.putInstantaneousSampleValue("Battery Percent", mb.percentageCapacity());
		return datum;

	}

	private void makeStats(GeneralNodeEnergyDatum datum) {
		Integer sum = 0;
		for (DatumDataSource<? extends EnergyDatum> ed : powerDatums.services()) {
			EnergyDatum dat = ed.readCurrentDatum();
			sum += dat.getWatts();
		}
		mb.setDraw(sum.doubleValue());
		datum.setWatts(sum);
	}

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return "net.solarnetwork.node.demandresponse.battery";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "DRTesting";
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		List<SettingSpecifier> results = super.getIdentifiableSettingSpecifiers();
		DRTesting defaults = new DRTesting();
		results.add(new BasicTextFieldSettingSpecifier("powerDatums.propertyFilters['UID']", "Main"));
		results.add(new BasicTextFieldSettingSpecifier("powerDatums.propertyFilters['groupUID']", ""));
		results.add(new BasicTextFieldSettingSpecifier("batteryMaxCharge", defaults.maxCharge.toString()));
		results.add(new BasicTextFieldSettingSpecifier("batteryCharge", defaults.charge.toString()));
		// TODO Auto-generated method stub
		return results;
	}

	public void setPowerDatums(OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> powerDatums) {
		this.powerDatums = powerDatums;
		this.magicflag = true;

	}

	public OptionalServiceCollection<DatumDataSource<? extends EnergyDatum>> getPowerDatums() {
		return powerDatums;
	}

	public void setBatteryMaxCharge(Double charge) {
		if (charge != null) {
			mb.setMax(charge);
		}
		this.maxCharge = charge;
	}

	public Double getBatteryMaxCharge() {
		return charge;
	}

	public void setBatteryCharge(Double charge) {
		if (charge != null) {
			mb.setCharge(charge);
		}
		this.charge = charge;
	}

	// note this returns the charge value the user set in settings page not the
	// current charge
	// of the battery
	public Double getBatteryCharge() {
		return this.charge;
	}

	public void setInstructionHandlers(Collection<InstructionHandler> instructionHandlers) {
		this.instructionHandlers = instructionHandlers;
	}

	public Collection<InstructionHandler> getInstructionHandlers() {
		return instructionHandlers;
	}

}
