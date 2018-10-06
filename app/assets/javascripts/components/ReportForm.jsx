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
});

class ReportForm extends React.Component {

  render() {
    const { classes, openReport, drawing, callback } = this.props;

    return (
      <Dialog
        className={classes.dialog}
        open={openReport}
        onClose={callback}
        aria-labelledby="form-dialog-title"
      >
        <div className={classes.formRoot}>
          <AppBar position="absolute" className={classes.appBar}>
            <Typography variant="title" color="inherit" noWrap className={classes.title}>
              Avalanche Report
            </Typography>
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

ReportForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(ReportForm);