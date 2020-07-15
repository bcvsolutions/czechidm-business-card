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
	/**
	 * Prepare form instance with values which will be rendered on FE
	 * @param identityDto object with identity for who the business card is used. You can search contract, eav, etc by this identity
	 * @param contractDto selected contract from FE
	 * @return form instance with/without default values, depends on implementation
	 */
	IdmFormInstanceDto getFormInstance(IdmIdentityDto identityDto, String contractDto);

	/**
	 * Prepare Business card object based on date and contractId if it's filled
	 *
	 * @param date Find all valid contracts for this date
	 * @param contractId If it's filled we will use this contract for default values, otherwise we will use main contract
	 * @return Business card dto
	 */
	BscBusinessCardDto getBusinessCard(String identity ,String date, String contractId);

	/**
	 * It will create PDF from dto
	 *
	 * @param dto DTO with values which we use for PDF generation
	 * @return bulk action dto so on FE we can render progress bar
	 */
	ResponseEntity<IdmBulkActionDto> printBusinessCard(BscBusinessCardDto dto);

	/**
	 * Called inside {@link #printBusinessCard(BscBusinessCardDto)} method
	 * @param dto attributes of this business card dto are transformed to Map
	 * @param identityDto dto of user for who we are preparing the business card
	 * @return Map with attributes from business card dto
	 */
	Map<String, Object> prepareAndTransformData(BscBusinessCardDto dto, IdmIdentityDto identityDto);
}
