import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import { DropTarget } from "react-dnd";
import IconButton from "@material-ui/core/IconButton";
import CaptionIcon from "@material-ui/icons/InsertComment";
import DeleteIcon from "@material-ui/icons/Delete";
import {AVALANCHE_IMAGE_TILE} from "../../Util";


const styles = theme => ({
  root: {
    position: 'relative',
    width: 166,
    height: 166,
    border: '1px solid',
    borderColor: theme.palette.divider,
    display: 'flex',
    justifyContent: 'center',
    '&:hover': {
      cursor: 'move',
    },
    margin: 1,
  },
  orderNumber: {
    position: 'absolute',
    top: 2,
    left: 6,
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
  canDrop(props) {
    return props.onImageDrop;
  },

  drop(props, monitor) {
    props.onImageDrop(props.index, monitor.getItem());
  }
};

const collect = (connect, monitor) => {
  return {
    connectDropTarget: connect.dropTarget(),
    isOver: monitor.isOver()
  };
};

const ImageGridCell = props => {
  const { classes, index, connectDropTarget, isOver, onCaptionChange, onDelete, children } = props;

  return connectDropTarget(
    <div
      className={classes.root}
      style={isOver && Boolean(children) ? { borderColor: 'red', borderWidth: 3 } : null}
    >
      <div className={classes.orderNumber}>{index + 1}</div>
      {children}
      {Boolean(children) && children.props.hasOwnProperty('image') &&
        <div className={classes.actionsBar}>
          <div className={classes.actionsWrapper}>
            <IconButton
              size="small"
              className={classes.actionButton}
              onClick={onCaptionChange}
            >
              <CaptionIcon className={classes.actionIcon}/>
            </IconButton>
            <IconButton
              size="small"
              className={classes.actionButton}
              onClick={onDelete}
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
  index: PropTypes.number.isRequired,
  onImageDrop: PropTypes.func,
  onDelete: PropTypes.func,
  onCaptionChange: PropTypes.func,
};

export default DropTarget(AVALANCHE_IMAGE_TILE, imageGridCellTarget, collect)(withStyles(styles)(ImageGridCell));