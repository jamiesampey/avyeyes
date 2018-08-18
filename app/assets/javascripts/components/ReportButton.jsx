import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import AddIcon from '@material-ui/icons/Add';

const styles = theme => ({
  root: {
    zIndex: 1,
    position: 'absolute',
    bottom: 10,
    right: 10,
  },
  button: {
    background: 'red',
    margin: theme.spacing.unit,
  },
  addIcon: {
    color: 'white',
  },
});

const ReportButton = props => {
  const { classes, startReport } = props;
  return (
    <div className={classes.root}>
      <Button
        variant="fab"
        mini={true}
        onClick={startReport}
        className={classes.button}
      >
        <AddIcon className={classes.addIcon} />
        Report Avalanche
      </Button>
    </div>
  );
};

ReportButton.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ReportButton);