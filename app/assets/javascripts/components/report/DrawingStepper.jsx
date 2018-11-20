import React from 'react';
import PropTypes from 'prop-types';

import Drawer from '@material-ui/core/Drawer';
import {withStyles} from '@material-ui/core/styles';
import Typography from "@material-ui/core/Typography";
import Stepper from "@material-ui/core/Stepper/Stepper";
import Step from "@material-ui/core/Step/Step";
import StepLabel from "@material-ui/core/StepLabel/StepLabel";
import StepContent from "@material-ui/core/StepContent/StepContent";
import Button from "@material-ui/core/Button";

import Cesium from "cesium/Cesium";

import {checkStatus, checkStatusAndParseJson, getCSRFTokenFromCookie} from "../../Util";
import GeoCompleteSelect from "./GeoCompleteSelect";

const styles = theme => ({
  drawerPaper: {
    position: 'relative',
    width: 350,
    height: '100%',
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
  stepLabel: {
    fontSize: '1.1rem',
  },
  instructions: {
    fontSize: '1.1rem',
    color: theme.palette.text.primary,
  },
  buttonsContainer: {
    textAlign: 'center',
    '& button': {
      marginRight: 10,
    }
  },
});

const MANUEL_VIEW_STEP = 1;
const NAV_HELP_BUTTON_CLASS = "cesium-navigation-help-button";

class DrawingStepper extends React.Component {
  constructor(props) {
    super(props);

    this.stepForward = this.stepForward.bind(this);
    this.beginDrawing = this.beginDrawing.bind(this);
    this.digestDrawing = this.digestDrawing.bind(this);
    this.convertDrawingToAvalanche = this.convertDrawingToAvalanche.bind(this);
    this.uploadDrawingScreenshot = this.uploadDrawingScreenshot.bind(this);

    this.state = {
      activeStep: 0,
      reportExtId: null,
      drawing: null,
    };
  }

  componentDidUpdate(prevProps, prevState, snapShot) {
    if (this.props.drawerOpen && !prevProps.drawerOpen && !this.state.reportExtId) {
      fetch('/api/avalanche/newReportId')
        .then(response => {
          return checkStatusAndParseJson(response);
        })
        .then(data => {
          console.info(`New report ID reserved: ${data.extId}`);
          this.setState({reportExtId: data.extId});
        })
        .catch(error => {
          console.error(`AvyEyes failed to reserve a new report ID. Error: ${error}`);
        });
    }

    if (this.state.activeStep === MANUEL_VIEW_STEP) {
      let navHelpButtons = document.getElementsByClassName(NAV_HELP_BUTTON_CLASS);
      navHelpButtons[0].click();
    }
  }

  beginDrawing() {
    this.stepForward();

    // Used to pause after adding/removing the polyline entity and before pushing to the cart3 array
    let pauseAndExecute = (endingCursorStyle, callback) => {
      this.props.controller.setCursorStyle('wait');
      setTimeout(() => {
        callback();
        this.props.controller.setCursorStyle(endingCursorStyle);
      }, 500);
    };

    let cesiumViewer = this.props.controller.viewer;
    let eventHandler = this.props.controller.eventHandler;

    //  temporarily replace the normal left-click and mouse-move event handlers
    let origLeftClickInputAction = eventHandler.getInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
    let origMouseMoveInputAction = eventHandler.getInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
    eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
    eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);

    this.props.controller.setCursorStyle('crosshair');

    let isDrawing = false;
    let lastPointAddTime = Date.now();
    let cart3Array = [];
    let drawingPolyline;

    eventHandler.setInputAction(() => {
      if (!isDrawing) {
        isDrawing = true;

        drawingPolyline = this.props.controller.addEntity({
          polyline: {
            positions: new Cesium.CallbackProperty(() => { return cart3Array; }, false),
            clampToGround: true,
            material: Cesium.Color.RED,
            width: 3
          }
        });

        pauseAndExecute('crosshair', () => {
          eventHandler.setInputAction((movement) => {
            let now = Date.now();
            if ((now - lastPointAddTime) > 50) {
              let ray = cesiumViewer.camera.getPickRay(movement.endPosition);
              let cartesianPos = cesiumViewer.scene.globe.pick(ray, cesiumViewer.scene);
              if (Cesium.defined(cartesianPos)) cart3Array.push(cartesianPos);
              lastPointAddTime = now;
            }
          }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        });

      } else {
        isDrawing = false;

        // Finished with the drawing... reset the Cesium event handlers and digest the drawing
        eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.LEFT_CLICK);
        eventHandler.removeInputAction(Cesium.ScreenSpaceEventType.MOUSE_MOVE);
        eventHandler.setInputAction(origLeftClickInputAction, Cesium.ScreenSpaceEventType.LEFT_CLICK);
        eventHandler.setInputAction(origMouseMoveInputAction, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

        pauseAndExecute('default', () => {
          this.props.controller.removeEntity(drawingPolyline);
          let newPolygonEntity = this.props.controller.addEntity({
            polygon: {
              hierarchy: {
                positions: cart3Array
              },
              perPositionHeight: true,
              material: Cesium.Color.RED.withAlpha(0.4),
              outline: false
            }
          });
          this.digestDrawing(cart3Array, newPolygonEntity);
        });
      }
    }, Cesium.ScreenSpaceEventType.LEFT_CLICK);
  }

  digestDrawing(cartesian3Array, drawingEntity) {
    let coordinates = [];
    let highestCartesian = null;
    let highestCartographic = null;
    let lowestCartesian = null;
    let lowestCartographic = null;

    cartesian3Array.forEach(cartesian => {
      let cartographic = Cesium.Ellipsoid.WGS84.cartesianToCartographic(cartesian);

      if (!highestCartographic || cartographic.height > highestCartographic.height) {
        highestCartesian = cartesian;
        highestCartographic = cartographic;
      }
      if (!lowestCartographic || cartographic.height < lowestCartographic.height) {
        lowestCartesian = cartesian;
        lowestCartographic = cartographic;
      }

      coordinates.push({
        longitude: parseFloat(Cesium.Math.toDegrees(cartographic.longitude).toFixed(8)),
        latitude: parseFloat(Cesium.Math.toDegrees(cartographic.latitude).toFixed(8)),
        altitude: Math.round(cartographic.height),
      });
    });

    let hypotenuse = Cesium.Cartesian3.distance(highestCartesian, lowestCartesian);
    let opposite = highestCartographic.height - lowestCartographic.height;

    this.setState({
      drawing: {
        entity: drawingEntity,
        latitude: parseFloat(Cesium.Math.toDegrees(highestCartographic.latitude).toFixed(8)),
        longitude: parseFloat(Cesium.Math.toDegrees(highestCartographic.longitude).toFixed(8)),
        altitude: Math.round(highestCartographic.height),
        aspect: DrawingStepper.getDrawingAspect(highestCartographic, lowestCartographic),
        angle: Math.round(Cesium.Math.toDegrees(Math.asin(opposite / hypotenuse))),
        perimeter: coordinates,
      }
    }, this.stepForward);
  }

  convertDrawingToAvalanche() {
    let { reportExtId, drawing } = this.state;
    const EMPTY_STRING = '';

    return {
      extId: reportExtId,
      location: {
        longitude: drawing.longitude,
        latitude: drawing.latitude,
        altitude: drawing.altitude,
      },
      slope: {
        aspect: drawing.aspect,
        angle: drawing.angle,
        elevation: drawing.altitude,
      },
      perimeter: drawing.perimeter,
      viewable: true,
      submitterEmail: EMPTY_STRING,
      submitterExp: EMPTY_STRING,
      date: EMPTY_STRING,
      areaName: EMPTY_STRING,
      weather: {
        recentSnow: EMPTY_STRING,
        recentWindSpeed: EMPTY_STRING,
        recentWindDirection: EMPTY_STRING,
      },
      classification: {
        avyType: EMPTY_STRING,
        trigger: EMPTY_STRING,
        triggerModifier: EMPTY_STRING,
        interface: EMPTY_STRING,
        rSize: 0,
        dSize: 0,
      },
      comments: EMPTY_STRING,
    };
  }

  uploadDrawingScreenshot() {
    let { reportExtId } = this.state;
    let csrfToken = getCSRFTokenFromCookie();

    let base64ImageContent = this.props.controller.viewer.canvas.toDataURL('image/jpeg', 0.8).replace(/^data:image\/jpeg;base64,/, "");
    let sliceSize = 1024;
    let byteChars = window.atob(base64ImageContent);

    let byteArrays = [];
    for (let offset = 0, len = byteChars.length; offset < len; offset += sliceSize) {
      let slice = byteChars.slice(offset, offset + sliceSize);

      let byteNumbers = new Array(slice.length);
      for (let i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }
      byteArrays.push(new Uint8Array(byteNumbers));
    }

    let formData = new FormData();
    formData.append("screenshot", new Blob(byteArrays, {type: 'image/jpeg'}));

    fetch(`/api/avalanche/${reportExtId}/images/screenshot?csrfToken=${csrfToken}`, {
      method: 'POST',
      body: formData,
    })
    .then(response => {
      checkStatus(response);
      console.info(`Screenshot successfully uploaded for new avalanche report ${reportExtId}`);
    })
    .catch(error => console.error(`ERROR uploading drawing screenshot for new avalanche report ${reportExtId}: ${error}`));
  }

  static getDrawingAspect(highestCartographic, lowestCartographic) {
    let lat1 = highestCartographic.latitude;
    let lat2 = lowestCartographic.latitude;
    let dLon = lowestCartographic.longitude - highestCartographic.longitude;

    let y = Math.sin(dLon) * Math.cos(lat2);
    let x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
    let heading = (Cesium.Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

    if (heading > 22.5 && heading <= 67.5) return "NE";
    if (heading > 67.5 && heading <= 112.5) return "E";
    if (heading > 112.5 && heading <= 157.5) return "SE";
    if (heading > 157.5 && heading <= 202.5) return "S";
    if (heading > 202.5 && heading <= 247.5) return "SW";
    if (heading > 247.5 && heading <= 292.5) return "W";
    if (heading > 292.5 && heading <= 337.5) return "NW";
    return "N";
  }

  stepForward() {
    this.setState(state => ({
      activeStep: state.activeStep + 1,
    }));
  }

  render() {
    const {classes, drawerOpen, clientData, drawingComplete } = this.props;

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
            <StepLabel classes={{label: classes.stepLabel}}>{clientData.help.avyReportStepOneLabel}</StepLabel>
            <StepContent>
              <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepOneContent}}/>
              <div>
                <GeoCompleteSelect
                  controller={this.props.controller}
                  onSelect={this.stepForward}
                />
              </div>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepTwoLabel}>
            <StepLabel classes={{label: classes.stepLabel}}>{clientData.help.avyReportStepTwoLabel}</StepLabel>
            <StepContent>
              <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepTwoContent}}/>
              <div className={classes.buttonsContainer}>
                <Button
                  variant="contained"
                  color="primary"
                  size="small"
                  onClick={this.beginDrawing}
                >
                  Begin Drawing
                </Button>
              </div>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepThreeLabel}>
            <StepLabel classes={{label: classes.stepLabel}}>{clientData.help.avyReportStepThreeLabel}</StepLabel>
            <StepContent>
              <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepThreeContent}}/>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepFourLabel}>
            <StepLabel classes={{label: classes.stepLabel}}>{clientData.help.avyReportStepFourLabel}</StepLabel>
            <StepContent>
              <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepFourContent}}/>
              <div className={classes.buttonsContainer}>
                <Button
                  variant="contained"
                  color="primary"
                  size="small"
                  onClick={() => {
                    this.props.controller.removeEntity(this.state.drawing.entity);
                    this.setState({activeStep: 1, drawing: null});
                  }}
                >
                  Redraw
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  size="small"
                  onClick={() => {
                    this.uploadDrawingScreenshot();
                    let newAvalanche = this.convertDrawingToAvalanche();

                    // remove the drawing
                    let cesiumDrawingEntity = this.state.drawing.entity;
                    this.props.controller.removeEntity(cesiumDrawingEntity);

                    this.setState({
                      activeStep: 0,
                      reportExtId: null,
                      drawing: null,
                    }, () => drawingComplete(newAvalanche));
                  }}
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

DrawingStepper.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  drawerOpen: PropTypes.bool.isRequired,
  clientData: PropTypes.object,
  controller: PropTypes.object,
  drawingComplete: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(DrawingStepper);
