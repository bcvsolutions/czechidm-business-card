package eu.bcvsolutions.idm.bsc.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.MediaType;

import eu.bcvsolutions.idm.rpt.api.renderer.AbstractReportRenderer;

/**
 * Render report into pdf
 *
 * @author Roman Kucera
 */
public abstract class BscAbstractPdfRenderer extends AbstractReportRenderer {

	public static final String RENDERER_EXTENSION = "pdf";

	@Override
	public MediaType getFormat() {
		return new MediaType("application", "pdf");
	}

	@Override
	public String getExtension() {
		return RENDERER_EXTENSION;
	}

	/**
	 * Get pdf as input stream
	 *
	 * @param pdf
	 * @return input stream for pdf
	 * @throws IOException
	 */
	protected InputStream getInputStream(byte[] pdf) throws IOException {
		//
		// save temp file
		File temp = getAttachmentManager().createTempFile();
		try (FileOutputStream outputStream = new FileOutputStream(temp)) {
			outputStream.write(pdf);
			return new FileInputStream(temp);
		}
	}
}
