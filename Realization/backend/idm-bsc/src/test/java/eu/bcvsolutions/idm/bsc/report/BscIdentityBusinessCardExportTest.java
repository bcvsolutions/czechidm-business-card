package eu.bcvsolutions.idm.bsc.report;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * @author Roman Kucera
 */
@Transactional
public class BscIdentityBusinessCardExportTest extends AbstractBulkActionTest {

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private RptReportService reportService;
	@Autowired
	ReportManager reportManager;

	@Before
	public void login() {
		getHelper().loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void processBulkAction() {
		// set config properties.
		getHelper().setConfigurationValue(BscConfiguration.BCK_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.FOP_CONFIG_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/fop.conf");
		getHelper().setConfigurationValue(BscConfiguration.IMAGE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.SAVE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");
		getHelper().setConfigurationValue(BscConfiguration.TEMPLATE_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/fop-businessCard.xml");
		getHelper().setConfigurationValue(BscConfiguration.TMP_PATH, System.getProperty("user.dir") + "/src/test/resources/fileTemplate/");

		List<IdmIdentityDto> identities = this.createIdentities(5);

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, BscIdentityBusinessCardExport.REPORT_NAME);
		Assert.assertEquals(NotificationLevel.SUCCESS, bulkAction.getLevel());

		Set<UUID> ids = this.getIdFromList(identities);
		bulkAction.setIdentifiers(this.getIdFromList(identities));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 5l, null, null);

		for (UUID id : ids) {
			IdmIdentityDto identityDto = identityService.get(id);
			assertNotNull(identityDto);
		}

		RptReportFilter reportFilter = new RptReportFilter();
		reportFilter.setText(BscIdentityBusinessCardExport.REPORT_NAME);

		List<RptReportDto> content = reportService.find(reportFilter, null).getContent();

		Assert.assertFalse(content.isEmpty());
		Assert.assertEquals(1, content.size());

		RptReportDto reportDto = content.get(0);

		RptRenderedReportDto render = reportManager.render(reportDto, BscIdentityPdfRenderer.NAME);

		Assert.assertNotNull(render);
	}
}