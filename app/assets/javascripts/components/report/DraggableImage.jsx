import React from 'react';
import {DragSource} from 'react-dnd';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
  image: {
    maxWidth: '100%',
    maxHeight: '100%',
  },
});

const imageSource = {
  beginDrag(props) {
    return {
      imageUrl: props.imageUrl,
      filename: props.filename,
      caption: props.caption,
    };
  }
};

const collect = (connect, monitor) => {
  return {
    connectDragSource: connect.dragSource(),
    isDragging: monitor.isDragging()
  };
};

const DraggableImage = props => {
  let { classes, isDragging, connectDragSource, imageUrl, filename, caption } = props;

  return connectDragSource(
    <img key={filename} className={classes.image} src={imageUrl} />
  );
};

export default DragSource('AvalancheImageTile', imageSource, collect)(withStyles(styles)(DraggableImage));