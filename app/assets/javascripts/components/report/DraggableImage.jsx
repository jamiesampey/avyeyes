import React from 'react';
import {DragSource} from 'react-dnd';
import {withStyles} from "@material-ui/core";
import {AVALANCHE_IMAGE_TILE} from "../../Util";
import PropTypes from "prop-types";

const styles = () => ({
  image: {
    width: '100%',
    height: '100%',
    backgroundPosition: 'center center',
    backgroundRepeat: 'no-repeat',
    backgroundSize: 'cover',
  },
});

const imageSource = {
  beginDrag(props) {
    return props.image;
  }
};

const collect = (connect, monitor) => {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging()
  };
};

const DraggableImage = props => {
  let { classes, connectDragSource, imageUrl } = props;

  return connectDragSource(
    <div
      className={classes.image}
      style={{
        backgroundImage: `url(${imageUrl})`,
      }}
    />
  );
};

DraggableImage.propTypes = {
  classes: PropTypes.object.isRequired,
  image: PropTypes.object.isRequired,
  imageUrl: PropTypes.string.isRequired,
};

export default DragSource(AVALANCHE_IMAGE_TILE, imageSource, collect)(withStyles(styles)(DraggableImage));