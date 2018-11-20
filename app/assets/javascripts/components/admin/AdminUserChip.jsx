import React from 'react';
import Chip from "@material-ui/core/Chip";
import {checkStatusAndParseJson} from "../../Util";

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
    return ( this.state.userEmail &&
      <Chip
        label={this.state.userEmail}
        onDelete={() => window.location = '/auth/logout'}
        color="primary"
      />
    )
  };
}

export default AdminUserChip;