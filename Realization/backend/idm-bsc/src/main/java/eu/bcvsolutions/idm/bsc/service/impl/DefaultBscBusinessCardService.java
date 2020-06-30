package eu.bcvsolutions.idm.bsc.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.report.BscIdentityBusinessCardExport;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * This service is used from {@Link BscBusinessCardController}
 *
 * @author Roman Kucera
 */
@Service("businessCardService")
@Priority(0)
public class DefaultBscBusinessCardService implements BscBusinessCardService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBscBusinessCardService.class);

	private final IdmIdentityContractService identityContractService;
	private final IdmIdentityService identityService;
	private final BulkActionManager bulkActionManager;

	private String nameAttrName = "name";
	private String titlesBeforeAttrName = "titlesBefore";
	private String titlesAfterAttrName = "titlesAfter";
	private String departmentAttrName = "department";
	private String positionAttrName = "position";
	private String personalNumberAttrName = "personalNumber";
	private String saveToHddAttrName = "saveToHdd";

	private String embeddedFormAttrName = "formAttribute";

	@Autowired
	public DefaultBscBusinessCardService(IdmIdentityContractService identityContractService, IdmIdentityService identityService, BulkActionManager bulkActionManager) {
		this.identityContractService = identityContractService;
		this.identityService = identityService;
		this.bulkActionManager = bulkActionManager;
	}

	/**
	 * Prepare and return form instance with values
	 *
	 * @param identityDto
	 * @param contractId
	 * @return Form instance with values
	 */
	@Override
	public IdmFormInstanceDto getFormInstance(IdmIdentityDto identityDto, String contractId) {
		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto();

		IdmFormAttributeDto nameAttr = getFormAttr(nameAttrName, false);
		IdmFormAttributeDto titlesBeforeAttr = getFormAttr(titlesBeforeAttrName, false);
		IdmFormAttributeDto titlesAfterAttr = getFormAttr(titlesAfterAttrName, false);
		IdmFormAttributeDto departmentAttr = getFormAttr(departmentAttrName, false);
		IdmFormAttributeDto positionAttr = getFormAttr(positionAttrName, false);
		IdmFormAttributeDto personalNumberAttr = getFormAttr(personalNumberAttrName, false);
		IdmFormAttributeDto saveToHddAttr = getFormAttr(saveToHddAttrName, false);

		IdmFormDefinitionDto definitionDto = new IdmFormDefinitionDto();
		definitionDto.addFormAttribute(nameAttr);
		definitionDto.addFormAttribute(titlesBeforeAttr);
		definitionDto.addFormAttribute(titlesAfterAttr);
		definitionDto.addFormAttribute(departmentAttr);
		definitionDto.addFormAttribute(positionAttr);
		definitionDto.addFormAttribute(personalNumberAttr);
		definitionDto.addFormAttribute(saveToHddAttr);

		IdmFormValueDto nameValue = getStringValue(nameAttr, identityDto.getFirstName() + " " + identityDto.getLastName());
		IdmFormValueDto titlesBeforeValue = getStringValue(titlesBeforeAttr, identityDto.getTitleBefore());
		IdmFormValueDto titlesAfterValue = getStringValue(titlesAfterAttr, identityDto.getTitleAfter());
		IdmFormValueDto departmentValue = getStringValue(departmentAttr, "");

		String position = "";
		if (!StringUtils.isBlank(contractId)) {
			IdmIdentityContractDto contractDto = identityContractService.get(contractId);
			if (contractDto != null && !StringUtils.isBlank(contractDto.getPosition())) {
				position = contractDto.getPosition();
			}
		}

		IdmFormValueDto positionValue = getStringValue(positionAttr, position);
		IdmFormValueDto personalNumberValue = getStringValue(personalNumberAttr, identityDto.getExternalCode());
		IdmFormValueDto saveToHddValue = getBoolValue(saveToHddAttr, true);

		List<IdmFormValueDto> values = new ArrayList<>(Arrays.asList(nameValue, titlesBeforeValue, titlesAfterValue, departmentValue,
				positionValue, personalNumberValue, saveToHddValue));

		formInstanceDto.setFormDefinition(definitionDto);
		formInstanceDto.setValues(values);

		return formInstanceDto;
	}

	protected IdmFormValueDto getBoolValue(IdmFormAttributeDto attr, boolean value) {
		IdmFormValueDto nameValue = new IdmFormValueDto();
		nameValue.setBooleanValue(value);
		nameValue.setFormAttribute(attr.getId());
		nameValue.setPersistentType(PersistentType.BOOLEAN);
		// set attribute to embedded
		Map<String, BaseDto> embedded = new HashMap<>();
		embedded.put(embeddedFormAttrName, attr);
		nameValue.setEmbedded(embedded);
		return nameValue;
	}

	protected IdmFormValueDto getStringValue(IdmFormAttributeDto attr, String value) {
		IdmFormValueDto nameValue = new IdmFormValueDto();
		nameValue.setShortTextValue(value);
		nameValue.setFormAttribute(attr.getId());
		nameValue.setPersistentType(PersistentType.SHORTTEXT);
		// set attribute to embedded
		Map<String, BaseDto> embedded = new HashMap<>();
		embedded.put(embeddedFormAttrName, attr);
		nameValue.setEmbedded(embedded);
		return nameValue;
	}

	protected IdmFormAttributeDto getFormAttr(String code, boolean isReadOnly) {
		IdmFormAttributeDto attributeDto = new IdmFormAttributeDto();
		attributeDto.setCode(code);
		attributeDto.setPersistentType(PersistentType.SHORTTEXT);
		attributeDto.setId(UUID.randomUUID());
		attributeDto.setReadonly(isReadOnly);
		return attributeDto;
	}

	/**
	 * Prepare Business card object based on date and contractId if it's filled
	 *
	 * @param date       Find all valid contracts for this date
	 * @param contractId If it's filled we will use this contract for default values, otherwise we will use main contract
	 * @return Business card dto
	 */
	@Override
	public BscBusinessCardDto getBusinessCard(String identity, String date, String contractId) {

		// TODO lookup by id or username
		IdmIdentityDto idmIdentityDto = identityService.getByUsername(identity);
		LocalDate localDate = LocalDate.parse(date);
		List<IdmIdentityContractDto> allValidForDate = identityContractService.findAllValidForDate(idmIdentityDto.getId(), localDate, false);
		Map<String, String> contracts = new LinkedHashMap<>();
		allValidForDate.forEach(idmIdentityContractDto -> {
			String niceLabel = idmIdentityContractDto.getPosition() + " - " + idmIdentityContractDto.getValidFrom()
					+ " - " + idmIdentityContractDto.getValidTill();
			contracts.put(idmIdentityContractDto.getId().toString(), niceLabel);
		});
		BscBusinessCardDto businessCardDto = new BscBusinessCardDto();
		if (StringUtils.isBlank(contractId) && !allValidForDate.isEmpty()) {
			contractId = allValidForDate.get(0).getId().toString();
		}
		businessCardDto.setFormInstance(getFormInstance(idmIdentityDto, contractId));
		businessCardDto.setDate(localDate.atStartOfDay(ZoneId.systemDefault()));

		businessCardDto.setContracts(contracts);

		return businessCardDto;
	}

	/**
	 * It will create PDF from dto
	 *
	 * @param dto DTO with values which we use for PDF generation
	 * @return
	 */
	@Override
	public ResponseEntity<InputStreamResource> printBusinessCard(BscBusinessCardDto dto) {
		LOG.info("We will generate business card");

		Map<Object, Object> params = new HashMap<>();

		IdmFormInstanceDto formInstance = dto.getFormInstance();
		Map<UUID, IdmFormAttributeDto> attributes = formInstance.getFormDefinition().getFormAttributes()
				.stream()
				.collect(Collectors.toMap(IdmFormAttributeDto::getId, attr -> attr));

		transformAttrsToMap(params, formInstance, attributes);

		// TODO make configurable
		params.put("imagePath", "profile.png");
		params.put("nameSize", ((String) params.getOrDefault(nameAttrName, "") + params.getOrDefault(titlesAfterAttrName, "") +
				params.getOrDefault(titlesBeforeAttrName, "")).length() > 38 ? "8.6pt" : "11pt");

		// RUN REPORT
		IdmBulkActionDto bulkActionDto = bulkActionManager.getAvailableActions(IdmIdentity.class).
				stream()
				.filter(action -> action.getName().equals(BscIdentityBusinessCardExport.REPORT_NAME))
				.findFirst()
				.orElse(null);
		if (bulkActionDto != null) {
			// TODO lookup by id or username
			IdmIdentityDto idmIdentityDto = identityService.getByUsername(dto.getUserId());
			Set<UUID> ids = Collections.singleton(idmIdentityDto.getId());
			bulkActionDto.setIdentifiers(ids);

			Map<String, Object> properties = new HashMap<>();
			properties.put(BscIdentityBusinessCardExport.BUSINESS_CARD_CODE, params);
			bulkActionDto.setProperties(properties);
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkActionDto);
			LOG.info("bulk action: " + processAction);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private void transformAttrsToMap(Map<Object, Object> params, IdmFormInstanceDto formInstance, Map<UUID, IdmFormAttributeDto> attributes) {
		formInstance.getValues().forEach(idmFormValueDto -> {
			IdmFormAttributeDto attr = attributes.getOrDefault(idmFormValueDto.getFormAttribute(), null);
			if (attr == null) {
				return;
			}
			if (attr.getCode().equals(saveToHddAttrName)) {
				params.put(attr.getCode(), idmFormValueDto.getBooleanValue());
			} else if (attr.getCode().equals(titlesBeforeAttrName)) {
				if (!StringUtils.isEmpty(idmFormValueDto.getShortTextValue())) {
					params.put(attr.getCode(), idmFormValueDto.getShortTextValue() + " ");
				}
			} else if (attr.getCode().equals(titlesAfterAttrName)) {
				if (!StringUtils.isEmpty(idmFormValueDto.getShortTextValue())) {
					params.put(attr.getCode(), ", " + idmFormValueDto.getShortTextValue());
				}
			} else {
				params.put(attr.getCode(), idmFormValueDto.getShortTextValue());
			}
		});
	}
}
