package eu.bcvsolutions.idm.bsc.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.bsc.BscModuleDescriptor;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Bsc configuration - interface
 *
 * @author Roman Kucera
 */
public interface BscConfiguration extends Configurable, ScriptEnabled {

	String PRIVATE_PREFIX_CONFIGURATION_BSC = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			BscModuleDescriptor.MODULE_ID + ConfigurationService.PROPERTY_SEPARATOR + "configuration" + ConfigurationService.PROPERTY_SEPARATOR;

	/**
	 * Property with id for tree type where is stored clinics
	 */
	String SAVE_PATH = PRIVATE_PREFIX_CONFIGURATION_BSC + "save.path";
	String BCK_PATH = PRIVATE_PREFIX_CONFIGURATION_BSC + "bck.path";

	String getSavePath();

	String getBckPath();

	@Override
	default String getConfigurableType() {
		return "configuration";
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}
}
