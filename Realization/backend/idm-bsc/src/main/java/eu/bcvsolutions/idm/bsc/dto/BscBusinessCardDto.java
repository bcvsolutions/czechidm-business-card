package eu.bcvsolutions.idm.bsc.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * @author Roman Kucera
 */
public class BscBusinessCardDto extends AbstractDto {

	private static final long serialVersionUID = 1;

	private ZonedDateTime date;

	private Map<String, String> contracts;

	private IdmFormInstanceDto formInstance;

	private String userId;

	private String selectedContract;

	public BscBusinessCardDto() {
	}

	/**
	 * Create DTO with date, contracts and form instance
	 * @param date We will find valid contracts for this date
	 * @param contracts List of valid contracts
	 * @param formInstance Form instance with values
	 */
	public BscBusinessCardDto(ZonedDateTime date, Map<String, String> contracts, IdmFormInstanceDto formInstance) {
		this.date = date;
		this.contracts = contracts;
		this.formInstance = formInstance;
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public void setDate(ZonedDateTime date) {
		this.date = date;
	}

	public Map<String, String> getContracts() {
		return contracts;
	}

	public void setContracts(Map<String, String> contracts) {
		this.contracts = contracts;
	}

	public IdmFormInstanceDto getFormInstance() {
		return formInstance;
	}

	public void setFormInstance(IdmFormInstanceDto formInstance) {
		this.formInstance = formInstance;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSelectedContract() {
		return selectedContract;
	}

	public void setSelectedContract(String selectedContract) {
		this.selectedContract = selectedContract;
	}
}
