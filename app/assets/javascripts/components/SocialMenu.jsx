import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {CopyToClipboard} from 'react-copy-to-clipboard';
import Menu from "@material-ui/core/Menu";
import MenuItem from "@material-ui/core/MenuItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import ListItemText from "@material-ui/core/ListItemText";
import LinkIcon from "@material-ui/icons/Link";
import SvgIcon from "@material-ui/core/SvgIcon";
import InfoBar from "./InfoBar";


const styles = theme => ({
  shareIcon: {
    margin: 0,
  },
  snackbarRoot: {
    backgroundColor: 'red',
  },
});

class SocialMenu extends React.Component {

  constructor(props) {
    super(props);

    this.openFacebookDialog = this.openFacebookDialog.bind(this);
    this.openTwitterDialog = this.openTwitterDialog.bind(this);

    this.state = {
      copyLinkOpen: false,
    }
  }

  openFacebookDialog() {
    const { avalanche, clientData, closeCallback } = this.props;

    closeCallback();

    FB.ui({
      method: 'share_open_graph',
      action_type: 'og.shares',
      action_properties: JSON.stringify({
        object: {
          'og:url': avalanche.extUrl,
          'og:title': avalanche.title,
          'og:description': avalanche.comments,
          'og:image': "https://" + clientData.s3.bucket + ".s3.amazonaws.com/avalanches/" + avalanche.extId + "/screenshot.jpg",
          "og:image:width": 800,
          "og:image:height": 600
        }
      })
    });
  };

  openTwitterDialog() {
    const { avalanche, closeCallback } = this.props;

    closeCallback();

    let shareURL = "http://twitter.com/share?";

    let params = {
      url: avalanche.extUrl,
      text: avalanche.title,
    };
    for(let prop in params) shareURL += '&' + prop + '=' + encodeURIComponent(params[prop]);

    const width = 550;
    const height = 450;
    let left = (window.screen.width / 2) - ((width / 2) + 10);
    let top = (window.screen.height / 2) - ((height / 2) + 50);

    window.open(shareURL, '', `left=${left},top=${top},width=${width},height=${height},menubar=0,toolbar=0,scrollbars=0,location=0,directories=0,resizable=0`);
  };

  render() {
    const { classes, anchorEl, clientData, avalanche, closeCallback } = this.props;

    return (
      <div>
        <Menu
          anchorEl={anchorEl}
          open={Boolean(anchorEl)}
          onClose={closeCallback}
        >
          <MenuItem onClick={this.openFacebookDialog} disabled={typeof FB === 'undefined'}>
            <ListItemIcon>
              <SvgIcon className={classes.shareIcon}>
                <path fill="#3B5998"
                      d="M22.676 0H1.324C.593 0 0 .593 0 1.324v21.352C0 23.408.593 24 1.324 24h11.494v-9.294H9.689v-3.621h3.129V8.41c0-3.099 1.894-4.785 4.659-4.785 1.325 0 2.464.097 2.796.141v3.24h-1.921c-1.5 0-1.792.721-1.792 1.771v2.311h3.584l-.465 3.63H16.56V24h6.115c.733 0 1.325-.592 1.325-1.324V1.324C24 .593 23.408 0 22.676 0"/>
              </SvgIcon>
            </ListItemIcon>
            <ListItemText inset primary="Facebook"/>
          </MenuItem>
          <MenuItem onClick={this.openTwitterDialog}>
            <SvgIcon className={classes.shareIcon}>
              <path fill="#1DA1F2"
                    d="M23.954 4.569c-.885.389-1.83.654-2.825.775 1.014-.611 1.794-1.574 2.163-2.723-.951.555-2.005.959-3.127 1.184-.896-.959-2.173-1.559-3.591-1.559-2.717 0-4.92 2.203-4.92 4.917 0 .39.045.765.127 1.124C7.691 8.094 4.066 6.13 1.64 3.161c-.427.722-.666 1.561-.666 2.475 0 1.71.87 3.213 2.188 4.096-.807-.026-1.566-.248-2.228-.616v.061c0 2.385 1.693 4.374 3.946 4.827-.413.111-.849.171-1.296.171-.314 0-.615-.03-.916-.086.631 1.953 2.445 3.377 4.604 3.417-1.68 1.319-3.809 2.105-6.102 2.105-.39 0-.779-.023-1.17-.067 2.189 1.394 4.768 2.209 7.557 2.209 9.054 0 13.999-7.496 13.999-13.986 0-.209 0-.42-.015-.63.961-.689 1.8-1.56 2.46-2.548l-.047-.02z"/>
            </SvgIcon>
            <ListItemText inset primary="Twitter"/>
          </MenuItem>
          <CopyToClipboard text={avalanche.extUrl}>
            <MenuItem onClick={() => this.setState({ copyLinkOpen: true, }, closeCallback)}>
              <ListItemIcon>
                <LinkIcon className={classes.shareIcon}/>
              </ListItemIcon>
              <ListItemText inset primary="Copy Link"/>
            </MenuItem>
          </CopyToClipboard>
        </Menu>
        <InfoBar
          open={this.state.copyLinkOpen}
          message={clientData.help.copyLinkMessage}
          duration={5}
          closeCallback={() => this.setState({copyLinkOpen: false})}
        />
      </div>
    );
  }
}

SocialMenu.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SocialMenu);