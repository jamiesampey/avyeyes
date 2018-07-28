import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import Config from '../Config';
import MenuButton from "./MenuButton";
import MenuDrawer from "./MenuDrawer";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";
import MouseBee from "./MouseBee";

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
    this.cesiumContainer = document.getElementById('cesiumContainer');
    this.toggleMenu = this.toggleMenu.bind(this);
    this.setCursorStyle = this.setCursorStyle.bind(this);
  }

  componentWillMount() {
    this.viewer = new Cesium.Viewer(this.cesiumContainer, Config.cesiumViewerOptions);
    this.controller = new CesiumController(this.viewer);
    this.eventHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);

    // this.eventHandler.setInputAction((movement) => {
    //   // this.form.hideReadOnlyForm();
    //
    //   let pick = this.viewer.scene.pick(movement.position);
    //   if (Cesium.defined(pick) && pick.id.name) {
    //     $("#avyMouseHoverTitle").hide();
    //
    //     let selectedAvalanche = pick.id;
    //     let avalancheUrl = "/avalanche/" + selectedAvalanche.id;
    //     let editKeyParam = this.getRequestParam("edit");
    //     if (editKeyParam) avalancheUrl += "?edit=" + editKeyParam;
    //
    //     $.getJSON(avalancheUrl, (data) => {
    //       if (pick.id.billboard) {
    //         // click on a pin, add the path and fly to it
    //         this.removeAllEntities();
    //         this.avalancheSpotlight = true;
    //         this.addAvalancheAndFlyTo(data);
    //       } else {
    //         // click on a path, display details
    //         $("#cesiumContainer").css("cursor", "wait");
    //         if (data.hasOwnProperty("viewable")) {
    //           this.form.enableAdminControls().then(function () {
    //             this.form.displayReadWriteForm(data);
    //             this.currentReport = new AvyReport(this);
    //           }.bind(this));
    //         } else if (data.hasOwnProperty("submitterEmail")) {
    //           this.form.displayReadWriteForm(data);
    //           this.currentReport = new AvyReport(this);
    //         } else {
    //           this.form.displayReadOnlyForm(movement.position, data);
    //         }
    //       }
    //     }).fail(function(jqxhr, textStatus, error) {
    //       console.log("AvyEyes error: " + error);
    //     });
    //   }
    // }, Cesium.ScreenSpaceEventType.LEFT_CLICK);


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

  setCursorStyle(style) {
    this.cesiumContainer.style.cursor = style;
    this.setState({
      cursorStyle: style,
    })
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
        <MouseBee viewer={this.viewer} eventHandler={this.eventHandler} cursorStyle={this.state.cursorStyle} setCursorStyle={this.setCursorStyle} />
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);