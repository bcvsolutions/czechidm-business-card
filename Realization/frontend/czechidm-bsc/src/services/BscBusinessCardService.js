import {Services, Utils} from 'czechidm-core';

/**
 *
 * @author Roman Kučera
 */
export default class BscBusinessCardService extends Services.AbstractService {

  constructor() {
    super();
  }

  getApiPath() {
    return '/identities/business-cards';
  }

  getNiceLabel(document) {
    if (!document) {
      return '';
    }
    return document.name;
  }

  getGroupPermission() {
    return 'BUSINESSCARD';
  }

  supportsAuthorization() {
    return true;
  }

  /**
   * Get data for user and date
   */
  getBackendForDate(entityId, date) {
    return Services.RestApiService.get(this.getApiPath() + `/${entityId}/${date}`)
        .then(response => {
          return response.json();
        })
        .then(json => {
          if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
          }
          return json;
        });
  }

  /**
   * Get data for user and date and contract
   */
  getBackendForDateAndContract(entityId, date, contractId) {
    return Services.RestApiService.get(this.getApiPath() + `/${entityId}/${date}/${contractId}`)
        .then(response => {
          return response.json();
        })
        .then(json => {
          if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
          }
          return json;
        });
  }

  /**
   * Generate business card
   */
  generateBusinessCard(entity) {
    return Services.RestApiService.post(this.getApiPath(), entity)
        .then(response => {
          return response.json();
        })
        .then(json => {
          if (Utils.Response.hasError(json)) {
            throw Utils.Response.getFirstError(json);
          }
          return json;
        });
  }
}
