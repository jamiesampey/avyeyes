import React from 'react';
import PropTypes from 'prop-types';
import Cesium from 'cesium/Cesium';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    position: 'absolute',
    zIndex: 10,
    color: '#edffff',
    background: '#303336',
    padding: 6,
    borderRadius: 5,
  },
});

class MouseBee extends React.Component {

  constructor(props) {
    super(props);

    this.props.eventHandler.setInputAction(movement => {
      if (this.props.cursorStyle === "wait") return; // in the process of opening a report

      let pick = this.props.viewer.scene.pick(movement.endPosition);
      if (Cesium.defined(pick) && pick.id.name) {
        this.props.setCursorStyle("pointer");
        this.setState({
          beeStyle: {
            display: 'block',
            left: movement.endPosition.x + 12,
            top: movement.endPosition.y + 10,
          },
          beeText: pick.id.name,
        });
      } else {
        this.props.setCursorStyle("default");
        this.setState({
          beeStyle: {
            display: 'none',
          },
          beeText: '',
        });
      }
    }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);

    this.state = {
      beeStyle: {
        display: 'none',
      },
      beeText: '',
    };
  }

  render() {
    const { classes } = this.props;

    return (
      <div className={classes.root} style={this.state.beeStyle}>
        {this.state.beeText}
      </div>
    )
  };
}

MouseBee.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(MouseBee);