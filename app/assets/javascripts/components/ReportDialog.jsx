import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import DialogActions from "@material-ui/core/DialogActions";
import Button from "@material-ui/core/Button";
import DialogContent from "@material-ui/core/DialogContent";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Typography from "@material-ui/core/Typography";
import Drawer from "@material-ui/core/Drawer";
import IconButton from "@material-ui/core/IconButton";
import Tooltip from "@material-ui/core/Tooltip";

import ViewListIcon from "@material-ui/icons/ViewList";
import ImageIcon from "@material-ui/icons/Image";
import CommentsIcon from "@material-ui/icons/InsertComment";

import ReportDetails from "./ReportDetails";
import ReportComments from "./ReportComments";
import ReportImages from "./ReportImages";

import {mockAvalanche} from "../Constants"; // TODO remove after form dev


const styles = theme => ({
  dialogPaper: {
    width: 800,
    height: 700,
  },
  dialogContent: {
    flexGrow: 1,
    zIndex: 1,
    padding: 0,
    overflow: 'hidden',
    position: 'relative',
    display: 'flex',
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
  },
  title: {
    flex: 1,
    paddingLeft: 15,
  },
  drawerPaper: {
    position: 'relative',
    marginTop: 55,
    background: theme.palette.background.default,
  },
  main: {
    width: 'inherit',
    marginTop: 45,
    paddingTop: 16,
    flexGrow: 1,
    backgroundColor: theme.palette.background.default,
    padding: 24,
  },
  dialogActionsRoot: {
    height: 48,
    marginTop: 0,
    marginBottom: 0,
    marginLeft: 48,
    borderTop: `1px solid ${theme.palette.divider}`,
    background: theme.palette.background.default,
  }
});

const initAvalanche = (extId, location, slope, perimeter) => {
  return {
    extId: extId,
    location: location,
    slope: slope,
    perimeter: perimeter,
    viewable: false,
    submitterEmail: '',
    submitterExp: '',
    date: '',
    areaName: '',
    weather: {
      recentSnow: -1,
      recentWindSpeed: '',
      recentWindDirection: '',
    },
    classification: {
      avyType: '',
      trigger: '',
      triggerModifier: '',
      interface: '',
      rSize: -1,
      dSize: -1.0,
    },
    comments: '',
  }
};

const MainContent = {
  details: 0,
  images: 1,
  comments: 2,
};

class ReportDialog extends React.Component {

  constructor(props) {
    super(props);
    this.updateAvalanche = this.updateAvalanche.bind(this);
    this.renderMainContent = this.renderMainContent.bind(this);

    this.state = {
      avalanche: mockAvalanche, // TODO set back to null after form dev
      main: MainContent.details,
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.openReport && !this.state.avalanche) {
      let drawing = this.props.drawing;
      console.info(`initializing avalanche report ${prevProps.reportExtId} from drawing`);

      let location = {
        longitude: drawing.latitude,
        latitude: drawing.longitude,
        altitude: drawing.altitude,
      };

      let slope = {
        aspect: drawing.aspect,
        angle: drawing.angle,
        elevation: drawing.altitude,
      };

      this.setState({
        avalanche: initAvalanche(this.props.reportExtId, location, slope, drawing.perimeter),
        main: MainContent.details,
      });
    }
  }

  updateAvalanche(field, value) {
    let updated = this.state.avalanche;

    let fields = field.split('.');
    if (fields.length === 2) updated[fields[0]][fields[1]] = value;
    else updated[field] = value;

    this.setState({avalanche: updated});
  }

  renderMainContent() {
    switch (this.state.main) {
      case MainContent.images:
        return (
          <ReportImages
            clientData={this.props.clientData}
            avalanche={this.state.avalanche}
            updateAvalanche={this.updateAvalanche}
          />
        );
      case MainContent.comments:
        return (
          <ReportComments
            clientData={this.props.clientData}
            avalanche={this.state.avalanche}
            updateAvalanche={this.updateAvalanche}
          />
        );
      default:
        return (
          <ReportDetails
            clientData={this.props.clientData}
            avalanche={this.state.avalanche}
            updateAvalanche={this.updateAvalanche}
          />
        );
    }
  }

  render() {
    const { classes, clientData } = this.props;
    if (!clientData || !this.state || !this.state.avalanche) return null;

    console.info(`avalanche is ${JSON.stringify(this.state.avalanche)}`);

    return (
      <Dialog
        classes={{paper: classes.dialogPaper}}
        maxWidth={false}
        open={this.props.openReport}
        onBackdropClick={() => {}}
        onEscapeKeyDown={() => {}}
      >
        <DialogContent className={classes.dialogContent}>
          <AppBar position="absolute" className={classes.appBar}>
            <Toolbar disableGutters={true}>
              <Typography variant="h6" color="inherit" noWrap className={classes.title}>
                Avalanche Report
              </Typography>
            </Toolbar>
          </AppBar>
          <Drawer variant="permanent" classes={{paper: classes.drawerPaper}}>
            <Tooltip placement="right" title={clientData.tooltips.avyFormSWAGFields}>
              <IconButton className={classes.button} onClick={() => this.setState({main: MainContent.details})}>
                <ViewListIcon />
              </IconButton>
            </Tooltip>
            <Tooltip placement="right" title={clientData.tooltips.avyFormImages}>
              <IconButton className={classes.button} onClick={() => this.setState({main: MainContent.images})}>
                <ImageIcon />
              </IconButton>
            </Tooltip>
            <Tooltip placement="right" title={clientData.tooltips.avyFormComments}>
              <IconButton className={classes.button} onClick={() => this.setState({main: MainContent.comments})}>
                <CommentsIcon />
              </IconButton>
            </Tooltip>
          </Drawer>
          <main className={classes.main}>
            { this.renderMainContent() }
          </main>
        </DialogContent>
        <DialogActions classes={{root: classes.dialogActionsRoot}} onClick={this.props.callback}>
          <Button color="primary">
            Cancel
          </Button>
          <Button color="primary">
            Submit
          </Button>
        </DialogActions>
      </Dialog>
    );
  };
}

ReportDialog.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  openReport: PropTypes.bool.isRequired,
  reportExtId: PropTypes.string,
  drawing: PropTypes.object,
  callback: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportDialog);