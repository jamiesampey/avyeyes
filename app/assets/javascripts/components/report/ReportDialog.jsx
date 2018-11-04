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

import {getRequestParam, getCSRFTokenFromCookie, checkStatus} from "../../Util";

const styles = theme => ({
  dialogPaper: {
    width: 800,
    height: 700,
    maxHeight: 700,
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
  },
});

const MainContent = {
  details: 0,
  images: 1,
  comments: 2,
};

class ReportDialog extends React.Component {

  constructor(props) {
    super(props);
    this.updateAvalancheField = this.updateAvalancheField.bind(this);
    this.submitReport = this.submitReport.bind(this);
    this.deleteReport = this.deleteReport.bind(this);
    this.renderMainContent = this.renderMainContent.bind(this);
    this.cleanup = this.cleanup.bind(this);

    this.state = {
      workingAvalanche: null,
      main: MainContent.details,
      errorFields: [],
    };
  }

  componentDidMount() {
    this.setState({workingAvalanche: this.props.avalanche});
  }

  updateAvalancheField(field, value) {
    let updated = this.state.workingAvalanche;

    let fields = field.split('.');
    if (fields.length === 2) updated[fields[0]][fields[1]] = value;
    else updated[field] = value;

    this.setState({workingAvalanche: updated});
  }

  submitReport(editKey) {
    let { workingAvalanche } = this.state;
    let csrfToken = getCSRFTokenFromCookie();

    let errorFields = [];
    if (!workingAvalanche.areaName) errorFields.push('areaName');
    if (!workingAvalanche.date) errorFields.push('date');
    if (!workingAvalanche.submitterEmail) errorFields.push('submitterEmail');
    if (!workingAvalanche.submitterExp) errorFields.push('submitterExp');

    this.setState({errorFields: errorFields});
    if (errorFields.length > 0) {
      return;
    }

    if (editKey) {
      fetch(`/api/avalanche/${workingAvalanche.extId}?edit=${editKey}&csrfToken=${csrfToken}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(workingAvalanche),
      })
      .then(response => {
        checkStatus(response);
        console.info(`Updated report ${workingAvalanche.extId}. Server response is ${response.status}`);
      })
      .catch(error => console.error(`Error updating report ${workingAvalanche.extId}: ${error}`))
      .finally(this.cleanup);
    } else {
      fetch(`/api/avalanche/${workingAvalanche.extId}?csrfToken=${csrfToken}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(workingAvalanche),
      })
      .then(response => {
        checkStatus(response);
        console.info(`Submitted new report ${workingAvalanche.extId}. Server response is ${response.status}`);
      })
      .catch(error => console.error(`Error submitting new report ${workingAvalanche.extId}: ${error}`))
      .finally(this.cleanup);
    }
  }

  deleteReport() {
    let { workingAvalanche } = this.state;
    let csrfToken = getCSRFTokenFromCookie();

    fetch(`/api/avalanche/${workingAvalanche.extId}?csrfToken=${csrfToken}`, {
      method: 'DELETE',
    })
      .then(response => {
        checkStatus(response);
        console.info(`Deleted report ${workingAvalanche.extId}. Server response is ${response.status}`);
      })
      .catch(error => console.error(`Error deleting report ${workingAvalanche.extId}: ${error}`))
      .finally(this.cleanup);
  }

  cleanup() {
    this.setState({workingAvalanche: null}, this.props.closeCallback);
  }

  renderMainContent(isAdminView) {
    if (!this.state.workingAvalanche) return null;

    switch (this.state.main) {
      case MainContent.images:
        return (
          <ReportImages
            clientData={this.props.clientData}
            avalanche={this.state.workingAvalanche}
            updateAvalanche={this.updateAvalancheField}
          />
        );
      case MainContent.comments:
        return (
          <ReportComments
            clientData={this.props.clientData}
            avalanche={this.state.workingAvalanche}
            updateAvalanche={this.updateAvalancheField}
          />
        );
      default:
        return (
          <ReportDetails
            clientData={this.props.clientData}
            avalanche={this.state.workingAvalanche}
            updateAvalanche={this.updateAvalancheField}
            errorFields={this.state.errorFields}
            isAdminView={isAdminView}
          />
        );
    }
  }

  render() {
    const { classes, clientData } = this.props;

    let editKey = getRequestParam("edit");
    let isAdminView = this.state.workingAvalanche && this.state.workingAvalanche.hasOwnProperty('viewable') && editKey;

    return (
      <Dialog
        classes={{paper: classes.dialogPaper}}
        maxWidth={false}
        open={Boolean(this.state.workingAvalanche)}
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
            { this.renderMainContent(isAdminView) }
          </main>
        </DialogContent>
        <DialogActions classes={{root: classes.dialogActionsRoot}}>
          { isAdminView &&
          <Button color="secondary" onClick={this.deleteReport}>
            Delete
          </Button>
          }
          <Button color="primary" onClick={this.cleanup}>
            Cancel
          </Button>
          <Button color="primary" onClick={() => this.submitReport(editKey)}>
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
  avalanche: PropTypes.object,
  closeCallback: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportDialog);