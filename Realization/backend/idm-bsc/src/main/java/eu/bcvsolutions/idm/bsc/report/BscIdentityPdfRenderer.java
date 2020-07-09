package eu.bcvsolutions.idm.bsc.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.bsc.templates.FOPProcessor;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.renderer.RendererRegistrar;

/**
 * Renderer for {@link BscIdentityBusinessCardExport} report.
 *
 * @author Roman Kucera
 */
@Component(BscIdentityPdfRenderer.NAME)
@Description(BscAbstractPdfRenderer.RENDERER_EXTENSION) // will be show as format for download
public class BscIdentityPdfRenderer extends BscAbstractPdfRenderer implements RendererRegistrar {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BscIdentityPdfRenderer.class);

	public static final String NAME = "bsc-identity-pdf-renderer";

	@Autowired
	private FOPProcessor fopProcessor;

	@Override
	@SuppressWarnings("unchecked")
	public InputStream render(RptReportDto report) {
		try (JsonParser jParser = getMapper().getFactory().createParser(getReportData(report))) {
			if (jParser.nextToken() == JsonToken.START_ARRAY) {
				List<File> partialFiles = new ArrayList();
				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					BscBusinessCardReportDto item = getMapper().readValue(jParser, BscBusinessCardReportDto.class);
					jParser.finishToken();
					if (item.getPdf() != null) {
						partialFiles.add(item.getPdf());
					}
				}
				byte[] bytes;
				try {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					fopProcessor.concatToPDF(partialFiles, outputStream);
					bytes = outputStream.toByteArray();
					outputStream.close();
					return getInputStream(bytes);
				} catch (Exception ex) {
					LOG.error("An error occurred while generating business card.", ex);
				}
			}
		} catch (IOException e) {
			LOG.error("Error during rendering", e);
		}
		return null;
	}

	@Override
	public String[] register(String reportName) {
		if (BscIdentityReportExecutor.NAME.equals(reportName)) {
			return new String[]{getName()};
		}
		return new String[0];
	}

	@Override
	public String getName() {
		return NAME;
	}

}
