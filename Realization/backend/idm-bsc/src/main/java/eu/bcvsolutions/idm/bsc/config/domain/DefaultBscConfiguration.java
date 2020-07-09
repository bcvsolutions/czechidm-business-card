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
	@Override
	public String getSavePath() {
		return getConfigurationService().getValue(SAVE_PATH);
	}

	@Override
	public String getBckPath() {
		return getConfigurationService().getValue(BCK_PATH);
	}

	@Override
	public String getImagePath() {
		return getConfigurationService().getValue(IMAGE_PATH);
	}

	@Override
	public String getTmpPath() {
		return getConfigurationService().getValue(TMP_PATH);
	}

	@Override
	public String getTemplatePath() {
		return getConfigurationService().getValue(TEMPLATE_PATH);
	}

	@Override
	public String getFopConfigPath() {
		return getConfigurationService().getValue(FOP_CONFIG_PATH);
	}
}
