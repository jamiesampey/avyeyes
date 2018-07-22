import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import Config from './Config';

import 'cesium/Widgets/widgets.css';
import '../stylesheets/AvyEyesClient.scss';
import MenuButton from "./MenuButton";
import MenuDrawer from "./MenuDrawer";


import classNames from 'classnames';
import {withStyles} from '@material-ui/core/styles';

const drawerWidth = 300;

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  appFrame: {
    zIndex: 1,
    overflow: 'hidden',
    position: 'relative',
    display: 'flex',
    width: '100%',
    height: '100%',
  },
  hide: {
    display: 'none',
  },
  content: {
    flexGrow: 1,
    width: '100%',
    height: '100%',
    overflow: 'hidden',
    backgroundColor: theme.palette.background.default,
    padding: theme.spacing.unit * 3,
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.leavingScreen,
    }),
  },
  'content-left': {
    marginLeft: -drawerWidth,
    padding: 0,
    height: '100%',
  },
  contentShift: {
    transition: theme.transitions.create('margin', {
      easing: theme.transitions.easing.easeOut,
      duration: theme.transitions.duration.enteringScreen,
    }),
  },
  'contentShift-left': {
    marginLeft: 0,
    padding: 0,
  },
  cesium: {
    height: '100%;',
    width: '100%;',
    margin: 0,
    padding: 0,
    overflow: 'hidden',
  },
});


class AvyEyesClient extends React.Component {

  constructor() {
    super();
    Cesium.Ion.defaultAccessToken = Config.cesiumAccessToken;
  }

  componentWillMount() {
    this.setState({
      menuOpen: false,
    });
  }

  componentDidMount() {
    this.cesiumViewer = new Cesium.Viewer("cesiumContainer", Config.cesiumViewerOptions);
    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
  }

  toggleMenu() {
    let previousMenuState = this.setState.menuOpen;
    this.setState({
      menuOpen: !previousMenuState,
    });
  }

  render() {
    const { classes, theme } = this.props;
    const { menuOpen } = this.state;

    return (
      <div id="AvyEyes" className={classes.root}>
        <div className={classes.appFrame}>
          <MenuDrawer drawerWidth={drawerWidth} toggle={this.toggleMenu} />
          <MenuButton toggle={this.toggleMenu} />
          <main
            className={classNames(classes.content, classes[`content-left`], {
              [classes.contentShift]: menuOpen,
              [classes[`contentShift-left`]]: menuOpen,
            })}
          >
            <div id="cesiumContainer" className={classes.cesium}/>
          </main>
        </div>
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);