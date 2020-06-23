import {Managers} from 'czechidm-core';
import {BscBusinessCardService} from '../services';

/**
 * @author Roman Kučera
 */
export default class BscBusinessCardManager extends Managers.DataManager {

  constructor() {
    super();
    this.service = new BscBusinessCardService();
    this.dataManager = new Managers.DataManager();
  }

  getModule() {
    return 'bsc';
  }

  getService() {
    return this.service;
  }

  /**
   * Get data for user and date
   */
  fetchBackendForDate(entityId, date, uiKey = null, cb = null) {
    return (dispatch) => {
      this.getService().getBackendForDate(entityId, date)
          .then(json => {
            if (cb) {
              cb(json, null);
            }
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            dispatch(this.receiveError({}, uiKey, error, cb));
          });
    };
  }
}
