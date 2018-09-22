import React from 'react';
import PropTypes from 'prop-types';

import Drawer from '@material-ui/core/Drawer';
import {withStyles} from '@material-ui/core/styles';
import Typography from "@material-ui/core/Typography";
import Stepper from "@material-ui/core/Stepper/Stepper";
import Step from "@material-ui/core/Step/Step";
import StepLabel from "@material-ui/core/StepLabel/StepLabel";
import StepContent from "@material-ui/core/StepContent/StepContent";
import Button from "@material-ui/core/Button/Button";

import Cesium from "cesium/Cesium";

import Select from "react-select";

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
  button: {
    marginTop: theme.spacing.unit,
    marginRight: theme.spacing.unit,
  },
  resetContainer: {
    padding: theme.spacing.unit * 3,
  },
});

class ReportDrawer extends React.Component {
  constructor(props) {
    super(props);

    this.handleNext = this.handleNext.bind(this);
    this.handleBack = this.handleNext.bind(this);
    this.handleReset = this.handleNext.bind(this);
    this.handleLocationChange = this.handleLocationChange.bind(this);
    this.handleLocationSelect = this.handleLocationSelect.bind(this);

    this.state = {
      activeStep: 0,
      locationOptions: [],
    };
  }

  handleNext() {
    this.setState(state => ({
      activeStep: state.activeStep + 1,
    }));
  };

  handleBack() {
    this.setState(state => ({
      activeStep: state.activeStep - 1,
    }));
  };

  handleReset() {
    this.setState({
      activeStep: 0,
    });
  };

  handleLocationChange(value) {
    if (!value || value.length < 5) return;

    this.props.controller.geocode(value, Cesium.GeocodeType.AUTOCOMPLETE).then( geocodeResults => {
      this.setState({
        locationOptions: geocodeResults.map(result => {
          return {
            label: result.displayName,
            value: JSON.stringify(result.destination),
          }
        })
      })
    });
  };

  handleLocationSelect(selected) {
    let location = JSON.parse(selected.value);
    let dest = location.x ? new Cesium.Cartesian3(location.x, location.y, location.z):
      new Cesium.Rectangle(location.west, location.south, location.east, location.north);
    this.props.controller.flyToDest(dest);
  };

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
        <Stepper className={classes.verticalStepper} activeStep={this.state.activeStep} orientation="vertical">
          <Step key={clientData.help.avyReportStepOneLabel}>
            <StepLabel>{clientData.help.avyReportStepOneLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepOneContent}}/>
              <div>
                <Select
                  onInputChange={this.handleLocationChange}
                  onChange={this.handleLocationSelect}
                  options={this.state.locationOptions}
                  placeholder="Location"
                />
                <Button className={classes.button}>
                  Begin Report
                </Button>
              </div>
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepTwoLabel}>
            <StepLabel>{clientData.help.avyReportStepTwoLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepTwoContent}}/>
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
