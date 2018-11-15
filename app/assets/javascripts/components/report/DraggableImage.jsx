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
    return {
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
  let { classes, isDragging, connectDragSource, imageUrl, filename } = props;

  return connectDragSource(
    <div
      key={filename}
      className={classes.image}
      style={{
        backgroundImage: `url(${imageUrl})`,
      }}
    />
  );
};

export default DragSource('AvalancheImageTile', imageSource, collect)(withStyles(styles)(DraggableImage));