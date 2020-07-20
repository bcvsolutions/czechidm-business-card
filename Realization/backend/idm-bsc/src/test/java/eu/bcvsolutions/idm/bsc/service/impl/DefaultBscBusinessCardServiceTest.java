package eu.bcvsolutions.idm.bsc.service.impl;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * @author Roman Kucera
 */
@Transactional
public class DefaultBscBusinessCardServiceTest extends AbstractIntegrationTest {

	@Autowired
	private BscBusinessCardService businessCardService;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmIdentityService identityService;

	@Test
	public void getBusinessCard() {
		IdmIdentityDto businessUser = getHelper().createIdentity("businessUser");
		IdmFormAttributeDto treeNodeFormAttribute = getHelper().createEavAttribute(DefaultBscBusinessCardService.BUSINESS_CARD_DEPARTMENT_EAV_NAME,
				IdmTreeNode.class, PersistentType.TEXT);

		LocalDate now = LocalDate.now();

		BscBusinessCardDto businessCard = businessCardService.getBusinessCard(businessUser.getUsername(), now.toString(), null);

		assertNotNull(businessCard);

		List<IdmIdentityContractDto> allValidForDate = contractService.findAllValidForDate(businessUser.getId(), now, false);
		assertNotNull(allValidForDate);
		assertEquals(allValidForDate.size(), businessCard.getContracts().size());

		IdmIdentityContractDto mainContract = allValidForDate.stream().filter(IdmIdentityContractDto::isMain).findFirst().orElse(null);
		assertNotNull(mainContract);
		assertEquals(mainContract.getId().toString(), businessCard.getSelectedContract());

		assertEquals(now.toString(), businessCard.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));

		assertNotNull(businessCard.getFormInstance());
		assertEquals(8, businessCard.getFormInstance().getValues().size());
	}

	@Test
	public void printBusinessCard() {
		// set config properties.
		getHelper().setConfigurationValue(BscConfiguration.BCK_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.FOP_CONFIG_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/fop.conf");
		getHelper().setConfigurationValue(BscConfiguration.IMAGE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.SAVE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.TEMPLATE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/fop-businessCard.xml");
		getHelper().setConfigurationValue(BscConfiguration.TMP_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");

		IdmIdentityDto businessUser = getHelper().createIdentity("businessUser");
		businessUser.setExternalCode("54321");
		businessUser = identityService.save(businessUser);

		IdmFormAttributeDto treeNodeFormAttribute = getHelper().createEavAttribute(DefaultBscBusinessCardService.BUSINESS_CARD_DEPARTMENT_EAV_NAME,
				IdmTreeNode.class, PersistentType.TEXT);

		LocalDate now = LocalDate.now();

		BscBusinessCardDto businessCard = businessCardService.getBusinessCard(businessUser.getUsername(), now.toString(), null);
		businessCard.setUserId(businessUser.getId().toString());

		ResponseEntity<IdmBulkActionDto> bulkAction = businessCardService.printBusinessCard(businessCard);

		assertNotNull(bulkAction);
	}
}