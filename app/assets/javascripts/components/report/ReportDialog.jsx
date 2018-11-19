import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
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
    overflowY: 'auto',
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
      editKey: getRequestParam("edit"),
      csrfToken: getCSRFTokenFromCookie(),
      workingAvalanche: null,
      main: MainContent.details,
      errorFields: [],
      deleteReport: false,
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
    let resultMessage = null;
    let { workingAvalanche, csrfToken } = this.state;

    let errorFields = [];
    if (!workingAvalanche.areaName) errorFields.push('areaName');
    if (!workingAvalanche.date) errorFields.push('date');
    if (!workingAvalanche.slope.angle || workingAvalanche.slope.angle > 90 || workingAvalanche.slope.angle < 1) errorFields.push('angle');
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
        resultMessage = `SUCCESS: Avalanche with ID '${workingAvalanche.extId}' was successfully updated`;
      })
      .catch(error => {
        resultMessage = `ERROR updating avalanche with ID '${workingAvalanche.extId}'. Error is: ${error}`;
      })
      .finally(() => this.cleanup(resultMessage));
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
        resultMessage = `SUCCESS: New avalanche with ID '${workingAvalanche.extId}' was successfully added. A confirmation email will be sent shortly`;
      })
      .catch(error => {
        resultMessage = `ERROR adding new avalanche with ID '${workingAvalanche.extId}'. Error is: ${error}`;
      })
      .finally(() => this.cleanup(resultMessage));
    }
  }

  deleteReport() {
    let resultMessage = null;
    let { workingAvalanche, csrfToken } = this.state;

    fetch(`/api/avalanche/${workingAvalanche.extId}?csrfToken=${csrfToken}`, {
      method: 'DELETE',
    })
      .then(response => {
        checkStatus(response);
        resultMessage = `SUCCESS: Avalanche ${workingAvalanche.extId} was deleted`;
      })
      .catch(error => {
        resultMessage = `ERROR deleting avalanche ${workingAvalanche.extId}. Error is: ${error}`;
      })
      .finally(() => this.cleanup(resultMessage));
  }

  cleanup(message) {
    this.setState({
      workingAvalanche: null,
      deleteReport: false,
    }, () => {
      this.props.setInfoMessage(message);
      this.props.onClose();
    });
  }

  renderMainContent(isAdminView) {
    if (!this.state.workingAvalanche) return null;

    let { clientData, setInfoMessage } = this.props;
    let { main, workingAvalanche, errorFields, editKey, csrfToken } = this.state;

    switch (main) {
      case MainContent.images:
        return (
          <ReportImages
            clientData={clientData}
            extId={workingAvalanche.extId}
            setInfoMessage={setInfoMessage}
            editKey={editKey}
            csrfToken={csrfToken}
          />
        );
      case MainContent.comments:
        return (
          <ReportComments
            clientData={clientData}
            avalanche={workingAvalanche}
            updateAvalanche={this.updateAvalancheField}
          />
        );
      default:
        return (
          <ReportDetails
            clientData={clientData}
            avalanche={workingAvalanche}
            updateAvalanche={this.updateAvalancheField}
            errorFields={errorFields}
            isAdminView={isAdminView}
          />
        );
    }
  }

  render() {
    const { classes, clientData } = this.props;
    const { workingAvalanche, editKey, deleteReport } = this.state;

    let isAdminView = workingAvalanche && workingAvalanche.hasOwnProperty('viewable') && editKey;

    return (
      <div>
        <Dialog
          classes={{paper: classes.dialogPaper}}
          maxWidth={false}
          open={Boolean(workingAvalanche)}
          disableBackdropClick
          disableEscapeKeyDown
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
              <Tooltip placement="right" title={clientData.tooltips.avyReportSWAGFields}>
                <IconButton className={classes.button} onClick={() => this.setState({main: MainContent.details})}>
                  <ViewListIcon />
                </IconButton>
              </Tooltip>
              <Tooltip placement="right" title={clientData.tooltips.avyReportImages}>
                <IconButton className={classes.button} onClick={() => this.setState({main: MainContent.images})}>
                  <ImageIcon />
                </IconButton>
              </Tooltip>
              <Tooltip placement="right" title={clientData.tooltips.avyReportComments}>
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
            <Button color="secondary" onClick={() => this.setState({ deleteReport: true })}>
              Delete
            </Button>
            }
            <Button color="primary" onClick={() => this.cleanup(null)}>
              Cancel
            </Button>
            <Button color="primary" onClick={() => this.submitReport(editKey)}>
              Submit
            </Button>
          </DialogActions>
        </Dialog>

        {deleteReport &&
          <Dialog
            open={Boolean(deleteReport)}
            disableBackdropClick
            disableEscapeKeyDown
            maxWidth="xs"
          >
            <DialogTitle>Delete Report</DialogTitle>
            <DialogContent>
              <Typography variant="body1" color="textPrimary">
                Delete avalanche report {workingAvalanche.extId} ?
              </Typography>
            </DialogContent>
            <DialogActions>
              <Button onClick={() => this.setState({deleteReport: false})} color="primary">
                Cancel
              </Button>
              <Button onClick={this.deleteReport} color="primary">
                Delete
              </Button>
            </DialogActions>
          </Dialog>
        }

      </div>
    );
  };
}

ReportDialog.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object,
  setInfoMessage: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportDialog);