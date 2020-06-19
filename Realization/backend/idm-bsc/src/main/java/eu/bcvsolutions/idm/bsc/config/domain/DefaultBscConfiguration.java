package eu.bcvsolutions.idm.bsc.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Bsc configuration - implementation
 *
 * @author Roman Kucera
 *
 */
@Component("bscConfiguration")
public class DefaultBscConfiguration
		extends AbstractConfiguration
		implements BscConfiguration {
}
