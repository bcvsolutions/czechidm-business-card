package eu.bcvsolutions.idm.bsc.report;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.bsc.domain.BscGroupPermission;
import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Generation of business card for one or multiple users
 *
 * @author Roman Kucera
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component("bscIdentityBusinessCardBulkAction")
@Description("Generate business card")
public class BscIdentityBusinessCardExport extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String REPORT_NAME = "bsc-identity-business-card-bulk-action";

	public static final String SAVE_TO_HDD_CODE = "save-to-hdd";
	public static final String BUSINESS_CARD_CODE = "business-card";

	private final RptReportService reportService;
	private final AttachmentManager attachmentManager;
	private final ObjectMapper mapper;
	//
	private File tempFile;
	private JsonGenerator jsonGenerator;
	private UUID relatedReport;

	@Autowired
	private IdmIdentityService identityService;

	public BscIdentityBusinessCardExport(RptReportService reportService,
										 AttachmentManager attachmentManager, ObjectMapper mapper) {
		this.reportService = reportService;
		this.attachmentManager = attachmentManager;
		this.mapper = mapper;
	}

	@Override
	protected boolean start() {
		boolean start = super.start();
		createReport();
		return start && relatedReport != null;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		Map<String, Object> properties = getProperties();
		if (properties.get(BUSINESS_CARD_CODE) == null) {
			// TODO calculate properties via BusinessCardService
		} else {
			properties = (Map<String, Object>) properties.get(BUSINESS_CARD_CODE);
		}

		final OperationResult result = new OperationResult();
		//
		//
		try {
			final JsonGenerator jGen = getJsonGenerator();
			jGen.writeObject(properties);
			result.setState(OperationState.EXECUTED);
		} catch (IOException e) {
			result.setState(OperationState.EXCEPTION);
			result.setCause(e.getMessage());
		}
		//
		return result;
	}

	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		final OperationResult superResult = super.end(result, ex);
		if (!close()) {
			superResult.setState(OperationState.EXCEPTION);
			superResult.setCause("Cannot close temp file");
		}
		//
		try {
			RptReportDto report = finishReport(superResult);
			// Adds attachment metadata to the operation result (for download attachment
			// directly from bulk action modal dialog).
			addAttachmentMetadata(result, report);
		} catch (FileNotFoundException e) {
			superResult.setState(OperationState.EXCEPTION);
			superResult.setCause(e.getMessage());
		}
		//
		return superResult;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		IdmFormAttributeDto businessCardData = new IdmFormAttributeDto(
				SAVE_TO_HDD_CODE,
				SAVE_TO_HDD_CODE,
				PersistentType.BOOLEAN);
		formAttributes.add(businessCardData);
		return formAttributes;
	}

	private JsonGenerator getJsonGenerator() throws IOException {
		if (jsonGenerator == null) {
			FileOutputStream outputStream = new FileOutputStream(getTempFile());
			jsonGenerator = getMapper().getFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			jsonGenerator.writeStartArray();
		}
		return jsonGenerator;
	}

	private RptReportDto finishReport(OperationResult result) throws FileNotFoundException {
		RptReportDto report = reportService.get(relatedReport);
		//
		FileInputStream fis = new FileInputStream(tempFile);
		IdmAttachmentDto attachment = createAttachment(report, fis);
		report.setData(attachment.getId());
		report.setResult(result);
		//
		return reportService.save(report);
	}

	protected void createReport() {
		RptReportDto report = new RptReportDto();
		report.setLongRunningTask(getLongRunningTaskId());
		report.setExecutorName(BscIdentityReportExecutor.NAME);
		report.setName(REPORT_NAME);

		this.relatedReport = reportService.save(report).getId();
	}

	private boolean close() {
		try {
			if (this.jsonGenerator != null && !jsonGenerator.isClosed()) {
				getJsonGenerator().writeEndArray();
				this.jsonGenerator.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private ObjectMapper getMapper() {
		return this.mapper;
	}

	private File getTempFile() {
		if (this.tempFile == null) {
			tempFile = attachmentManager.createTempFile();
		}
		return this.tempFile;
	}

	protected IdmAttachmentDto createAttachment(RptReportDto report, InputStream jsonData) {
		IdmAttachmentDto attachmentDto = new IdmAttachmentDto();
		attachmentDto.setDescription(getDescription());
		attachmentDto.setName(getName());
		attachmentDto.setMimetype(MediaType.APPLICATION_JSON_UTF8.toString());
		attachmentDto.setInputData(jsonData);
		return attachmentManager.saveAttachment(report, attachmentDto);
	}

	/**
	 * Adds attachment metadata to the operation result (for download attachment
	 * directly from bulk action modal dialog).
	 *
	 * @param result
	 */
	private void addAttachmentMetadata(OperationResult result, RptReportDto report) {

		IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
		OperationResult taskResult = task.getResult();

		if (OperationState.EXECUTED == taskResult.getState()) {
			ResultModel model = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_PARTITIAL_DOWNLOAD,
					ImmutableMap.of(//
							AttachableEntity.PARAMETER_DOWNLOAD_URL,
							MessageFormat.format("rpt/reports/{0}/render?renderer=bsc-identity-pdf-renderer", report.getId()),
							AttachableEntity.PARAMETER_OWNER_ID, report.getId(), //
							AttachableEntity.PARAMETER_OWNER_TYPE, report.getClass().getName()//
					));//

			taskResult.setModel(model);
			getLongRunningTaskService().save(task);
		}
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(BscGroupPermission.BSC_BUSINESS_CARD_ADMIN);
	}

	@Override
	public String getName() {
		return REPORT_NAME;
	}
}
