package eu.bcvsolutions.idm.bsc.dto;

import java.time.ZonedDateTime;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * Dto which is used for data transfer between BE and FE
 * @author Roman Kucera
 */
public class BscBusinessCardDto extends AbstractDto {

	private static final long serialVersionUID = 1;

	private ZonedDateTime date;

	private Map<String, IdmIdentityContractDto> contracts;

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
	public BscBusinessCardDto(ZonedDateTime date, Map<String, IdmIdentityContractDto> contracts, IdmFormInstanceDto formInstance) {
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

	public Map<String, IdmIdentityContractDto> getContracts() {
		return contracts;
	}

	public void setContracts(Map<String, IdmIdentityContractDto> contracts) {
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
