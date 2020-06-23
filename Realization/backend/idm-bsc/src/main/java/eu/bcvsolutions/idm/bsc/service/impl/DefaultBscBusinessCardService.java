package eu.bcvsolutions.idm.bsc.service.impl;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;

/**
 * This service is used from {@Link BscBusinessCardController}
 * @author Roman Kucera
 */
@Service("businessCardService")
@Priority(0)
public class DefaultBscBusinessCardService implements BscBusinessCardService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBscBusinessCardService.class);

	private final IdmIdentityContractService identityContractService;
	private final IdmIdentityService identityService;

	@Autowired
	public DefaultBscBusinessCardService(IdmIdentityContractService identityContractService, IdmIdentityService identityService) {
		this.identityContractService = identityContractService;
		this.identityService = identityService;
	}

	/**
	 * Prepare and return form instance with values
	 * @return Form instance with values
	 */
	@Override
	public IdmFormInstanceDto getFormInstance() {
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		// TODO create attributes
		IdmFormAttributeDto attributeDto = new IdmFormAttributeDto();
		attributeDto.setCode("test");
		attributeDto.setDefaultValue("some default value");
		attributeDto.setPersistentType(PersistentType.SHORTTEXT);
		attributeDto.setId(UUID.randomUUID());

		// TODO create definition
		IdmFormDefinitionDto definitionDto = new IdmFormDefinitionDto();
		definitionDto.addFormAttribute(attributeDto);

		// TODO create values
		IdmFormValueDto valueDto = new IdmFormValueDto();
		valueDto.setShortTextValue("Test value for attr");
		valueDto.setFormAttribute(attributeDto.getId());
		valueDto.setPersistentType(PersistentType.SHORTTEXT);

		Map<String, BaseDto> embedded = new HashMap<>();
		embedded.put("formAttribute", attributeDto);
		valueDto.setEmbedded(embedded);

		formInstanceDto.setFormDefinition(definitionDto);
		formInstanceDto.setValues(Collections.singletonList(valueDto));

		return formInstanceDto;
	}

	/**
	 * Prepare Business card object based on date and contractId if it's filled
	 * @param date Find all valid contracts for this date
	 * @param contractId If it's filled we will use this contract for default values, otherwise we will use main contract
	 * @return Business card dto
	 */
	@Override
	public BscBusinessCardDto getBusinessCard(String identity, String date, String contractId) {
		BscBusinessCardDto businessCardDto = new BscBusinessCardDto();
		businessCardDto.setFormInstance(getFormInstance());
		businessCardDto.setDate(ZonedDateTime.now());

		// TODO lookup by id or username
		IdmIdentityDto idmIdentityDto = identityService.getByUsername(identity);
		LocalDate localDate = LocalDate.parse(date);
		List<IdmIdentityContractDto> allValidForDate = identityContractService.findAllValidForDate(idmIdentityDto.getId(), localDate, false);
		businessCardDto.setContracts(allValidForDate);

		return businessCardDto;
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
