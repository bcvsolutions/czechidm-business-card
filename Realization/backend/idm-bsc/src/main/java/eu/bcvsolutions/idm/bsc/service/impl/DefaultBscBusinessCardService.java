package eu.bcvsolutions.idm.bsc.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.bsc.BscModuleDescriptor;
import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.report.BscIdentityBusinessCardExport;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.bsc.util.ImageUtils;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * This service is used from {@Link BscBusinessCardController}
 *
 * @author Roman Kucera
 */
@Service
public class DefaultBscBusinessCardService implements BscBusinessCardService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBscBusinessCardService.class);

	@Autowired
	protected IdmIdentityContractService identityContractService;
	@Autowired
	protected IdmIdentityService identityService;
	@Autowired
	protected BulkActionManager bulkActionManager;
	@Autowired
	protected BscConfiguration bscConfiguration;
	@Autowired
	protected FormService formService;

	protected String nameAttrName = "name";
	protected String titlesBeforeAttrName = "titlesBefore";
	protected String titlesAfterAttrName = "titlesAfter";
	protected String departmentAttrName = "department";
	protected String departmentButtonAttrName = "departmentButton";
	protected String positionAttrName = "position";
	protected String personalNumberAttrName = "personalNumber";
	protected String saveToHddAttrName = "saveToHdd";

	protected String embeddedFormAttrName = "formAttribute";

	@Autowired
	public DefaultBscBusinessCardService() {
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

		IdmFormAttributeDto nameAttr = getFormAttr(nameAttrName, false, PersistentType.SHORTTEXT);
		IdmFormAttributeDto titlesBeforeAttr = getFormAttr(titlesBeforeAttrName, false, PersistentType.SHORTTEXT);
		IdmFormAttributeDto titlesAfterAttr = getFormAttr(titlesAfterAttrName, false, PersistentType.SHORTTEXT);
		IdmFormAttributeDto departmentAttr = getFormAttr(departmentAttrName, false, PersistentType.TEXT);
		departmentAttr.setFaceType(BaseFaceType.TEXTAREA);
		IdmFormAttributeDto departmentEditAttr = getFormAttr(departmentButtonAttrName, false, PersistentType.SHORTTEXT);
		departmentEditAttr.setFaceType("BSC-BUTTON");
		IdmFormAttributeDto positionAttr = getFormAttr(positionAttrName, false, PersistentType.SHORTTEXT);
		IdmFormAttributeDto personalNumberAttr = getFormAttr(personalNumberAttrName, false, PersistentType.SHORTTEXT);
		IdmFormAttributeDto saveToHddAttr = getFormAttr(saveToHddAttrName, false, PersistentType.BOOLEAN);

		IdmFormDefinitionDto definitionDto = new IdmFormDefinitionDto();
		definitionDto.addFormAttribute(nameAttr);
		definitionDto.addFormAttribute(titlesBeforeAttr);
		definitionDto.addFormAttribute(titlesAfterAttr);
		definitionDto.addFormAttribute(departmentAttr);
		definitionDto.addFormAttribute(departmentEditAttr);
		definitionDto.addFormAttribute(positionAttr);
		definitionDto.addFormAttribute(personalNumberAttr);
		definitionDto.addFormAttribute(saveToHddAttr);
		definitionDto.setModule(BscModuleDescriptor.MODULE_ID);
		definitionDto.setType(BscBusinessCardDto.class.getName());
		definitionDto.setCode(BscModuleDescriptor.MODULE_ID);

		IdmFormValueDto nameValue = getShortTextValue(nameAttr, identityDto.getFirstName() + " " + identityDto.getLastName());
		IdmFormValueDto titlesBeforeValue = getShortTextValue(titlesBeforeAttr, identityDto.getTitleBefore());
		IdmFormValueDto titlesAfterValue = getShortTextValue(titlesAfterAttr, identityDto.getTitleAfter());


		String department = "";
		String position = "nedefinováno";
		String treeNodeEavUrl = "";
		if (!StringUtils.isBlank(contractId)) {
			IdmIdentityContractDto contractDto = identityContractService.get(contractId);
			if (contractDto != null && !StringUtils.isBlank(contractDto.getPosition())) {
				position = contractDto.getPosition();
			}
			if (contractDto != null && contractDto.getWorkPosition() != null) {
				// get department from eav
				Optional<IdmFormValueDto> value = formService.getValues(contractDto.getWorkPosition(), IdmTreeNodeDto.class, "businessCardName").stream().findFirst();
				if (value.isPresent()) {
					department = value.get().getStringValue();
				}
				// URL for editing department directly in EAV
				treeNodeEavUrl = "/tree/nodes/" + contractDto.getWorkPosition() + "/eav";
			}
		}

		IdmFormValueDto departmentValue = getStringValue(departmentAttr, department);

		IdmFormValueDto departmentButtonValue = getShortTextValue(departmentEditAttr, treeNodeEavUrl);
		IdmFormValueDto positionValue = getShortTextValue(positionAttr, position);
		IdmFormValueDto personalNumberValue = getShortTextValue(personalNumberAttr, identityDto.getExternalCode());
		IdmFormValueDto saveToHddValue = getBoolValue(saveToHddAttr, true);

		List<IdmFormValueDto> values = new ArrayList<>(Arrays.asList(nameValue, titlesBeforeValue, titlesAfterValue, departmentValue,
				departmentButtonValue, positionValue, personalNumberValue, saveToHddValue));

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

	protected IdmFormValueDto getShortTextValue(IdmFormAttributeDto attr, String value) {
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

	protected IdmFormValueDto getStringValue(IdmFormAttributeDto attr, String value) {
		IdmFormValueDto nameValue = new IdmFormValueDto();
		nameValue.setStringValue(value);
		nameValue.setFormAttribute(attr.getId());
		nameValue.setPersistentType(PersistentType.TEXT);
		// set attribute to embedded
		Map<String, BaseDto> embedded = new HashMap<>();
		embedded.put(embeddedFormAttrName, attr);
		nameValue.setEmbedded(embedded);
		return nameValue;
	}

	protected IdmFormAttributeDto getFormAttr(String code, boolean isReadOnly, PersistentType type) {
		IdmFormAttributeDto attributeDto = new IdmFormAttributeDto();
		attributeDto.setCode(code);
		attributeDto.setPersistentType(type);
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

		IdmIdentityDto idmIdentityDto = getIdentity(identity);
		LocalDate localDate = LocalDate.parse(date);
		List<IdmIdentityContractDto> allValidForDate = identityContractService.findAllValidForDate(idmIdentityDto.getId(), localDate, false);
		allValidForDate.sort(Comparator.comparing(IdmIdentityContractDto::getPosition));
		Map<String, IdmIdentityContractDto> contracts = new LinkedHashMap<>();
		allValidForDate.forEach(idmIdentityContractDto -> contracts.put(idmIdentityContractDto.getId().toString(), idmIdentityContractDto));

		BscBusinessCardDto businessCardDto = new BscBusinessCardDto();
		if (StringUtils.isBlank(contractId) && !allValidForDate.isEmpty()) {
			// default contract is main.
			IdmIdentityContractDto mainContract = allValidForDate.stream().filter(IdmIdentityContractDto::isMain).findFirst().orElse(null);
			if (mainContract != null) {
				contractId = mainContract.getId().toString();
			}
		}
		String finalContractId = contractId;
		boolean isSelectedStillAvailable = allValidForDate.stream().anyMatch(contractDto -> contractDto.getId().toString().equals(finalContractId));
		if (!isSelectedStillAvailable) {
			IdmIdentityContractDto contractDto = allValidForDate.stream().findFirst().orElse(null);
			if (contractDto != null) {
				contractId = contractDto.getId().toString();
			}
		}

		businessCardDto.setSelectedContract(contractId);
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
	public ResponseEntity<IdmBulkActionDto> printBusinessCard(BscBusinessCardDto dto) {
		LOG.info("We will generate business card");

		IdmIdentityDto idmIdentityDto = getIdentity(dto.getUserId());
		Map<String, Object> params = prepareAndTransformData(dto, idmIdentityDto);

		// RUN REPORT
		IdmBulkActionDto bulkActionDto = bulkActionManager.getAvailableActions(IdmIdentity.class).
				stream()
				.filter(action -> action.getName().equals(BscIdentityBusinessCardExport.REPORT_NAME))
				.findFirst()
				.orElse(null);
		if (bulkActionDto != null) {
			bulkActionDto = prepareAndRunBulkAction(idmIdentityDto.getId(), bulkActionDto, params);
			return new ResponseEntity<>(bulkActionDto, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}

	protected IdmBulkActionDto prepareAndRunBulkAction(UUID userId, IdmBulkActionDto bulkActionDto, Map<String, Object> params) {
		Set<UUID> ids = Collections.singleton(userId);
		bulkActionDto.setIdentifiers(ids);

		Map<String, Object> properties = new HashMap<>();
		properties.put(BscIdentityBusinessCardExport.BUSINESS_CARD_CODE, params);
		properties.put(BscIdentityBusinessCardExport.SAVE_TO_HDD_CODE, params.get(saveToHddAttrName));
		bulkActionDto.setProperties(properties);
		bulkActionDto = bulkActionManager.processAction(bulkActionDto);
		LOG.info("bulk action: " + bulkActionDto);
		return bulkActionDto;
	}

	@Override
	public Map<String, Object> prepareAndTransformData(BscBusinessCardDto dto, IdmIdentityDto identityDto) {
		Map<String, Object> params = new HashMap<>();

		transformAttrsToMap(params, dto);
		makeRoundCorners(params, identityDto);

		params.put("nameSize", ((String) params.getOrDefault(nameAttrName, "") + params.getOrDefault(titlesAfterAttrName, "") +
				params.getOrDefault(titlesBeforeAttrName, "")).length() > 38 ? "8.6pt" : "11pt");

		String bckPath = bscConfiguration.getBckPath();
		if (!StringUtils.isBlank(bckPath)) {
			params.put("backgroundImage", bckPath + "defaulBck.png");
		}

		return params;
	}

	protected void makeRoundCorners(Map<String, Object> params, IdmIdentityDto identityDto) {
		// round corners
		String imagePath = bscConfiguration.getImagePath();
		String tmpPath = bscConfiguration.getTmpPath();
		String randTmp;
		if (!StringUtils.isBlank(imagePath) && !StringUtils.isBlank(tmpPath)) {
			try {
				if (!StringUtils.isBlank(identityDto.getExternalCode())) {
					imagePath += identityDto.getExternalCode() + ".png";
				}
				randTmp = tmpPath + UUID.randomUUID().toString() + ".png";
				ImageUtils.writeToFile(
						ImageUtils.makeRoundedCorner(ImageUtils.readFromFile(imagePath), 40), "png", randTmp);
				params.put("imagePath", randTmp);
			} catch (IOException e) {
				LOG.warn("Can not load image", e);
			}
		}
	}

	protected void transformAttrsToMap(Map<String, Object> params, BscBusinessCardDto dto) {
		IdmFormInstanceDto formInstance = dto.getFormInstance();
		Map<UUID, IdmFormAttributeDto> attributes = formInstance.getFormDefinition().getFormAttributes()
				.stream()
				.collect(Collectors.toMap(IdmFormAttributeDto::getId, attr -> attr));

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
			} else if (attr.getCode().equals(departmentAttrName)) {
				if (!StringUtils.isEmpty(idmFormValueDto.getStringValue())) {
					AtomicInteger suffix = new AtomicInteger(1);
					Arrays.asList(idmFormValueDto.getStringValue().split("\\n")).forEach(s -> {
						params.put("department" + suffix, s);
						suffix.getAndIncrement();
					});
				}
			} else {
				params.put(attr.getCode(), idmFormValueDto.getShortTextValue());
			}
		});
	}

	private IdmIdentityDto getIdentity(String identifier) {
		IdmIdentityDto idmIdentityDto = identityService.getByUsername(identifier);
		if (idmIdentityDto == null) {
			idmIdentityDto = identityService.get(identifier);
		}
		return idmIdentityDto;
	}
}
