package eu.bcvsolutions.idm.bsc.domain;

import java.util.Arrays;
import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.bsc.BscModuleDescriptor;

/**
 * Aggregate base permission. Name can't contain character '_' - its used for joining to authority name.
 *
 * @author Roman Kucera
 *
 */
public enum BscGroupPermission implements GroupPermission {

	/*
	 * Define your group permission there and example permission you can remove
	 */
	BUSINESSCARD(
			IdmBasePermission.ADMIN);

	public static final String BSC_BUSINESS_CARD_ADMIN = "BUSINESSCARD" + BasePermission.SEPARATOR + "ADMIN";

	private final List<BasePermission> permissions;

	private BscGroupPermission(BasePermission... permissions) {
		this.permissions = Arrays.asList(permissions);
	}
	
	@Override
	public List<BasePermission> getPermissions() {
		return permissions;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public String getModule() {
		return BscModuleDescriptor.MODULE_ID;
	}
}
