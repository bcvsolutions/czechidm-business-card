import React, {PropTypes} from 'react';
import {Advanced, Basic, Utils, Managers, Domain} from 'czechidm-core';
import {connect} from 'react-redux';
import {BscBusinessCardManager} from '../../redux';
import _ from 'lodash';
import {DataManager} from "czechidm-core/src/redux";

/**
 * @author Roman Kučera
 */

const identityManager = new Managers.IdentityManager();
const businessCardManager = new BscBusinessCardManager();

const uiKey = 'business-card';

class BscIdentityBusinessCardTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    const {entityId} = this.props;
    this.context.store.dispatch(businessCardManager.fetchBackendForDate(entityId, "2020-06-22", uiKey));
  }

  componentDidMount() {
    super.componentDidMount();
  }

  componentWillMount() {
    this.setState({
      showLoading: false
    });
  }


  getContentKey() {
    return 'bsc:content.business-card';
  }

  save(identityId, event) {
  }

  render() {
    const {uiKey, showLoading, _permissions, businessCardEntity} = this.props;
    const identity = this.context.store.getState().security.userContext.id;
    let formInstance = new Domain.FormInstance({});
    console.log(businessCardEntity);
    if (businessCardEntity) {
      formInstance = new Domain.FormInstance(businessCardEntity.formInstance.formDefinition, businessCardEntity.formInstance.values);
    }
    return (
        <Basic.AbstractForm
            ref="form">
          <Basic.Loading isStatic showLoading={showLoading}/>
          {
            !businessCardEntity
            ||
            <div>
              <Basic.DateTimePicker
                  mode="date"
                  ref="date"
                  label={this.i18n('date')}
                  value={businessCardEntity.date}/>
              <div>
                <Advanced.EavForm
                    ref="formInstance"
                    formInstance={formInstance}
                    useDefaultValue={true}/>
              </div>
            </div>
          }
        </Basic.AbstractForm>
    );
  }
}

BscIdentityBusinessCardTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  entityId: PropTypes.string,
  showLoading: PropTypes.bool
};

BscIdentityBusinessCardTable.defaultProps = {
  showLoading: true
};

function select(state, component) {
  const {entityId} = component;
  return {
    showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}`),
    _permissions: identityManager.getPermissions(state, null, entityId),
    identity: identityManager.getEntity(state, entityId),
    businessCardEntity: Managers.DataManager.getData(state, uiKey),
  };
}

export default connect(select, null, null, {withRef: true})(BscIdentityBusinessCardTable);
