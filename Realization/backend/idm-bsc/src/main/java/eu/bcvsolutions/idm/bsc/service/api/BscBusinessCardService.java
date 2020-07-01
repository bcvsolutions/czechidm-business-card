package eu.bcvsolutions.idm.bsc.service.api;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * @author Roman Kucera
 */
public interface BscBusinessCardService {
	IdmFormInstanceDto getFormInstance(IdmIdentityDto identityDto, String contractDto);

	BscBusinessCardDto getBusinessCard(String identity ,String date, String contractId);

	ResponseEntity<IdmBulkActionDto> printBusinessCard(BscBusinessCardDto dto);

	Map<String, Object> prepareAndTransformData(BscBusinessCardDto dto);
}
