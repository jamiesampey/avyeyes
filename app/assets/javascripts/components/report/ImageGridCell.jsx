import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { DropTarget } from "react-dnd";
import IconButton from "@material-ui/core/IconButton";
import MagnifyIcon from "@material-ui/icons/ZoomIn";
import CaptionIcon from "@material-ui/icons/InsertComment";
import DeleteIcon from "@material-ui/icons/Delete";


const styles = theme => ({
  root: {
    position: 'relative',
    width: 170,
    height: 170,
    border: '1px solid',
    borderColor: theme.palette.divider,
    display: 'flex',
    justifyContent: 'center',
    '&:hover': {
      cursor: 'move',
    },
  },
  orderNumber: {
    position: 'absolute',
    top: 2,
    left: 4,
    fontSize: '1.1rem',
    color: 'crimson',
  },
  actionsBar: {
    display: 'inline-block',
    position: 'absolute',
    bottom: 20,
    left: 0,
    height: 36,
    width: '100%',
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  actionsWrapper: {
    float: 'right',
  },
  actionButton: {
    padding: 6,
  },
  actionIcon: {
    color: 'white',
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
  const { classes, order, connectDropTarget, isOver, onCaptionChange, onDelete, children } = props;

  return connectDropTarget(
    <div className={classes.root}>
      <div className={classes.orderNumber}>{order + 1}</div>
      {children}
      {children &&
        <div className={classes.actionsBar}>
          <div className={classes.actionsWrapper}>
            <IconButton
              size="small"
              className={classes.actionButton}
              onClick={(e) => {
                console.info(`Magnify in cell ${order}`);
              }}
            >
              <MagnifyIcon className={classes.actionIcon}/>
            </IconButton>
            <IconButton
              size="small"
              className={classes.actionButton}
              onClick={(e) => {
                onCaptionChange(order, "some new caption");
              }}
            >
              <CaptionIcon className={classes.actionIcon}/>
            </IconButton>
            <IconButton
              size="small"
              className={classes.actionButton}
              onClick={(e) => {
                onDelete(order);
              }}
            >
              <DeleteIcon className={classes.actionIcon}/>
            </IconButton>
          </div>
        </div>
      }
    </div>
  );
};

ImageGridCell.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default DropTarget('AvalancheImageTile', imageGridCellTarget, collect)(withStyles(styles)(ImageGridCell));