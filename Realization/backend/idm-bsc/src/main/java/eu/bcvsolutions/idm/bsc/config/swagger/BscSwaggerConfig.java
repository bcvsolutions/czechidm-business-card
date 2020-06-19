package eu.bcvsolutions.idm.bsc.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.bsc.BscModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Bsc module swagger configuration
 *
 * @author Roman Kucera
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class BscSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private BscModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public Docket bscApi() {
		return api("eu.bcvsolutions.idm.rest");
	}
}
