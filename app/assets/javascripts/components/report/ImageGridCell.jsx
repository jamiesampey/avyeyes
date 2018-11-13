import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

const styles = theme => ({
  root: {
    width: 170,
    height: 170,
    border: '1px solid',
    borderColor: theme.palette.divider,
  },
});

const ImageGridCell = props => {
  const { classes, order, children, onImageDrop } = props;

  return (
    <div className={classes.root}>
      {children}
    </div>
  );
};

ImageGridCell.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ImageGridCell);