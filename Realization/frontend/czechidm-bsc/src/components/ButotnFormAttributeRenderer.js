import React from 'react';
import classNames from 'classnames';
//
import {Advanced, Basic} from 'czechidm-core';

/**
 * Button form value component
 *
 * @author Roman Kucera
 */
export default class ButtonFormAttributeRenderer extends Advanced.AbstractFormAttributeRenderer {

  constructor(props, context) {
    super(props, context);
  }

  /**
   * Returns true, when multi value mode is supported
   *
   * @return {boolean}
   */
  supportsMultiple() {
    return false;
  }

  forward() {
    const {values} = this.props;
    if (values && values.length === 1) {
      this.context.history.push(values[0].value);
    }
  }

  renderSingleInput(originalValues) {
    const {attribute, values, validationErrors, className, style} = this.props;
    const showOriginalValue = !!originalValues;
    // create button
    return (
        <div className="form-group">
          <Basic.Button
              type="button"
              level="primary"
              onClick={this.forward.bind(this)}>
            {this.getLabel(null, showOriginalValue)}
          </Basic.Button>
        </div>
    );
  }
}
