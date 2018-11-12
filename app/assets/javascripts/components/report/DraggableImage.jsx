import React from 'react';
import {DragSource} from 'react-dnd';
import {withStyles} from "@material-ui/core";

const styles = theme => ({
  root: {
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

function DraggableImage({ isDragging, connectDragSource, imageUrl, filename, caption }) {
  return connectDragSource(
    <div key={filename}>
      Hello World {/*<img src={imageUrl} />*/}
    </div>
  );
}

export default DragSource('AvalancheImageTile', imageSource, collect)(withStyles(styles)(DraggableImage));