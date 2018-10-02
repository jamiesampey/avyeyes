import React from 'react';
import PropTypes from 'prop-types';

import Drawer from '@material-ui/core/Drawer';
import {withStyles} from '@material-ui/core/styles';
import Typography from "@material-ui/core/Typography";
import Stepper from "@material-ui/core/Stepper/Stepper";
import Step from "@material-ui/core/Step/Step";
import StepLabel from "@material-ui/core/StepLabel/StepLabel";
import StepContent from "@material-ui/core/StepContent/StepContent";

import Cesium from "cesium/Cesium";

import Select from "react-select";
import Button from "@material-ui/core/Button/Button";
import {parseApiResponse} from "../Util";

const styles = theme => ({
  drawerPaper: {
    position: 'relative',
    width: 350,
  },
  reportHeading: {
    marginLeft: 16,
    marginTop: 16,
    fontSize: '1.2rem',
    fontWeight: theme.typography.fontWeightRegular,
  },
  verticalStepper: {
    padding: 16,
  },
  reportStepButton: {

  },
});

const locationSelectStyles = {
  menu: () => ({
    border: 1,
    zIndex: 10,
  }),
  menuList: () => ({
  }),
};

const ManualViewStep = 1;
const NavHelpButtonClass = "cesium-navigation-help-button";

class ReportDrawer extends React.Component {
  constructor(props) {
    super(props);

    this.stepForward = this.stepForward.bind(this);
    this.stepBackward = this.stepForward.bind(this);
    this.handleLocationChange = this.handleLocationChange.bind(this);
    this.handleLocationSelect = this.handleLocationSelect.bind(this);
    this.beginDrawing = this.beginDrawing.bind(this);
    this.digestDrawing = this.digestDrawing.bind(this);

    this.state = {
      activeStep: 0,
      locationOptions: [],
      reportExtId: null,
      drawingPolygon: null,
    };
  }

  componentDidUpdate(prevProps, prevState, snapShot) {
    if (this.props.drawerOpen && !this.state.reportExtId) {
      fetch('/api/avalanche/newReportId')
        .then(response => {
          return parseApiResponse(response);
        })
        .then(data => {
          console.info(`reserved extId ${data.extId}`);
          this.setState({reportExtId: data.extId});
        })
        .catch(error => {
          console.error(`AvyEyes failed to reserve a new report ID. Error: ${error}`);
        });
    }

    if (this.state.activeStep === ManualViewStep) {
      let navHelpButtons = document.getElementsByClassName(NavHelpButtonClass);
      navHelpButtons[0].click();
    }
  }

  stepForward() {
    this.setState(state => ({
      activeStep: state.activeStep + 1,
    }));
  }

  stepBackward() {
    this.setState(state => ({
      activeStep: state.activeStep - 1,
    }));
  }

  handleLocationChange(value) {
    if (!value || value.length < 1) return;

    this.props.controller.geocode(value).then( geocodeResults => {
      this.setState({
        locationOptions: geocodeResults.map(result => {
          return {
            label: result.displayName,
            value: JSON.stringify(result.destination),
          }
        })
      })
    });
  }

  handleLocationSelect(selected) {
    let location = JSON.parse(selected.value);
    let dest = location.x ? new Cesium.Cartesian3(location.x, location.y, location.z):
      new Cesium.Rectangle(location.west, location.south, location.east, location.north);
    this.props.controller.flyToDest(dest);

    this.stepForward();
  }

  beginDrawing() {
    this.stepForward();

    let cesiumViewer = this.props.controller.viewer;
    let eventHandler = this.props.controller.eventHandler;

    // remove the default avalanche-click event handler
    eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);

    this.props.controller.setCursorStyle('crosshair');

    let isDrawing = false;
    let cartesian3Array = [];
    let drawingPolyline;
    let drawingPolylineColor = Cesium.Color.RED;
    let drawingPolygonColor = Cesium.Color.RED.withAlpha(0.4);

    eventHandler.setInputAction(function() {
      if (isDrawing) {

        // reset Cesium ScreenSpace event handlers
        eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
        eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        this.props.controller.setAvalancheSelectHandler();

        this.props.controller.setCursorStyle('default');

        this.setState({ drawingPolygon: this.props.controller.addEntity({
            polygon: {
              hierarchy: {
                positions: cartesian3Array
              },
              perPositionHeight: true,
              material: drawingPolygonColor,
              outline: false
            }
          })
        });

        this.props.controller.removeEntity(drawingPolyline);
        this.digestDrawing(cartesian3Array);
        isDrawing = false;
      } else {
        drawingPolyline = this.props.controller.addEntity({
          polyline: {
            positions: new Cesium.CallbackProperty(function() {
              return cartesian3Array;
            }, false),
            material: drawingPolylineColor,
            width: 3
          }
        });

        isDrawing = true;
      }
    }.bind(this), Cesium.ScreenSpaceEventType.LEFT_CLICK);

