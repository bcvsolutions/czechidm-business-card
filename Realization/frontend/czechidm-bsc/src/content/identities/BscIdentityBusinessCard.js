import React from 'react';
import {Basic, Domain} from 'czechidm-core';
import BscIdentityBusinessCardTable from './BscIdentityBusinessCardTable';

/**
 * @author Roman Kučera
 */
export default class BscIdentityBusinessCard extends Basic.AbstractContent {

  getContentKey() {
    return 'bsc:content.identity.business-card';
  }

  getNavigationKey() {
    return 'profile-business-card';
  }

  render() {
    return (
        <div className="tab-pane-table-body">
          {this.renderContentHeader({style: {marginBottom: 0}})}

          <Basic.Panel className="no-border last">
            <BscIdentityBusinessCardTable
                uiKey="bsc-identity-business-card"
                entityId={this.props.match.params.entityId}/>
          </Basic.Panel>
        </div>
    );
  }
}

BscIdentityBusinessCard.propTypes = {};
BscIdentityBusinessCard.defaultProps = {};
