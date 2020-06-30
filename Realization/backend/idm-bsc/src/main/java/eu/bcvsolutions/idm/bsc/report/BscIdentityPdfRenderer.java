package eu.bcvsolutions.idm.bsc.report;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.bsc.templates.FOPProcessor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
	private BscConfiguration bscConfiguration;

	@Override
	@SuppressWarnings("unchecked")
	public InputStream render(RptReportDto report) {
		try (JsonParser jParser = getMapper().getFactory().createParser(getReportData(report))) {
			int rowNum = 0;
			//
			byte[] bytes = new byte[0];
			if (jParser.nextToken() == JsonToken.START_ARRAY && jParser.nextToken() == JsonToken.START_OBJECT) {
				// write single entity
				// TODO check when this will run for multiple users
//				while (jParser.nextToken() == JsonToken.START_OBJECT) {
					Map<String, Object> item = getMapper().readValue(jParser, Map.class);

					try {
						bytes = new FOPProcessor().generateByteA("fop-businessCard", item, "cs");
					} catch (IOException e) {
						LOG.error("Not able to load template", e);
					}
					try (InputStream is = new ByteArrayInputStream(bytes)) {
						String mimeType = "application/pdf";
						ResponseEntity.BodyBuilder response = ResponseEntity
								.ok()
								.contentLength(is.available())
								.header(HttpHeaders.CONTENT_DISPOSITION, String.format("inline; filename=\"%s\"", "vizitka"));
						// append media type, if it's filled
						if (StringUtils.isNotBlank(mimeType)) {
							response = response.contentType(MediaType.valueOf(mimeType));
						}

						//
						if ((boolean) item.getOrDefault("saveToHdd", false)) {
							LOG.info("Save to disk");
							saveToHdd(bytes);
						}
					} catch (IOException e) {
						throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, e);
					}
//				}
			}
			// close and return input stream
			return getInputStream(bytes);
		} catch (IOException e) {
			e.printStackTrace();
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

	private void saveToHdd(byte[] bytes) {
		String savePath = bscConfiguration.getSavePath();
		if (!StringUtils.isBlank(savePath)) {
			// TODO add some date suffix
			File targetFile = new File(savePath);
			try (OutputStream outStream = new FileOutputStream(targetFile)) {
				outStream.write(bytes);
			} catch (IOException e) {
				LOG.error("Can't write to file", e);
			}
		}
	}
}
