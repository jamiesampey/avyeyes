import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Drawer from '@material-ui/core/Drawer';
import List from '@material-ui/core/List';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  hide: {
    display: 'none',
  },
  drawerPaper: {
    position: 'relative',
    width: 300,
  },
  drawerHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'flex-end',
    padding: '0 8px',
  },
});

class MenuDrawer extends React.Component {

  constructor() {
    super();
  }

  render() {
    const { classes, theme, showDrawer, menuToggle } = this.props;

    return (
      <div className={classes.root}>
        <Drawer
          variant="persistent"
          anchor="left"
          open={showDrawer}
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
  }
}

MenuDrawer.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(MenuDrawer);
