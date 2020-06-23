package eu.bcvsolutions.idm.bsc.dto;

import java.time.ZonedDateTime;
import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * @author Roman Kucera
 */
public class BscBusinessCardDto extends AbstractDto {

	private static final long serialVersionUID = 1;

	private ZonedDateTime date;

	private List<IdmIdentityContractDto> contracts;

	private IdmFormInstanceDto formInstance;

	public BscBusinessCardDto() {
	}

	/**
	 * Create DTO with date, contracts and form instance
	 * @param date We will find valid contracts for this date
	 * @param contracts List of valid contracts
	 * @param formInstance Form instance with values
	 */
	public BscBusinessCardDto(ZonedDateTime date, List<IdmIdentityContractDto> contracts, IdmFormInstanceDto formInstance) {
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

	public List<IdmIdentityContractDto> getContracts() {
		return contracts;
	}

	public void setContracts(List<IdmIdentityContractDto> contracts) {
		this.contracts = contracts;
	}

	public IdmFormInstanceDto getFormInstance() {
		return formInstance;
	}

	public void setFormInstance(IdmFormInstanceDto formInstance) {
		this.formInstance = formInstance;
	}
}
