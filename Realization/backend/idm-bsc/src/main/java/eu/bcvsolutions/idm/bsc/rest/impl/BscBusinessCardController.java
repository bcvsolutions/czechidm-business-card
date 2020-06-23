package eu.bcvsolutions.idm.bsc.rest.impl;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.bsc.domain.BscGroupPermission;
import eu.bcvsolutions.idm.bsc.dto.BscBusinessCardDto;
import eu.bcvsolutions.idm.bsc.service.api.BscBusinessCardService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * @author Roman Kucera
 */

@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/identities/business-cards")
@Api(
		value = BscBusinessCardController.TAG,
		tags = {BscBusinessCardController.TAG},
		description = "Operation with Business cards",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class BscBusinessCardController implements BaseDtoController<BscBusinessCardDto> {

	protected static final String TAG = "Business cards";

	@Autowired
	private BscBusinessCardService businessCardService;

	@Autowired
	public BscBusinessCardController() {
		super();
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/{date}/{contractId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + BscGroupPermission.BSC_BUSINESS_CARD_ADMIN + "')")
	@ApiOperation(
			value = "Business card detail for contract",
			nickname = "getBusinessCardForContract",
			response = BscBusinessCardDto.class,
			tags = {BscBusinessCardController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = BscGroupPermission.BSC_BUSINESS_CARD_ADMIN, description = "")})
			})
	public ResponseEntity<?> getForContract(
			@ApiParam(value = "Identity's identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Date", required = true)
			@PathVariable @NotNull String date,
			@ApiParam(value = "Contract", required = true)
			@PathVariable String contractId) {
		return new ResponseEntity<>(businessCardService.getBusinessCard(backendId, date, contractId), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/{date}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + BscGroupPermission.BSC_BUSINESS_CARD_ADMIN + "')")
	@ApiOperation(
			value = "Business card detail",
			nickname = "getBusinessCard",
			response = BscBusinessCardDto.class,
			tags = {BscBusinessCardController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = BscGroupPermission.BSC_BUSINESS_CARD_ADMIN, description = "")})
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "Identity's identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Business card's uuid identifier.", required = true)
			@PathVariable @NotNull String date) {
		return new ResponseEntity<>(businessCardService.getBusinessCard(backendId, date, null), HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + BscGroupPermission.BSC_BUSINESS_CARD_ADMIN + "')")
	@ApiOperation(
			value = "Print Business card",
			nickname = "postBusinessCard",
			response = BscBusinessCardDto.class,
			tags = {BscBusinessCardController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = BscGroupPermission.BSC_BUSINESS_CARD_ADMIN, description = "")})
			})
	public ResponseEntity<?> post(@Valid @RequestBody BscBusinessCardDto dto) {
		businessCardService.printBusinessCard(dto);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
