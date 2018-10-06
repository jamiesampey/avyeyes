import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';

const styles = theme => ({
  root: {
    zIndex: 1,
    position: 'absolute',
    top: 8,
    left: 8,
    width: 260,
    height: 60,
    background: '#303336',
    borderRadius: 5,
    opacity: .85,
  },
  button: {
    position: 'absolute',
    right: 0,
    marginRight: 4,
    margin: theme.spacing.unit,
  },
  menuIcon: {
    color: 'white',
  },
});

const TitleDiv = props => {
  const { classes, menuToggle } = props;
  return (
    <div className={classes.root}>
      <img src="/assets/images/title.png"/>
      <IconButton onClick={menuToggle} className={classes.button}>
        <MenuIcon className={classes.menuIcon} />
      </IconButton>
    </div>
  );
};

TitleDiv.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(TitleDiv);