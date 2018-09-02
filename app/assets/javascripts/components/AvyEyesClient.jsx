import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from 'cesium/Cesium';
import CesiumController from "../CesiumController";
import Config from '../Config';
import MenuButton from "./MenuButton";
import MainMenu from "./MainMenu";
import ReportButton from "./ReportButton";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";
import MouseBee from "./MouseBee";
import FilterSnackbar from "./FilterSnackbar";
import AvyCard from "./AvyCard";
import InfoBar from "./InfoBar";
import HelpDialog from "./HelpDialog";

import 'cesium/Widgets/widgets.css';
import '../../stylesheets/AvyEyesClient.scss';

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
    this.clearFilter = this.clearFilter.bind(this);
    this.setCursorStyle = this.setCursorStyle.bind(this);
    this.showHelp = this.showHelp.bind(this);

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
      currentMenuPanel: null,
      infoMessage: null,
      help: null,
      currentAvalanche: null,
      avalancheFilter: {
        fromDate: '',
        toDate: '',
        avyTypes: [],
        triggers: [],
        interfaces: [],
        rSize: 0,
        dSize: 0,
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

  filterAvalanches(updatedFilter) {
    let boundingBox = [];
    try {
      boundingBox = this.controller.getBoundingBox();
    } catch(error) {
      this.setState({
        infoMessage: this.state.clientData.help.horizonInView
      });
      return;
    }

    let searchQueryString = `/api/avalanche/search?latMax=${boundingBox[0]}&latMin=${boundingBox[1]}&lngMax=${boundingBox[2]}&lngMin=${boundingBox[3]} \
      &camAlt=${this.viewer.camera.positionCartographic.height} \
      &camLng=${Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.longitude)} \
      &camLat=${Cesium.Math.toDegrees(this.viewer.camera.positionCartographic.latitude)}`;

    let appendFilter = (filter) => {
      if (!filter) return;
      if (filter.fromDate) searchQueryString += `&fromDate=${filter.fromDate}`;
      if (filter.toDate) searchQueryString += `&toDate=${filter.toDate}`;
      if (filter.avyTypes.length > 0) searchQueryString += `&avyTypes=${filter.avyTypes.join(',')}`;
      if (filter.triggers.length > 0) searchQueryString += `&triggers=${filter.triggers.join(',')}`;
      if (filter.interfaces.length > 0) searchQueryString += `&interfaces=${filter.interfaces.join(',')}`;
      if (filter.rSize > 0) searchQueryString += `&rSize=${filter.rSize}`;
      if (filter.dSize > 0) searchQueryString += `&dSize=${filter.dSize}`;
    };

    if (updatedFilter) {
      this.setState({avalancheFilter: updatedFilter});
      appendFilter(updatedFilter);
    } else {
      appendFilter(this.state.avalancheFilter);
    }

    //console.info(`sending searchQueryString ${searchQueryString}`);

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

  clearFilter() {
    this.filterAvalanches({fromDate: '', toDate: '', avyTypes: [], triggers: [], interfaces: [], rSize: 0, dSize: 0 });
  }

  setCursorStyle(style) {
    this.cesiumContainer.style.cursor = style;
  }

  showHelp(params) {
    this.setState({
      currentMenuPanel: null,
      help: {
        title: params.title,
        content: params.content,
      }
    })
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        { this.state.currentAvalanche &&
          <AvyCard
            avalanche={this.state.currentAvalanche}
            clientData={this.state.clientData}
            setCursorStyle={this.setCursorStyle}
            closeCallback={() => { this.setState({ currentAvalanche: null }) }}
          />
        }

        <MainMenu
          menuPanel={this.state.currentMenuPanel}
          changeMenuPanel={(panel) => { this.setState({currentMenuPanel: panel}) }}
          clientData={this.state.clientData}
          filter={this.state.avalancheFilter}
          applyFilter={this.filterAvalanches}
          clearFilter={this.clearFilter}
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

        <MouseBee
          viewer={this.viewer}
          eventHandler={this.eventHandler}
          cursorStyle={this.cesiumContainer.style.cursor}
          setCursorStyle={this.setCursorStyle}
        />

        <FilterSnackbar
          menuPanel={this.state.currentMenuPanel}
          filter={this.state.avalancheFilter}
          clearFilter={this.clearFilter}
        />

        <MenuButton menuToggle={() => { this.setState({currentMenuPanel: FilterMenuPanel}) }} />
        <ReportButton startReport={() => { this.setState({currentMenuPanel: ReportMenuPanel}) }} />
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