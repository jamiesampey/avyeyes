import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Drawer from "@material-ui/core/Drawer";
import IconButton from "@material-ui/core/IconButton";
import CloseIcon from "@material-ui/icons/Close";
import ViewListIcon from "@material-ui/icons/ViewList";
import ImageIcon from "@material-ui/icons/Image";
import CommentsIcon from "@material-ui/icons/InsertComment";

const styles = theme => ({
  dialog: {

  },
  formRoot: {
    height: 430,
    width: 500,
    zIndex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
  },
  title: {
    flex: 1,
    paddingLeft: 15,
  },
  closeIcon: {
    color: 'white',
  },
  drawerPaper: {
    position: 'relative',
  },
  content: {
    backgroundColor: theme.palette.background.default,
    padding: theme.spacing.unit * 3,
    minWidth: 0, // So the Typography noWrap works
  },
  toolbar: theme.mixins.toolbar,
  hiddenForm: {
    display: 'none',
  },
});

class AvyFormDialog extends React.Component {

  render() {
    const { classes, avalanche, closeCallback } = this.props;
    if (avalanche === null) return (<div className={classes.hiddenForm} />);

    this.props.setCursorStyle("default");

    if (avalanche.hasOwnProperty("viewable")) {
      console.info(`Received ADMIN details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.enableAdminControls().then(() => {
      // this.form.displayReadWriteForm(data);
      // });
    } else if (avalanche.hasOwnProperty("submitterEmail")) {
      console.info(`Received READ-WRITE details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.displayReadWriteForm(data);
    } else {
      console.info(`Received READ-ONLY details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.displayReadOnlyForm(movement.position, data);
    }

    return (
      <Dialog
        className={classes.dialog}
        open={avalanche !== null}
        onClose={closeCallback}
        aria-labelledby="form-dialog-title"
      >
        <div className={classes.formRoot}>
          <AppBar position="absolute" className={classes.appBar}>
            <Toolbar disableGutters={true}>
              <Typography variant="title" color="inherit" noWrap className={classes.title}>
                {avalanche.title}
              </Typography>
              <IconButton className={classes.button} aria-label="Close" onClick={closeCallback}>
                <CloseIcon className={classes.closeIcon} />
              </IconButton>
            </Toolbar>
          </AppBar>
          <Drawer
            variant="permanent"
            classes={{
              paper: classes.drawerPaper,
            }}
          >
            <div className={classes.toolbar} />
            <IconButton className={classes.button} aria-label="SWAG Details">
              <ViewListIcon />
            </IconButton>
            <IconButton className={classes.button} aria-label="Avalanche Images">
              <ImageIcon />
            </IconButton>
            <IconButton className={classes.button} aria-label="Submitter's Comments">
              <CommentsIcon />
            </IconButton>
          </Drawer>
          <main>
            <Typography noWrap>main avalanche content</Typography>
          </main>
        </div>
      </Dialog>
    );
  };
}

AvyFormDialog.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(AvyFormDialog);