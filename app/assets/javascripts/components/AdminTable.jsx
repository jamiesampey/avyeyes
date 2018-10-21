import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';

const styles = theme => ({
  root: {
  },

});


class AdminTable extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const { classes } = this.props;

    return (
      <div>
        Admin table placeholder
      </div>
    );
  }
}

AdminTable.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AdminTable);