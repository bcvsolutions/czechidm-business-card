package eu.bcvsolutions.idm.bsc;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Initialize Bsc module
 *
 * @author Roman Kucera
 *
 */
@Component
@DependsOn("initApplicationData")
public class BscModuleInitializer implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BscModuleInitializer.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		LOG.info("Module [{}] initialization", BscModuleDescriptor.MODULE_ID);
	}
}
