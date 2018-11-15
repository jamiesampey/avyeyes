import React from 'react';
import {DragSource} from 'react-dnd';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
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
  let { classes, isDragging, connectDragSource, image, imageUrl } = props;

  return connectDragSource(
    <div
      key={image.filename}
      className={classes.image}
      style={{
        backgroundImage: `url(${imageUrl})`,
      }}
    />
  );
};

export default DragSource('AvalancheImageTile', imageSource, collect)(withStyles(styles)(DraggableImage));