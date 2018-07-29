import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    position: 'absolute',
    top: 8,
    right: 82,
    zIndex: 10,
    padding: 6,
    background: '#303336',
    color: '#edffff',
    borderRadius: 5,
  },
});

class EyeAltitude extends React.Component {

  componentWillMount() {
    let self = this;
    let eyeAltSetter = {};

    let toAltitudeString = (meters) => {
      return `eye at ${(meters > 10000) ? (meters / 1000).toFixed(2) + " km" : meters + " meters"}`;
    };

    this.props.viewer.camera.moveStart.addEventListener(() => {
      eyeAltSetter = setInterval(() => {
        self.setState({
          eyeAltitude: toAltitudeString(self.props.viewer.camera.positionCartographic.height.toFixed(0)),
        });
      }, 10);
    });

    this.props.viewer.camera.moveEnd.addEventListener(() => {
      clearInterval(eyeAltSetter);
    });

    // set the initial altitude
    this.setState({
      eyeAltitude: toAltitudeString(this.props.viewer.camera.positionCartographic.height.toFixed(0)),
    });
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root}>{this.state.eyeAltitude}</div>
    )
  };
}

EyeAltitude.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(EyeAltitude);