import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Stepper from '@material-ui/core/Stepper';
import Step from '@material-ui/core/Step';
import StepLabel from '@material-ui/core/StepLabel';
import StepContent from '@material-ui/core/StepContent';
import Button from '@material-ui/core/Button';
import Select from 'react-select';
import Cesium from "cesium/Cesium";

const styles = theme => ({
  root: {
    width: '90%',
  },
  verticalStepper: {
    padding: 0,
  },
  button: {
    marginTop: theme.spacing.unit,
    marginRight: theme.spacing.unit,
  },
  resetContainer: {
    padding: theme.spacing.unit * 3,
  },
});


class ReportSteps extends React.Component {
  constructor(props) {
    super(props);

    this.controller = props.cesiumController;

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

    this.controller.geocode(value, Cesium.GeocodeType.AUTOCOMPLETE).then( geocodeResults => {
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
    this.controller.flyToDest(dest);
  };

  render() {
    const { classes, clientData } = this.props;
    const { activeStep } = this.state;

    return (
      <div className={classes.root}>
        <Stepper className={classes.verticalStepper} activeStep={activeStep} orientation="vertical">
          <Step key={clientData.help.avyReportStepOneLabel}>
            <StepLabel>{clientData.help.avyReportStepOneLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepOneContent}} />
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
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepTwoContent}} />
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepThreeLabel}>
            <StepLabel>{clientData.help.avyReportStepThreeLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepThreeContent}} />
            </StepContent>
          </Step>
          <Step key={clientData.help.avyReportStepFourLabel}>
            <StepLabel>{clientData.help.avyReportStepFourLabel}</StepLabel>
            <StepContent>
              <div dangerouslySetInnerHTML={{__html: clientData.help.avyReportStepFourContent}} />
            </StepContent>
          </Step>
        </Stepper>
      </div>
    );
  }
}

ReportSteps.propTypes = {
  classes: PropTypes.object,
};

export default withStyles(styles)(ReportSteps);