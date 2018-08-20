import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from 'cesium/Cesium';
import Config from '../Config';
import MenuButton from "./MenuButton";
import MenuDrawer from "./MenuDrawer";
import ReportButton from "./ReportButton";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";
import MouseBee from "./MouseBee";
import AvyCard from "./AvyCard";

import 'cesium/Widgets/widgets.css';
import '../../stylesheets/AvyEyesClient.scss';

import CesiumController from "../CesiumController";
import { getRequestParam, parseApiResponse } from "../Util";
import { FilterMenuPanel, ReportMenuPanel } from "../Constants";

const styles = theme => ({
  root: {
    width: '100%',
    height: '100%',
    overflow: 'hidden',
  },
});


class AvyEyesClient extends React.Component {

  constructor(props) {
    super(props);

    this.cesiumContainer = document.getElementById('cesiumContainer');
    this.viewer = new Cesium.Viewer(this.cesiumContainer, Config.cesiumViewerOptions);
    this.controller = new CesiumController(this.viewer);
    this.eventHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);

    this.filterAvalanches = this.filterAvalanches.bind(this);
    this.setCursorStyle = this.setCursorStyle.bind(this);
    this.renderAvyCard = this.renderAvyCard.bind(this);

    fetch('/api/clientData')
      .then(response => {
        return parseApiResponse(response);
      })
      .then(data => {
        this.setState({ clientData: data });
      })
      .catch(error => {
        console.error(`Unable to retrieve client data bundle from server. Error: ${error}`);
      });

    this.viewer.camera.moveEnd.addEventListener(() => {
      this.filterAvalanches();
    });

    this.eventHandler.setInputAction(movement => {
      // this.form.hideReadOnlyForm();

      let pick = this.viewer.scene.pick(movement.position);
      if (Cesium.defined(pick) && pick.id.name) {

        if (!pick.id.billboard) {
          // clicked on a path, set wait cursor
          this.setCursorStyle("wait");
        }

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
              this.controller.addAvalancheAndFlyTo(data);
            } else {
              // clicked on a path, display details
              this.setState({
                currentAvalanche: data,
              });
            }
          })
          .catch(error => {
            console.error(`Failed to fetch details for avalanche ${selectedAvalanche.id}. Error: ${error}`);
          });

      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    this.loadFacebookAPI();

    this.state = {
      currentMenuPanel: FilterMenuPanel,
      currentAvalanche: null,
      avalancheFilter: {
        fromDate: '',
        toDate: '',
        avyType: [],
        trigger: [],
        interface: [],
        rSize: '',
        dSize: '',
      }
    };
  }

  componentDidMount() {
    let extIdUrlParam = window.location.pathname.substr(1); // remove initial path slash

    if (extIdUrlParam) {
      fetch(`/api/avalanche/${extIdUrlParam}`)
        .then(response => {
          return parseApiResponse(response);
        })
        .then(data => {
          this.controller.addAvalancheAndFlyTo(data);
        })
        .catch(error => {
          this.controller.geolocateAndFlyTo();
        });
    } else {
      this.controller.geolocateAndFlyTo();
    }
  }

  loadFacebookAPI() {
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
  }

  filterAvalanches(filter) {
    let boundingBox = [];
    try {
      boundingBox = this.controller.getBoundingBox();
    } catch(error) {
      return;
    }

    let searchQueryString = `/api/avalanche/search?latMax=${boundingBox[0]}&latMin=${boundingBox[1]}&lngMax=${boundingBox[2]}&lngMin=${boundingBox[3]} \
      &camAlt=${this.viewer.camera.positionCartographic.height} \
      &camLng=${Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.longitude)} \
      &camLat=${Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.latitude)}`;

    if (filter) {
      if (filter.fromDate) searchQueryString += `&fromDate=${filter.fromDate}`;
      if (filter.toDate) searchQueryString += `&toDate=${filter.toDate}`;
      if (filter.avyType.length > 0) searchQueryString += `&avyType=${filter.avyType.join(',')}`;
      if (filter.trigger.length > 0) searchQueryString += `&trigger=${filter.trigger.join(',')}`;
      if (filter.interface.length > 0) searchQueryString += `&interface=${filter.interface.join(',')}`;
      if (filter.rSize) searchQueryString += `&rSize=${filter.rSize}`;
      if (filter.dSize) searchQueryString += `&dSize=${filter.dSize}`;

      this.setState({avalancheFilter: filter});
    }

    //console.info(`searchQueryString is ${searchQueryString}`);

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

  setCursorStyle(style) {
    this.cesiumContainer.style.cursor = style;
  }

  renderAvyCard() {
    return (
      <AvyCard
        avalanche={this.state.currentAvalanche}
        clientData={this.state.clientData}
        setCursorStyle={this.setCursorStyle}
        closeCallback={() => { this.setState({ currentAvalanche: null }) }}
      />
    )
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        <MenuButton menuToggle={() => { this.setState({currentMenuPanel: FilterMenuPanel}) }} />
        <ReportButton startReport={() => { this.setState({currentMenuPanel: ReportMenuPanel}) }} />
        <EyeAltitude viewer={this.viewer} />
        <ResetViewButton controller={this.controller} />

        <MenuDrawer
          menuPanel={this.state.currentMenuPanel}
          changeMenuPanel={(panel) => { this.setState({currentMenuPanel: panel}) }}
          clientData={this.state.clientData}
          filter={this.state.avalancheFilter}
          applyFilter={this.filterAvalanches}
        />

        <MouseBee
          viewer={this.viewer}
          eventHandler={this.eventHandler}
          cursorStyle={this.cesiumContainer.style.cursor}
          setCursorStyle={this.setCursorStyle}
        />

        {this.state.currentAvalanche && this.renderAvyCard()}
      </div>
    );
  }
}

AvyEyesClient.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyEyesClient);