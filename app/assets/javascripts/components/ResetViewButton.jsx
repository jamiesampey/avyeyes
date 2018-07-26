import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import IconButton from '@material-ui/core/IconButton';
import RefreshIcon from '@material-ui/icons/Refresh';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    position: 'absolute',
    top: 7,
    right: 46,
    height: 32,
    width: 32,
    zIndex: 10,
    backgroundColor: '#303336',
    borderRadius: 4,
    border: '1px solid #444',
    '&:hover': {
      backgroundColor: '#48b',
      borderColor: '#aef',
    },
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
      <IconButton className={classes.root} onClick={this.resetView}>
        <RefreshIcon className={classes.icon} />
      </IconButton>
    )
  };
}

ResetViewButton.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ResetViewButton);