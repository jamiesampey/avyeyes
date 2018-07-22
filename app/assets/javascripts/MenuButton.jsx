import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Button from '@material-ui/core/Button';
import MenuIcon from '@material-ui/icons/Menu';

const styles = theme => ({
  root: {
    zIndex: 1,
    position: 'absolute',
    top: 10,
    left: 10,
  },
  button: {
    background: '#153570',
    margin: theme.spacing.unit,
  },
  menuIcon: {
    // marginRight: theme.spacing.unit,
  },
});

const MenuButton = (props) => {
  const { classes } = props;
  return (
    <div className={classes.root}>
      <Button variant="fab" size="small" className={classes.button}>
        <MenuIcon className={classes.menuIcon} />
      </Button>
    </div>
  );
};

MenuButton.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(MenuButton);