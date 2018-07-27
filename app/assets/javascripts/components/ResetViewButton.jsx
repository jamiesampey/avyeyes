import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import IconButton from '@material-ui/core/IconButton';
import RefreshIcon from '@material-ui/icons/Refresh';
import { withStyles } from '@material-ui/core/styles';

const rootStyle = {
  position: 'absolute',
  top: 4,
  right: 42,
  zIndex: 10,
};

const styles = theme => ({
  button: {
    height: 32,
    width: 32,
  },
  icon: {
    color: '#edffff',
  }
});

class ResetViewButton extends React.Component {

  constructor() {
    super();
    this.resetView = this.resetView.bind(this);
  }

  resetView() {
    let camPos = this.props.controller.viewer.camera.positionCartographic;
    let target = this.props.controller.targetEntityFromCoords(Cesium.Math.toDegrees(camPos.longitude), Cesium.Math.toDegrees(camPos.latitude), false);
    this.props.controller.flyTo(target, 0.0, -89.9, 500000);
  }

  render() {
    const { classes } = this.props;

    return (
      <div className="cesium-button cesium-toolbar-button" style={rootStyle}>
        <IconButton className={classes.button} onClick={this.resetView}>
          <RefreshIcon className={classes.icon} />
        </IconButton>
      </div>
    )
  };
}

ResetViewButton.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ResetViewButton);