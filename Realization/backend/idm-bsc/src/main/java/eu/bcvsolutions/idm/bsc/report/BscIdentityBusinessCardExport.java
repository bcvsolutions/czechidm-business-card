package eu.bcvsolutions.idm.bsc.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.bsc.domain.BscGroupPermission;
import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.bsc.templates.FOPProcessor;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(BscIdentityBusinessCardExport.class);

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
	private List<String> personalNumbers = new LinkedList<>();

	List<File> partialFiles = new ArrayList();

	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private BscConfiguration bscConfiguration;
	@Autowired
	@Lazy
	private BscBusinessCardService businessCardService;
	@Autowired
	private FOPProcessor fopProcessor;

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
		personalNumbers.add(dto.getExternalCode());

		// get data for user or load them from properties
		Map<String, Object> properties = getProperties();
		if (properties.get(BUSINESS_CARD_CODE) == null) {
			BscBusinessCardDto businessCard = businessCardService.getBusinessCard(dto.getUsername(), LocalDate.now().toString(), null);
			properties = businessCardService.prepareAndTransformData(businessCard, dto);
		} else {
			properties = (Map<String, Object>) properties.get(BUSINESS_CARD_CODE);
		}

		final OperationResult result = new OperationResult();

		//Generate partial pdf
		File pf = null;
		try {
			pf = fopProcessor.convertToAreaTreeXML(properties);
			partialFiles.add(pf);
		} catch (IOException e) {
			LOG.error("Not able to load template", e);
		}

		try {
			// Create dto for data transfer into renderer
			BscBusinessCardReportDto businessCardReportDto = new BscBusinessCardReportDto();
			businessCardReportDto.setUserIdentifier(dto.getId());
			businessCardReportDto.setPdf(pf);
			businessCardReportDto.setParams(properties);

			final JsonGenerator jGen = getJsonGenerator();
			jGen.writeObject(businessCardReportDto);
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
		// Save pdf to disk
		boolean saveToHdd = (Boolean) getProperties().getOrDefault(SAVE_TO_HDD_CODE, false);
		if (saveToHdd) {
			LOG.info("Save to disk");
			saveToHdd();
		}

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
		FileInputStream fis = new FileInputStream(getTempFile());
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

	/**
	 * Save pdf to drive
	 */
	private void saveToHdd() {
		StringBuilder filePath = new StringBuilder();
		String personalNumber = personalNumbers.stream().findFirst().orElse("");

		String savePath = bscConfiguration.getSavePath();
		if (!StringUtils.isBlank(savePath)) {
			filePath.append(savePath);
			filePath.append(personalNumber);
			filePath.append('_');
			filePath.append(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
			filePath.append(".pdf");
			byte[] bytes = null;
			File targetFile = new File(filePath.toString());
			try (OutputStream outStream = new FileOutputStream(targetFile)) {
				ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
				fopProcessor.concatToPDF(partialFiles, pdfOutput);
				bytes = pdfOutput.toByteArray();
				pdfOutput.close();

				outStream.write(bytes);
			} catch (IOException e) {
				LOG.error("Can't write to file", e);
			}
		}
	}
}
