package net.solarnetwork.node.testing.DRTests;

import java.util.Collections;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.springframework.context.MessageSource;

import net.solarnetwork.node.job.AbstractJob;
import net.solarnetwork.node.settings.SettingSpecifier;
import net.solarnetwork.node.settings.SettingSpecifierProvider;

public class DRTestingJob extends AbstractJob implements SettingSpecifierProvider {

	private DRTesting drtesting;

	@Override
	public String getSettingUID() {
		// TODO Auto-generated method stub
		return drtesting.getSettingUID();
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return drtesting.getDisplayName();
	}

	@Override
	public MessageSource getMessageSource() {
		// TODO Auto-generated method stub
		return drtesting.getMessageSource();
	}

	@Override
	public List<SettingSpecifier> getSettingSpecifiers() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	protected void executeInternal(JobExecutionContext jobContext) throws Exception {
		// TODO Auto-generated method stub

	}

}
