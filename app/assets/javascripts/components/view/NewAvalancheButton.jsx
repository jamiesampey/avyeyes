import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Fab from '@material-ui/core/Fab';
import AddIcon from '@material-ui/icons/Add';

const styles = theme => ({
  root: {
    zIndex: 1,
    position: 'absolute',
    bottom: 10,
    right: 10,
  },
  button: {
    color: 'white',
    background: 'red',
    margin: theme.spacing.unit,
  },
  addIcon: {
    color: 'white',
    marginRight: theme.spacing.unit,
  },
});

const NewAvalancheButton = props => {
  const { classes, visible, startReport } = props;

  if (!visible) return null;

  return (
    <div className={classes.root}>
      <Fab
        variant="extended"
        onClick={startReport}
        className={classes.button}
      >
        <AddIcon className={classes.addIcon} />
        New Avalanche
      </Fab>
    </div>
  );
};

NewAvalancheButton.propTypes = {
  classes: PropTypes.object.isRequired,
  visible: PropTypes.bool.isRequired,
  startReport: PropTypes.func.isRequired,
};

export default withStyles(styles)(NewAvalancheButton);