import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Chip from "@material-ui/core/Chip";
import {checkStatusAndParseJson} from "../Util";

const styles = theme => ({

});

class AdminUserChip extends React.Component {

  constructor(props) {
    super(props);

    fetch('/api/currentUser')
      .then(response => {
        return checkStatusAndParseJson(response);
      })
      .then(data => {
        if (data.email) this.setState({ userEmail: data.email });
      })
      .catch(error => {
        console.error(`Error retrieving currentUser: ${error}`);
      });

    this.state = {
      userEmail: null,
    };
  }

  render() {
    const { classes } = this.props;

    return ( this.state.userEmail &&
      <Chip
        label={this.state.userEmail}
        onDelete={() => window.location = '/auth/logout'}
        color="primary"
      />
    )
  };
}

AdminUserChip.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(AdminUserChip);