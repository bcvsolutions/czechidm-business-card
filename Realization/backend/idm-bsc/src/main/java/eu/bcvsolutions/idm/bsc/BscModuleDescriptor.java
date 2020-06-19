package eu.bcvsolutions.idm.bsc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.bsc.domain.BscGroupPermission;
import eu.bcvsolutions.idm.bsc.domain.BscResultCode;

/**
 * Bsc module descriptor
 *
 * @author Roman Kucera
 */
@Component
@PropertySource("classpath:module-" + BscModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + BscModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class BscModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "bsc";

	@Override
	public String getId() {
		return MODULE_ID;
	}

	/**
	 * Enables links to swagger documentation
	 */
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}

	@Override
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		return configs;
	}

	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(BscGroupPermission.values());
	}

	@Override
	public List<ResultCode> getResultCodes() {
		return Arrays.asList(BscResultCode.values());
	}
}
