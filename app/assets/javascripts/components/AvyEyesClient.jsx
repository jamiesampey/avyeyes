import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import classNames from 'classnames';
import Config from '../Config';
import ReportButton from "./ReportButton";
import InfoBar from "./InfoBar";
import HelpDialog from "./HelpDialog";

import 'cesium/Widgets/widgets.css';
import '../../stylesheets/AvyEyesClient.scss';

import {checkStatusAndParseJson} from "../Util";
import ReportDrawer from "./ReportDrawer";
import CesiumView from "./CesiumView";

const reportDrawerWidth = 350;

const styles = theme => ({
  root: {
    flexGrow: 1,
    zIndex: 1,
    overflow: 'hidden',
    position: 'relative',
    display: 'flex',
    width: '100%',
    height: '100%',
  },
  content: {
    flexGrow: 1,
    width: '100%',
    height: '100%',
    overflow: 'hidden',
    backgroundColor: theme.palette.background.default,
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  contentLeft: {
    marginLeft: -(reportDrawerWidth+1),
    padding: 0,
    height: '100%',
  },
  contentShift: {
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.easeOut,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  contentShiftLeft: {
    marginLeft: 0,
    padding: 0,
  },
});


class AvyEyesClient extends React.Component {

  constructor(props) {
    super(props);

    this.showHelp = this.showHelp.bind(this);

    fetch('/api/clientData')
      .then(response => {
        return checkStatusAndParseJson(response);
      })
      .then(data => {
        this.setState({clientData: data});
      })
      .catch(error => {
        console.error(`Unable to retrieve client data bundle from server. Error: ${error}`);
      });

    window.fbAsyncInit = function() {
      FB.init({
        appId: Config.facebookAppId,
        xfbml: true,
        version: 'v3.1',
      });
      console.debug('Facebook async init succeeded');
    };

    (function(d, s, id) {
      let js, fjs = d.getElementsByTagName(s)[0];
      if (d.getElementById(id)) return;
      js = d.createElement(s); js.id = id;
      js.src = "//connect.facebook.net/en_US/sdk.js";
      fjs.parentNode.insertBefore(js, fjs);
    }(document, 'script', 'facebook-jssdk'));

    this.state = {
      reportDrawerOpen: false,
      cesiumController: null,
      currentAvalanche: null,
      infoMessage: null,
      help: null,
    };
  }

  showHelp(params) {
    this.setState({
      help: {
        title: params.title,
        content: params.content,
      }
    })
  }

  render() {
    const { classes } = this.props;
    if (!this.state.clientData) return null;

    return (
      <div className={classes.root}>
        <ReportDrawer
          drawerOpen={this.state.reportDrawerOpen}
          clientData={this.state.clientData}
          controller={this.state.cesiumController}
          drawingComplete={avalanche => this.setState({
            currentAvalanche: avalanche,
            reportDrawerOpen: false,
          })
          }
        />
        <main className={classNames(classes.content, classes.contentLeft, {
            [classes.contentShift]: this.state.reportDrawerOpen,
            [classes.contentShiftLeft]: this.state.reportDrawerOpen,
          })}
        >

          <CesiumView
            clientData={this.state.clientData}
            currentAvalanche={this.state.currentAvalanche}
            setCurrentAvalanche={avalanche => this.setState({ currentAvalanche: avalanche })}
            setController={cesiumController => this.setState({ cesiumController: cesiumController })}
            showHelp={this.showHelp}
          />

          { this.state.help &&
            <HelpDialog
              title={this.state.help.title}
              contentString={this.state.help.content}
              closeCallback={() => this.setState({ help: null }) }
            />
          }

          <InfoBar
            open={Boolean(this.state.infoMessage)}
            message={this.state.infoMessage}
            duration={15}
            closeable
            closeCallback={() => this.setState({ infoMessage: null }) }
          />

          <ReportButton
            visible={!this.state.reportDrawerOpen}
            startReport={() => { this.setState({reportDrawerOpen: true}) }}
          />
        </main>
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);