package eu.bcvsolutions.idm.bsc.service.impl;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * This service is used from {@Link BscBusinessCardController}
 * @author Roman Kucera
 */
@Service("businessCardService")
@Priority(0)
public class DefaultBscBusinessCardService implements BscBusinessCardService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBscBusinessCardService.class);

	@Autowired
	public DefaultBscBusinessCardService() {
	}

	/**
	 * Prepare and return form instance with values
	 * @return Form instance with values
	 */
	@Override
	public IdmFormInstanceDto getFormInstance() {
		return new IdmFormInstanceDto();
	}

	/**
	 * Prepare Business card object based on date and contractId if it's filled
	 * @param date Find all valid contracts for this date
	 * @param contractId If it's filled we will use this contract for default values, otherwise we will use main contract
	 * @return Business card dto
	 */
	public BscBusinessCardDto getBusinessCard(String date, String contractId) {
		return new BscBusinessCardDto();
	}

	/**
	 * It will create PDF from dto
	 * @param dto DTO with values which we use for PDF generation
	 */
	@Override
	public void printBusinessCard(BscBusinessCardDto dto) {
		LOG.info("Nothing here yet");
	}
}
