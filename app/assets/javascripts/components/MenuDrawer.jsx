import React from 'react';
import PropTypes from 'prop-types';

import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import {withStyles} from '@material-ui/core/styles';

const drawerWidth = 300;

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    width: drawerWidth,
  },
  drawerHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
  },
});

const MenuDrawer = (props) => {
  const { classes, theme, showDrawer, menuToggle } = props;

  return (
    <div className={classes.root}>
      <Drawer
        variant="temporary"
        anchor="left"
        open={showDrawer}
        ModalProps={{ onBackdropClick: menuToggle }}
        classes={{
          paper: classes.drawerPaper,
        }}
      >
        <div className={classes.drawerHeader}>
          AvyEyes
          <IconButton onClick={menuToggle}>
            <ChevronLeftIcon/>
          </IconButton>
        </div>
        <Divider />
        <List>list item 1</List>
        <Divider />
        <List>list item 2</List>
      </Drawer>
    </div>
  );
};

MenuDrawer.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(MenuDrawer);
