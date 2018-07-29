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
import { getRequestParam, parseApiResponse } from "../Util";

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

    this.filterAvalanches = this.filterAvalanches.bind(this);
    this.toggleMenu = this.toggleMenu.bind(this);
    this.setCursorStyle = this.setCursorStyle.bind(this);
  }

  componentWillMount() {
    this.viewer = new Cesium.Viewer(this.cesiumContainer, Config.cesiumViewerOptions);
    this.controller = new CesiumController(this.viewer);
    this.eventHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);

    this.viewer.camera.moveEnd.addEventListener(() => {
      if (!(this.state.currentReport || this.state.avalancheSpotlight)) {
        this.filterAvalanches();
      }
    });

    this.eventHandler.setInputAction(movement => {
      // this.form.hideReadOnlyForm();

      let pick = this.viewer.scene.pick(movement.position);
      if (Cesium.defined(pick) && pick.id.name) {
        // $("#avyMouseHoverTitle").hide();

        let selectedAvalanche = pick.id;
        let avalancheUrl = "/api/avalanche/" + selectedAvalanche.id;
        let editKeyParam = getRequestParam("edit");
        if (editKeyParam) avalancheUrl += "?edit=" + editKeyParam;

        fetch(avalancheUrl)
          .then(response => {
            return parseApiResponse(response);
          })
          .then(data => {
            if (pick.id.billboard) {
              // clicked on a pin, add the path and fly to it
              this.controller.removeAllEntities();
              // this.avalancheSpotlight = true;
              this.controller.addAvalancheAndFlyTo(data);

            } else {
              // clicked on a path, display details
              this.setCursorStyle("wait");

              if (data.hasOwnProperty("viewable")) {
                console.info(`Received admin details for avalanche ${selectedAvalanche.id}`);
                // this.form.enableAdminControls().then(() => {
                // this.form.displayReadWriteForm(data);
                // this.currentReport = new AvyReport(this);
                // });
              } else if (data.hasOwnProperty("submitterEmail")) {
                console.info(`Received read-write details for avalanche ${selectedAvalanche.id}`);
                // this.form.displayReadWriteForm(data);
                // this.currentReport = new AvyReport(this);
              } else {
                console.info(`Received read-only details for avalanche ${selectedAvalanche.id}`);
                // this.form.displayReadOnlyForm(movement.position, data);
              }
            }
          })
          .catch(error => {
            console.error(`Failed to fetch details for avalanche ${selectedAvalanche.id}. Error: ${error}`);
          });

      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);


    this.setState({
      menuOpen: false,
      avalancheSpotlight: false,
      currentReport: '',
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
        return parseApiResponse(response);
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

  filterAvalanches() {
    let boundingBox = this.controller.getBoundingBox();

    let searchQueryString = "/api/avalanche/search?latMax=" + boundingBox[0] + "&latMin=" + boundingBox[1] + "&lngMax=" + boundingBox[2] + "&lngMin=" + boundingBox[3]
      + "&camAlt=" + this.viewer.camera.positionCartographic.height
      + "&camLng=" + Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.longitude)
      + "&camLat=" + Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.latitude);
      // TODO get avalanche filter fields working once the filter form is setup
      // + "&fromDate=" + $("#avyFilterFromDate").val() + "&toDate=" + $("#avyFilterToDate").val()
      // + "&avyType=" + $("#avyFilterType").val() + "&trigger=" + $("#avyFilterTrigger").val() + "&interface=" + $("#avyFilterInterface").val()
      // + "&rSize=" + $("#avyFilterRsizeValue").val() + "&dSize=" + $("#avyFilterDsizeValue").val()
      // + "&numCaught=" + $("#avyFilterNumCaught").val() + "&numKilled=" + $("#avyFilterNumKilled").val();

    fetch(searchQueryString)
      .then(response => {
        return parseApiResponse(response);
      })
      .then(data => {
        this.controller.addAvalanches(data);
      })
      .catch(error => {
        console.error(`Failed to filter avalanches. Error ${error}`);
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