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
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
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
 * This service is used from {@link eu.bcvsolutions.idm.bsc.rest.impl.BscBusinessCardController}
 *
 * @author Roman Kucera
 */
@Service
public class DefaultBscBusinessCardService implements BscBusinessCardService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultBscBusinessCardService.class);

	public static String BUSINESS_CARD_DEPARTMENT_EAV_NAME = "businessCardName";

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
	@Autowired
	protected IdmContractSliceService contractSliceService;

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
			if (contractDto == null) {
				contractDto = contractSliceService.get(contractId);
			}
			if (contractDto != null && !StringUtils.isBlank(contractDto.getPosition())) {
				position = contractDto.getPosition();
			}
			if (contractDto != null && contractDto.getWorkPosition() != null) {
				// get department from eav
				Optional<IdmFormValueDto> value = formService.getValues(contractDto.getWorkPosition(), IdmTreeNodeDto.class, BUSINESS_CARD_DEPARTMENT_EAV_NAME).stream().findFirst();
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

	@Override
	public BscBusinessCardDto getBusinessCard(String identity, String date, String contractId) {
		IdmIdentityDto idmIdentityDto = getIdentity(identity);
		LocalDate localDate = LocalDate.parse(date);
		List<IdmIdentityContractDto> allValidForDate = identityContractService.findAllValidForDate(idmIdentityDto.getId(), localDate, Boolean.FALSE);
		allValidForDate.sort(Comparator.comparing(IdmIdentityContractDto::getPosition));
		Map<String, IdmIdentityContractDto> contracts = new LinkedHashMap<>();
		allValidForDate.forEach(idmIdentityContractDto -> contracts.put(idmIdentityContractDto.getId().toString(), idmIdentityContractDto));

		// get slices
		IdmContractSliceFilter contractSliceFilter = new IdmContractSliceFilter();
		contractSliceFilter.setIdentity(idmIdentityDto.getId());
		List<IdmContractSliceDto> allSlicesForUser = contractSliceService.find(contractSliceFilter, null).getContent();
		// Find future slices
		allSlicesForUser.forEach(idmContractSliceDto -> {
			if (!idmContractSliceDto.isUsingAsContract() && idmContractSliceDto.isValid(localDate)) {
				contracts.put(idmContractSliceDto.getId().toString(), idmContractSliceDto);
				allValidForDate.add(idmContractSliceDto);
			}
		});

		BscBusinessCardDto businessCardDto = new BscBusinessCardDto();
		if (StringUtils.isBlank(contractId) && !allValidForDate.isEmpty()) {
			// default contract is main.
			IdmIdentityContractDto mainContract = allValidForDate.stream().filter(IdmIdentityContractDto::isMain).findFirst().orElse(null);
			if (mainContract != null) {
				contractId = mainContract.getId().toString();
			} else {
				contractId = allValidForDate.get(0).getId().toString();
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

	@Override
	public Map<String, Object> prepareAndTransformData(BscBusinessCardDto dto, IdmIdentityDto identityDto) {
		Map<String, Object> params = transformAttrsToMap(dto);
		makeRoundCorners(params, identityDto.getExternalCode());

		params.put("nameSize", ((String) params.getOrDefault(nameAttrName, "") + params.getOrDefault(titlesAfterAttrName, "") +
				params.getOrDefault(titlesBeforeAttrName, "")).length() > 38 ? "8.6pt" : "11pt");

		String bckPath = bscConfiguration.getBckPath();
		if (!StringUtils.isBlank(bckPath)) {
			params.put("backgroundImage", bckPath + "defaulBck.png");
		}

		return params;
	}

	/**
	 * Helper method for creating {@link IdmFormValueDto} in {@link #getFormInstance(IdmIdentityDto, String) getFormInstance}
	 *
	 * @param attr  {@link IdmFormAttributeDto} for which we want to create value
	 * @param value boolean value
	 * @return {@link IdmFormValueDto} for specific attribute and with specific value
	 */
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

	/**
	 * Helper method for creating {@link IdmFormValueDto} in {@link #getFormInstance(IdmIdentityDto, String) getFormInstance}
	 * This method create value with {@link PersistentType} SHORTEXT
	 *
	 * @param attr  {@link IdmFormAttributeDto} for which we want to create value
	 * @param value String value
	 * @return {@link IdmFormValueDto} for specific attribute and with specific value
	 */
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

	/**
	 * Helper method for creating {@link IdmFormValueDto} in {@link #getFormInstance(IdmIdentityDto, String) getFormInstance}
	 * This method create value with {@link PersistentType} TEXT
	 *
	 * @param attr  {@link IdmFormAttributeDto} for which we want to create value
	 * @param value String value
	 * @return {@link IdmFormValueDto} for specific attribute and with specific value
	 */
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

	/**
	 * Helper method for creating {@link IdmFormAttributeDto} in {@link #getFormInstance(IdmIdentityDto, String) getFormInstance}
	 *
	 * @param code       code for the attribute
	 * @param isReadOnly boolean value if attribute should be read only or not
	 * @param type       {@link PersistentType} of the attribute
	 * @return {@link IdmFormAttributeDto} with specific code and type
	 */
	protected IdmFormAttributeDto getFormAttr(String code, boolean isReadOnly, PersistentType type) {
		IdmFormAttributeDto attributeDto = new IdmFormAttributeDto();
		attributeDto.setCode(code);
		attributeDto.setPersistentType(type);
		attributeDto.setId(UUID.randomUUID());
		attributeDto.setReadonly(isReadOnly);
		return attributeDto;
	}

	/**
	 * It will create bulk action with specific params
	 *
	 * @param userId        this value is send into bulk action, so it will run bulk action for this one user
	 * @param bulkActionDto dto for {@link BscIdentityBusinessCardExport} bulk action
	 * @param params        Map with params for bulk action
	 * @return created bulk action which was started
	 */
	protected IdmBulkActionDto prepareAndRunBulkAction(UUID userId, IdmBulkActionDto bulkActionDto, Map<String, Object> params) {
		Set<UUID> ids = Collections.singleton(userId);
		bulkActionDto.setIdentifiers(ids);

		Map<String, Object> properties = new HashMap<>();
		properties.put(BscIdentityBusinessCardExport.BUSINESS_CARD_CODE, params);
		properties.put(BscIdentityBusinessCardExport.SAVE_TO_HDD_CODE, params.get(saveToHddAttrName));
		bulkActionDto.setProperties(properties);
		bulkActionDto = bulkActionManager.processAction(bulkActionDto);
		LOG.info("bulk action: [{}]", bulkActionDto);
		return bulkActionDto;
	}

	/**
	 * It will make round corners of the user's photo for business card
	 *
	 * @param params      Map with params, it will add new param with the rounded image
	 * @param fileIdentifier identifier of the file with picture for user e.g external number, username, ...
	 */
	protected void makeRoundCorners(Map<String, Object> params, String fileIdentifier) {
		// round corners
		String imagePath = bscConfiguration.getImagePath();
		String imageFileExtension = bscConfiguration.getImageFileExtension();
		String tmpPath = bscConfiguration.getTmpPath();
		String randTmp;
		if (!StringUtils.isBlank(imagePath) && !StringUtils.isBlank(tmpPath) && !StringUtils.isBlank(imageFileExtension)) {
			try {
				if (!StringUtils.isBlank(fileIdentifier)) {
					imagePath += fileIdentifier + "." + imageFileExtension;
				}
				randTmp = tmpPath + UUID.randomUUID().toString() + "." + imageFileExtension;
				// Saving as png, because jpg is not supported.
				ImageUtils.writeToFile(
						ImageUtils.makeRoundedCorner(ImageUtils.readFromFile(imagePath), 40), "png", randTmp);
				params.put("imagePath", randTmp);
			} catch (IOException e) {
				LOG.warn("Can not load image", e);
			}
		}
	}

	/**
	 * Transform {@link BscBusinessCardDto} into {@link Map}
	 *
	 * @param dto {@link BscBusinessCardDto} for transformation
	 * @return Map with attributes from {@link BscBusinessCardDto}
	 */
	protected Map<String, Object> transformAttrsToMap(BscBusinessCardDto dto) {
		Map<String, Object> params = new HashMap<>();
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
				transformDepartment(params, idmFormValueDto);
			} else {
				params.put(attr.getCode(), idmFormValueDto.getShortTextValue());
			}
		});
		return params;
	}

	/**
	 * Transform department attribute, we need to split the attribute into 4 pieces and put these peaces as individual params into Map
	 *
	 * @param params          {@link Map} with params
	 * @param idmFormValueDto {@link IdmFormValueDto} which will be transformed into params
	 */
	protected void transformDepartment(Map<String, Object> params, IdmFormValueDto idmFormValueDto) {
		if (!StringUtils.isEmpty(idmFormValueDto.getStringValue())) {
			AtomicInteger suffix = new AtomicInteger(1);
			Arrays.asList(idmFormValueDto.getStringValue().split("\\n")).forEach(s -> {
				params.put("department" + suffix, s);
				suffix.getAndIncrement();
			});
		}
	}

	/**
	 * Method for finding user by username or id
	 *
	 * @param identifier it could be username or String representation of UUID
	 * @return {@link IdmIdentityDto} which was found for specific identifier
	 */
	private IdmIdentityDto getIdentity(String identifier) {
		IdmIdentityDto idmIdentityDto = identityService.getByUsername(identifier);
		if (idmIdentityDto == null) {
			idmIdentityDto = identityService.get(identifier);
		}
		return idmIdentityDto;
	}
}
