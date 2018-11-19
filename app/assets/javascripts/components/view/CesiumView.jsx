import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Cesium from 'cesium/Cesium';
import CesiumController from "../../CesiumController";
import MenuButton from "./TitleDiv";
import FilterDrawer from "./FilterDrawer";
import AdminUserChip from "../admin/AdminUserChip";
import EyeAltitude from "./EyeAltitude";
import ResetViewButton from "./ResetViewButton";
import MouseBee from "./MouseBee";
import FilterSnackbar from "./FilterSnackbar";
import AvyCard from "./AvyCard";

import 'cesium/Widgets/widgets.css';
import '../../../stylesheets/AvyEyesClient.scss';

import {checkStatusAndParseJson, fetchAvalanche} from "../../Util";
import ReportDialog from "../report/ReportDialog";
import InfoBar from "./InfoBar";

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
  userChip: {
    position: 'absolute',
    top: 8,
    right: 240,
    zIndex: 10,
  },
});


class CesiumView extends React.Component {

  constructor(props) {
    super(props);

    this.cesiumRef = React.createRef();

    this.renderAvalanche = this.renderAvalanche.bind(this);
    this.renderCesiumDecorators = this.renderCesiumDecorators.bind(this);
    this.filterAvalanches = this.filterAvalanches.bind(this);
    this.clearFilter = this.clearFilter.bind(this);

    this.state = {
      cesiumInitialized: false,
      infoBarMessage: null,
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
    this.controller = new CesiumController(this.cesiumRef.current);

    this.controller.viewer.camera.moveEnd.addEventListener(() => {
      if (!this.props.currentAvalanche) this.filterAvalanches();
    });

    this.controller.eventHandler.setInputAction(movement => {
      let pick = this.controller.viewer.scene.pick(movement.position);
      if (Cesium.defined(pick) && pick.id.name) {
        let selectedAvalanche = pick.id;

        fetchAvalanche(selectedAvalanche.id)
          .then(data => {
            if (pick.id.billboard) {
              // clicked on a pin, add the path and fly to it
              this.controller.removeAllEntities();
              this.controller.addAvalancheAndFlyTo(data, () => setTimeout(this.props.setCurrentAvalanche(data), 2000));
            } else {
              // clicked on a path, display details
              this.props.setCurrentAvalanche(data);
            }
          })
          .catch(error => {
            console.error(`Failed to fetch details for avalanche ${selectedAvalanche.id}. Error: ${error}`);
          });

      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);

    this.props.setController(this.controller);
    this.setState({ cesiumInitialized: true });

    let extIdUrlParam = window.location.pathname.substr(1); // remove initial path slash

    // init avalanche fly to and display
    if (extIdUrlParam) {
      fetchAvalanche(extIdUrlParam)
        .then(data => {
          this.controller.addAvalancheAndFlyTo(data, () => setTimeout(this.props.setCurrentAvalanche(data), 2000));
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
        infoBarMessage: this.props.clientData.help.horizonInView
      });
      return;
    }

    let searchQueryString = `/api/avalanche/search?latMax=${boundingBox[0]}&latMin=${boundingBox[1]}&lngMax=${boundingBox[2]}&lngMin=${boundingBox[3]} \
      &camAlt=${this.controller.viewer.camera.positionCartographic.height} \
      &camLng=${Cesium.Math.toDegrees(this.controller.viewer.camera.positionCartographic.longitude)} \
      &camLat=${Cesium.Math.toDegrees(this.controller.viewer.camera.positionCartographic.latitude)}`;

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
        return checkStatusAndParseJson(response);
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

  renderAvalanche() {
    const { clientData, currentAvalanche, setCurrentAvalanche } = this.props;

    return (!currentAvalanche.areaName || currentAvalanche.submitterEmail) ?
      <ReportDialog
        clientData={clientData}
        avalanche={currentAvalanche}
        setInfoMessage={(message) => this.setState({ infoBarMessage: message })}
        onClose={() => setCurrentAvalanche(null)}
      />
      :
      <AvyCard
        clientData={clientData}
        avalanche={currentAvalanche}
        setInfoMessage={(message) => this.setState({ infoBarMessage: message })}
        onClose={() => setCurrentAvalanche(null)}
      />;
  }

  renderCesiumDecorators() {
    const { classes, clientData, currentAvalanche, showHelp } = this.props;
    const { filterDrawerOpen, avalancheFilter } = this.state;

    return (
      <div>
        { currentAvalanche && this.renderAvalanche() }

        <FilterDrawer
          drawerOpen={filterDrawerOpen}
          drawerClose={() => this.setState({filterDrawerOpen: false}) }
          clientData={clientData}
          filter={avalancheFilter}
          applyFilter={this.filterAvalanches}
          clearFilter={this.clearFilter}
          showHelp={showHelp}
        />

        <FilterSnackbar
          drawerOpen={filterDrawerOpen}
          filter={avalancheFilter}
          clearFilter={this.clearFilter}
        />

        { Boolean(this.state.infoBarMessage) &&
          <InfoBar
            open={Boolean(this.state.infoBarMessage)}
            message={this.state.infoBarMessage}
            duration={5}
            onClose={() => this.setState({infoBarMessage: null}) }
          />
        }

        <MouseBee controller={this.controller}/>
        <MenuButton menuToggle={() => { this.setState({filterDrawerOpen: true}) }} />
        <div className={classes.userChip}><AdminUserChip/></div>
        <EyeAltitude viewer={this.controller.viewer} />
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
  clientData: PropTypes.object.isRequired,
  currentAvalanche: PropTypes.object,
  setCurrentAvalanche: PropTypes.func.isRequired,
  setController: PropTypes.func.isRequired,
  showHelp: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(CesiumView);