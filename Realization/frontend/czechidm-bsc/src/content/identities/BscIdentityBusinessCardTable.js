import React from 'react';
import PropTypes from 'prop-types';
import {Advanced, Basic, Utils, Managers, Domain} from 'czechidm-core';
import {connect} from 'react-redux';
import {BscBusinessCardManager} from '../../redux';
import moment from 'moment';

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
    this.context.store.dispatch(businessCardManager.fetchBackendForDate(entityId, moment().format('YYYY-MM-DD'), uiKey));
  }

  componentDidMount() {
    super.componentDidMount();
  }

  getContentKey() {
    return 'bsc:content.business-card';
  }

  save(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs['confirm-save'].show(
        this.i18n('generate.message'),
        this.i18n('generate.header')
    ).then(result => {
      // Confirmed
      console.log("generate")
      // Add actual values into dto and send it to BE
      let {businessCardEntity, entityId} = this.props;
      businessCardEntity.formInstance.values = this.refs.formInstance.getValues();
      businessCardEntity.userId = entityId;
      businessCardEntity.selectedContract = this.refs.form.getData().contracts;
      console.log(businessCardEntity);
      this.context.store.dispatch(businessCardManager.generateBusinessCard(businessCardEntity, uiKey));
    }, (err) => {
      // Rejected
      // must be defined, but can be empty (otherwise uncaught exception to console is shown)
      console.log("denied")
    });
  }

  onContractChange(...contract) {
    // TODO on first load form refs are empty
    const entity = this.refs.form.getData();
    console.log(entity);
    console.log(contract[0].value);
    console.log(this.refs.formInstance.getValues());
    const {entityId, businessCardEntity} = this.props;
    this.refs.contracts.setValue(null);
    if (businessCardEntity) {
      this.context.store.dispatch(businessCardManager.fetchBackendForDateAndContract(entityId, entity.date, contract[0].value, uiKey));
    }
  }

  onDateChange(date) {
    // TODO on first load form refs are empty
    if (date) {
      const entity = this.refs.form.getData();
      console.log(entity);
      console.log(date.format('YYYY-MM-DD'));
      console.log(this.refs.formInstance.getValues());
      const {entityId, businessCardEntity} = this.props;
      if (businessCardEntity) {
        this.context.store.dispatch(businessCardManager.fetchBackendForDateAndContract(entityId, date.format('YYYY-MM-DD'), entity.contracts, uiKey));
      }
    }
  }

  render() {
    const {uiKey, showLoading, _permissions, businessCardEntity} = this.props;
    let formInstance = new Domain.FormInstance({});
    let value = null;
    let options = [];
    console.log(businessCardEntity);
    console.log(showLoading);
    if (businessCardEntity) {
      formInstance = new Domain.FormInstance(businessCardEntity.formInstance.formDefinition, businessCardEntity.formInstance.values);
      // If there are some contracts use the first one as default option
      if (Object.keys(businessCardEntity.contracts).length > 0) {
        value = Object.entries(businessCardEntity.contracts)[0][0];
        Object.keys(businessCardEntity.contracts).map(key => options.push({
          value: key,
          niceLabel: businessCardEntity.contracts[key]
        }));
      }
    }
    if (!businessCardEntity || showLoading) {
      return (
          <Basic.Loading isStatic show/>
      );
    }

    return (
        <div>
          <Basic.Confirm ref="confirm-save" level="success"/>
          <Basic.AbstractForm
              ref="form" data={{date: businessCardEntity.date, contracts: value}}>
            <div>
              <Basic.DateTimePicker
                  mode="date"
                  ref="date"
                  label={this.i18n('date')}
                  onChange={this.onDateChange.bind(this)}/>
              <Basic.EnumSelectBox
                  ref="contracts"
                  options={options}
                  onChange={this.onContractChange.bind(this)}
                  label={this.i18n('contract')}
                  required
                  clearable={false}/>
              <Advanced.EavForm
                  ref="formInstance"
                  formInstance={formInstance}
                  useDefaultValue={true}/>
            </div>
          </Basic.AbstractForm>
          <Basic.PanelFooter>
            <Basic.Button
                hidden={false}
                onClick={this.save.bind(this)}
                level="success"
                showLoadingIcon
                showLoadingText={this.i18n('button.generate')}>
              {this.i18n('button.generate')}
            </Basic.Button>
          </Basic.PanelFooter>
        </div>
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
    // showLoading: Utils.Ui.isShowLoading(state, uiKey),
    _permissions: identityManager.getPermissions(state, null, entityId),
    identity: identityManager.getEntity(state, entityId),
    businessCardEntity: Managers.DataManager.getData(state, uiKey),
    showLoading: Managers.DataManager.isShowLoading(state, uiKey)
  };
}

export default connect(select, null, null, {forwardRef: true})(BscIdentityBusinessCardTable);