    eventHandler.setInputAction(function(movement) {
      if (!isDrawing) return;

      let ray = cesiumViewer.camera.getPickRay(movement.endPosition);
      let cartesianPos = cesiumViewer.scene.globe.pick(ray, cesiumViewer.scene);

      if (Cesium.defined(cartesianPos)) {
        if (cartesian3Array.length === 0) {
          cartesian3Array.push(cartesianPos);
        } else if (Cesium.Cartesian3.distance(cartesian3Array[cartesian3Array.length - 1], cartesianPos) > 4) {
          cartesian3Array.push(cartesianPos);
        }
      }
    }.bind(this), Cesium.ScreenSpaceEventType.MOUSE_MOVE);
  }

  digestDrawing(cartesian3Array) {
    let coordStr = "";
    let highestCartesian;
    let highestCartographic;
    let lowestCartesian;
    let lowestCartographic;

    cartesian3Array.forEach((i, cartesianPos) => {
      let cartographicPos = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesianPos);

      if (!highestCartographic || cartographicPos.height > highestCartographic.height) {
        highestCartesian = cartesianPos;
        highestCartographic = cartographicPos;
      }
      if (!lowestCartographic || cartographicPos.height < lowestCartographic.height) {
        lowestCartesian = cartesianPos;
        lowestCartographic = cartographicPos;
      }

      coordStr += Cesium.Math.toDegrees(cartographicPos.longitude).toFixed(8)
        + "," + Cesium.Math.toDegrees(cartographicPos.latitude).toFixed(8)
        + "," + cartographicPos.height.toFixed(2) + " ";
    });

    let hypotenuse = Cesium.Cartesian3.distance(highestCartesian, lowestCartesian);
    let opposite = highestCartographic.height - lowestCartographic.height;

    // this.view.form.setReportDrawingInputs(Cesium.Math.toDegrees(highestCartographic.longitude).toFixed(8),
    //   Cesium.Math.toDegrees(highestCartographic.latitude).toFixed(8),
    //   Math.round(highestCartographic.height),
    //   getAspect(highestCartographic, lowestCartographic),
    //   Math.round(Cesium.Math.toDegrees(Math.asin(opposite / hypotenuse))),
    //   coordStr.trim());
  }

  render() {
    const {classes, drawerOpen, clientData } = this.props;

    if (!clientData) return null;

    return (
      <Drawer
        variant="persistent"
        anchor="left"
        open={drawerOpen}
        classes={{
          paper: classes.drawerPaper,
        }}
      >
        <Typography className={classes.reportHeading}>
          Report an Avalanche
        </Typography>
        <Stepper orientation="vertical" className={classes.verticalStepper} activeStep={this.state.activeStep}>
          <Step key={clientData.help.avyReportStepOneLabel}>
            <StepLabel>{clientData.help.avyReportStepOneLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepOneContent}}/>
              <div>
                <Select
                  styles={locationSelectStyles}
                  onInputChange={this.handleLocationChange}
                  onChange={this.handleLocationSelect}
                  options={this.state.locationOptions}
                  placeholder="Location"
                />
              </div>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepTwoLabel}>
            <StepLabel>{clientData.help.avyReportStepTwoLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepTwoContent}}/>
              <div>
                <Button
                  variant="contained"
                  color="primary"
                  size="small"
                  className={classes.reportStepButton}
                  onClick={this.beginDrawing}
                >
                  Begin Drawing
                </Button>
              </div>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepThreeLabel}>
            <StepLabel>{clientData.help.avyReportStepThreeLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepThreeContent}}/>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepFourLabel}>
            <StepLabel>{clientData.help.avyReportStepFourLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepFourContent}}/>
              <div>
                <Button
                  variant="contained"
                  color="primary"
                  size="small"
                  className={classes.reportStepButton}
                  onClick={this.acceptDrawing}
                >
                  Accept Drawing
                </Button>
              </div>
            </StepContent>
          </Step>
        </Stepper>
      </Drawer>
    )
  }
}

ReportDrawer.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  drawerOpen: PropTypes.bool.isRequired,
  clientData: PropTypes.object,
  controller: PropTypes.object,
};

export default withStyles(styles, { withTheme: true })(ReportDrawer);
