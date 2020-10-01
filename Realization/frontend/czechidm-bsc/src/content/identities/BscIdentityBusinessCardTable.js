import React from 'react';
import PropTypes from 'prop-types';
import {Advanced, Basic, Utils, Managers, Domain} from 'czechidm-core';
import {connect} from 'react-redux';
import {BscBusinessCardManager} from '../../redux';
import moment from 'moment';
import uuid from 'uuid';

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
    this.setState({showLoading: true, longRunningTask: null, hideFields: false});
    this.context.store.dispatch(businessCardManager.fetchBackendForDate(entityId, moment().format('YYYY-MM-DD'), uiKey, (card, error) => {
      if (!error) {
        this.setState({formInstanceKey: uuid.v1(), showLoading: false})
      }
    }));
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
      this.context.store.dispatch(businessCardManager.generateBusinessCard(businessCardEntity, uiKey + "generate", (bulkAction, error) => {
        if (!error) {
          this.setState({longRunningTask: bulkAction})
        }
      }));
    }, (err) => {
      // Rejected
      // must be defined, but can be empty (otherwise uncaught exception to console is shown)
      console.log("denied")
    });
  }

  onContractChange(contract, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({showLoading: true});
    const entity = this.refs.form.getData();
    const {entityId, businessCardEntity} = this.props;
    this.refs.contracts.setValue(null);
    if (businessCardEntity) {
      this.context.store.dispatch(businessCardManager.fetchBackendForDateAndContract(entityId, entity.date, contract.value, uiKey, (card, error) => {
        if (!error) {
          this.setState({showLoading: false})
        }
      }));
    }
  }

  onDateChange(date, event) {
    if (event) {
      event.preventDefault();
    }
    this.setState({showLoading: true, hideFields: false});
    if (date) {
      const entity = this.refs.form.getData();
      const {entityId, businessCardEntity} = this.props;
      if (businessCardEntity) {
        this.context.store.dispatch(businessCardManager.fetchBackendForDateAndContract(entityId, date.format('YYYY-MM-DD'), entity.contracts, uiKey, (card, error) => {
          console.log(error);
          if (!error) {
            this.setState({showLoading: false})
          }
        }));
      }
    } else {
      this.setState({hideFields: true, showLoading: false});
    }
  }

  closeModal() {
    const {entityId} = this.props;
    this.context.store.dispatch(businessCardManager.fetchBackendForDate(entityId, moment().format('YYYY-MM-DD'), uiKey, (card, error) => {
      if (!error) {
        this.setState({formInstanceKey: uuid.v1(), showLoading: false})
      }
    }));
    this.setState({longRunningTask: null, showLoading: false});
  }

  render() {
    const {uiKey, _permissions, businessCardEntity} = this.props;
    const {formInstanceKey, longRunningTask, showLoading, hideFields} = this.state;
    let formInstance = new Domain.FormInstance({});
    let options = [];
    if (businessCardEntity) {
      formInstance = new Domain.FormInstance(businessCardEntity.formInstance.formDefinition, businessCardEntity.formInstance.values);
      if (Object.keys(businessCardEntity.contracts).length > 0) {
        Object.keys(businessCardEntity.contracts).map(key => {
          const contract = businessCardEntity.contracts[key];
          let position = '';
          let validFrom = '';
          let validTill = '';
          if (contract.position) {
            position = contract.position;
          }
          if (contract.validFrom) {
            validFrom = contract.validFrom;
          }
          if (contract.validTill) {
            validTill = contract.validTill;
          }
          options.push({
            value: key,
            niceLabel: this.i18n('contract-nice-label',{ position: position, validFrom: validFrom, validTill: validTill })
          });
        });
      }
    }
    if (!businessCardEntity) {
      return (
          <Basic.Loading isStatic show/>
      );
    }

    return (
        <div>
          <Basic.Confirm ref="confirm-save" level="success"/>
          <Basic.AbstractForm
              ref="form" data={{date: businessCardEntity.date, contracts: businessCardEntity.selectedContract}}>
            <Basic.Col lg={4} className="col-lg-4" style={{padding: 0}}>
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
                  clearable={false}
                  readOnly={hideFields}/>
              <Basic.Panel showLoading={showLoading} className="no-border no-margin">
                <Advanced.EavForm
                    ref="formInstance"
                    key={uuid.v1()}
                    formInstance={formInstance}
                    useDefaultValue={false}
                    rendered={!hideFields}/>
              </Basic.Panel>
            </Basic.Col>
          </Basic.AbstractForm>
          <Basic.Modal
              bsSize="large"
              show={longRunningTask}
              onHide={this.closeModal.bind(this)}
              backdrop="static"
              keyboard>
            <Basic.Modal.Body
                style={{padding: 0, marginBottom: -20}}
                rendered={longRunningTask}>
              <Advanced.LongRunningTask
                  entityIdentifier={longRunningTask ? longRunningTask.longRunningTaskId : null}
                  // header={ this.i18n(`${backendBulkAction.module }:eav.bulk-action.${ backendBulkAction.name }.label`)}
                  showProperties={false}
                  // onComplete={() => this.reload()}
                  footerButtons={
                    <Basic.Button
                        level="link"
                        onClick={this.closeModal.bind(this)}>
                      {this.i18n('button.close')}
                    </Basic.Button>
                  }/>
            </Basic.Modal.Body>
          </Basic.Modal>
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
  // showLoading: PropTypes.bool
};

BscIdentityBusinessCardTable.defaultProps = {
  // showLoading: true
};

function select(state, component) {
  const {entityId} = component;
  return {
    _permissions: identityManager.getPermissions(state, null, entityId),
    identity: identityManager.getEntity(state, entityId),
    businessCardEntity: Managers.DataManager.getData(state, uiKey),
    // showLoading: Managers.DataManager.isShowLoading(state, uiKey)
  };
}

export default connect(select, null, null, {forwardRef: true})(BscIdentityBusinessCardTable);
