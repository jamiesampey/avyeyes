import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import {DropTarget} from "react-dnd";

const styles = theme => ({
  root: {
    position: 'relative',
    width: 164,
    height: 164,
    padding: 2,
    border: '1px solid',
    borderColor: theme.palette.divider,
    display: 'flex',
    justifyContent: 'center',
  },
  orderNumber: {
    position: 'absolute',
    top: 2,
    left: 4,
    fontSize: '1.1rem',
    color: theme.palette.divider,
  },
});

const imageGridCellTarget = {
  drop(props, monitor) {
    // TODO disallow drop on gridCells that do not have an onImageDrop prop
    props.onImageDrop(props.order, monitor.getItem());
  }
};

const collect = (connect, monitor) => {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver()
  };
};

const ImageGridCell = props => {
  const { classes, order, connectDropTarget, isOver, children } = props;

  return connectDropTarget(
    <div className={classes.root}>
      <div className={classes.orderNumber}>{order + 1}</div>
      {children}
    </div>
  );
};

ImageGridCell.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default DropTarget('AvalancheImageTile', imageGridCellTarget, collect)(withStyles(styles)(ImageGridCell));