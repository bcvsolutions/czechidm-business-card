package eu.bcvsolutions.idm.bsc.report;

import org.springframework.context.annotation.Description;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;

/**
 * This executor only exists so that {@link BscIdentityBusinessCardExport} and {@link BscIdentityPdfRenderer} are able to
 * work together.
 *
 * @author Roman Kucera
 */
@Component
@Description("Generating of business cards")
@Order(Integer.MAX_VALUE)
public class BscIdentityReportExecutor extends AbstractReportExecutor {

	public static final String NAME = "bsc-identity-report";

	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isDisabled() {
		return true;
	}

}
