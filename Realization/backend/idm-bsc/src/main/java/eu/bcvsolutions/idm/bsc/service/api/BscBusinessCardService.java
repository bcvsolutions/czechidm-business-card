package eu.bcvsolutions.idm.bsc.service.api;

import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * @author Roman Kucera
 */
public interface BscBusinessCardService {
	IdmFormInstanceDto getFormInstance();

	BscBusinessCardDto getBusinessCard(String identity ,String date, String contractId);

	void printBusinessCard(BscBusinessCardDto dto);
}
