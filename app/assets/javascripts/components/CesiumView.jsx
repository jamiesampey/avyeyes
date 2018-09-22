import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from 'cesium/Cesium';
import CesiumController from "../CesiumController";
import Config from '../Config';
import MenuButton from "./MenuButton";
import FilterDrawer from "./FilterDrawer";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";
import MouseBee from "./MouseBee";
import FilterSnackbar from "./FilterSnackbar";
import AvyCard from "./AvyCard";

import 'cesium/Widgets/widgets.css';
import '../../stylesheets/AvyEyesClient.scss';

import {getRequestParam, parseApiResponse} from "../Util";

const styles = theme => ({
  root: {
    width: '100%',
    height: '100%',
    overflow: 'hidden',
  },
  cesiumContainer: {
    height: '100%',
    width: '100%',
    margin: 0,
    padding: 0,
    overflow: 'hidden',
  },
});


class CesiumView extends React.Component {

  constructor(props) {
    super(props);

    this.cesiumRef = React.createRef();

    this.renderCesiumDecorators = this.renderCesiumDecorators.bind(this);
    this.filterAvalanches = this.filterAvalanches.bind(this);
    this.clearFilter = this.clearFilter.bind(this);
    this.setCursorStyle = this.setCursorStyle.bind(this);

    this.state = {
      cesiumInitialized: false,
      currentAvalanche: null,
      filterDrawerOpen: false,
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
    this.viewer = new Cesium.Viewer(this.cesiumRef.current, Config.cesiumViewerOptions);
    this.controller = new CesiumController(this.viewer);
    this.eventHandler = new Cesium.ScreenSpaceEventHandler(this.viewer.scene.canvas);

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

    this.props.initialized(this.controller);
    this.setState({ cesiumInitialized: true });

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

  filterAvalanches(updatedFilter) {
    let boundingBox = [];
    try {
      boundingBox = this.controller.getBoundingBox();
    } catch(error) {
      this.setState({
        infoMessage: this.props.clientData.help.horizonInView
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
    this.cesiumRef.current.style.cursor = style;
  }

  renderCesiumDecorators() {
    const { clientData, showHelp } = this.props;
    const { currentAvalanche, filterDrawerOpen, avalancheFilter } = this.state;

    return (
      <div>
        { currentAvalanche &&
          <AvyCard
            avalanche={currentAvalanche}
            clientData={clientData}
            setCursorStyle={this.setCursorStyle}
            closeCallback={() => {
              this.setState({currentAvalanche: null})
            }}
          />
        }

        <FilterDrawer
          drawerOpen={filterDrawerOpen}
          drawerClose={() => this.setState({filterDrawerOpen: false}) }
          clientData={clientData}
          filter={avalancheFilter}
          applyFilter={this.filterAvalanches}
          clearFilter={this.clearFilter}
          showHelp={showHelp}
        />

        <MouseBee
          viewer={this.viewer}
          eventHandler={this.eventHandler}
          setCursorStyle={this.setCursorStyle}
        />

        <FilterSnackbar
          drawerOpen={filterDrawerOpen}
          filter={avalancheFilter}
          clearFilter={this.clearFilter}
        />

        <MenuButton menuToggle={() => { this.setState({filterDrawerOpen: true}) }} />
        <EyeAltitude viewer={this.viewer} />
        <ResetViewButton controller={this.controller} />
      </div>
    );
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>
        <div ref={this.cesiumRef} className={classes.cesiumContainer} />
        { this.state.cesiumInitialized && this.renderCesiumDecorators() }
      </div>
    );
  }
}

CesiumView.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  showHelp: PropTypes.func.isRequired,
  initialized: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(CesiumView);