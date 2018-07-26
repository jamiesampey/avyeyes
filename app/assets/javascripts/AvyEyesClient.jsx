import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import Config from './Config';
import MenuButton from "./MenuButton";
import MenuDrawer from "./MenuDrawer";
import EyeAltitude from "./EyeAltitude";

import 'cesium/Widgets/widgets.css';
import '../stylesheets/AvyEyesClient.scss';

import {withStyles} from '@material-ui/core/styles';

const styles = theme => ({
  avyeyes: {
    width: '100%',
    height: '100%',
    overflow: 'hidden',
  },
  cesium: {
    height: '100%;',
    width: '100%;',
    overflow: 'hidden',
  },
});


class AvyEyesClient extends React.Component {

  constructor() {
    super();
    Cesium.Ion.defaultAccessToken = Config.cesiumAccessToken;
    this.toggleMenu = this.toggleMenu.bind(this);
  }

  componentWillMount() {
    Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3MjNkZTk5Yy1iMGViLTQ1M2YtOGFkZS1kZDIwNzNkMDE4YzUiLCJpZCI6MjE1NSwiaWF0IjoxNTMxNzk1MjM1fQ.A7eUkBj1ZtZHoOtyWJyGixTAXs7mXE6SVCA_lJod_-c';
    this.cesiumViewer = new Cesium.Viewer('cesiumContainer', Config.cesiumViewerOptions);
    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);

    this.setState({
      menuOpen: false,
    });
  }

  toggleMenu() {
    let previousMenuState = this.state.menuOpen;
    this.setState({
      menuOpen: !previousMenuState,
    });
  }

  render() {
    const { classes, theme } = this.props;
    const { menuOpen } = this.state;

    return (
      <div id="AvyEyes" className={classes.avyeyes}>
        <MenuDrawer showDrawer={menuOpen} menuToggle={this.toggleMenu} />
        <MenuButton menuToggle={this.toggleMenu} />
        <EyeAltitude cesiumViewer={this.cesiumViewer} />
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);