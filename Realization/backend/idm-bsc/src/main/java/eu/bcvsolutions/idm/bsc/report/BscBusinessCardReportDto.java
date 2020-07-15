package eu.bcvsolutions.idm.bsc.report;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for data transfer between {@link BscIdentityBusinessCardExport} and {@link BscIdentityPdfRenderer}
 * @author Roman Kucera
 */
public class BscBusinessCardReportDto implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID userIdentifier;
	private File pdf;
	private Map<String, Object> params;

	public UUID getUserIdentifier() {
		return userIdentifier;
	}

	public void setUserIdentifier(UUID userIdentifier) {
		this.userIdentifier = userIdentifier;
	}

	public File getPdf() {
		return pdf;
	}

	public void setPdf(File pdf) {
		this.pdf = pdf;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
