import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import Config from '../Config';
import MenuButton from "./MenuButton";
import MenuDrawer from "./MenuDrawer";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";

import 'cesium/Widgets/widgets.css';
import '../../stylesheets/AvyEyesClient.scss';

import {withStyles} from '@material-ui/core/styles';
import CesiumController from "../CesiumController";


const styles = theme => ({
  root: {
    width: '100%',
    height: '100%',
    overflow: 'hidden',
  },
});


class AvyEyesClient extends React.Component {

  constructor() {
    super();
    this.toggleMenu = this.toggleMenu.bind(this);
  }

  componentWillMount() {
    this.viewer = new Cesium.Viewer('cesiumContainer', Config.cesiumViewerOptions);
    this.controller = new CesiumController(this.viewer);
    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);

    this.setState({
      menuOpen: false,
    });
  }

  componentDidMount() {
    let extIdUrlParam = window.location.pathname.substr(1); // remove initial path slash

    if (!extIdUrlParam) {
      this.controller.geolocateAndFlyTo();
      return;
    }

    fetch(`/api/avalanche/${extIdUrlParam}`)
      .then(response => {
        if (response.status === 200) {
          return response.json();
        } else {
          throw new Error(response.statusText)
        }
      })
      .then(data => {
        this.setState({
          avalancheSpotlight: true,
        });
        this.controller.addAvalancheAndFlyTo(data);
      })
      .catch(error => {
        this.controller.geolocateAndFlyTo();
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
      <div className={classes.root}>
        <MenuDrawer showDrawer={menuOpen} menuToggle={this.toggleMenu} />
        <MenuButton menuToggle={this.toggleMenu} />
        <EyeAltitude viewer={this.viewer} />
        <ResetViewButton controller={this.controller} />
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);