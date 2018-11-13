import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

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

const ImageGridCell = props => {
  const { classes, order, children, onImageDrop } = props;

  return (
    <div className={classes.root}>
      <div className={classes.orderNumber}>{order + 1}</div>
      {children}
    </div>
  );
};

ImageGridCell.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ImageGridCell);