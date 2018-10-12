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
import FormControl from "@material-ui/core/FormControl";
import Tooltip from "@material-ui/core/Tooltip";
import InputLabel from "@material-ui/core/InputLabel";
import Input from "@material-ui/core/Input";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";

import ViewListIcon from "@material-ui/icons/ViewList";
import ImageIcon from "@material-ui/icons/Image";
import CommentsIcon from "@material-ui/icons/InsertComment";

import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import Table from "@material-ui/core/Table";
import TableCell from "@material-ui/core/TableCell";

import mockAvalanche from "../Constants"; // TODO remove after form dev

const styles = theme => ({
  dialogPaper: {
    width: 800,
    height: 600,
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
  },
  main: {
    marginTop: 45,
    paddingTop: 8,
    paddingRight: 0,
    flexGrow: 1,
    backgroundColor: theme.palette.background.default,
    padding: 24,
  },
  swagTableRow: {
    height: 30,
  },
  swagTableCell: {
    paddingTop: 8,
    paddingRight: 0,
    paddingBottom: 8,
    paddingLeft: 0,
  },
  formField: {
    margin: 10,
  },
  dialogActionsRoot: {
    marginLeft: 50,
    borderTop: '1px solid',
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

class ReportForm extends React.Component {

  constructor(props) {
    super(props);
    this.updateAvalanche = this.updateAvalanche.bind(this);

    this.state = {
      avalanche: mockAvalanche, // TODO set back to null after form dev
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (this.props.openReport && !this.state.avalanche) {
      let drawing = this.props.drawing;
      console.info(`initializing avalanche ${prevProps.reportExtId} from drawing`);

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
      });
    }
  }

  updateAvalanche(field, value) {
    let updated = this.state.avalanche;
    updated[field] = value;
    this.setState({avalanche: updated});
  }

  render() {
    const { classes, clientData } = this.props;
    if (!clientData || !this.state || !this.state.avalanche) return null;

    let { avalanche } = this.state;
    console.info(`avalanche is ${JSON.stringify(avalanche)}`);

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
              <Typography variant="title" color="inherit" noWrap className={classes.title}>
                Avalanche Report
              </Typography>
            </Toolbar>
          </AppBar>
          <Drawer variant="permanent" classes={{paper: classes.drawerPaper}}>
            <Tooltip placement="right" title={clientData.tooltips.avyFormSWAGFields}>
              <IconButton className={classes.button}>
                <ViewListIcon />
              </IconButton>
            </Tooltip>
            <Tooltip placement="right" title={clientData.tooltips.avyFormImages}>
              <IconButton className={classes.button}>
                <ImageIcon />
              </IconButton>
            </Tooltip>
            <Tooltip placement="right" title={clientData.tooltips.avyFormComments}>
              <IconButton className={classes.button}>
                <CommentsIcon />
              </IconButton>
            </Tooltip>
          </Drawer>
          <main className={classes.main}>
            <form>
              <Table>
                <TableBody>
                  <TableRow className={classes.swagTableRow}>
                    <TableCell className={classes.swagTableCell}>
                      <FormControl className={classes.formField} style={{width: 500}}>
                        <Tooltip placement="right" title={clientData.tooltips.avyFormAreaName}>
                          <InputLabel className={classes.fieldLabel} shrink={true}>Area Name</InputLabel>
                        </Tooltip>
                        <Input
                          type="text"
                          value={avalanche.areaName}
                          onChange={(event) => this.updateAvalanche("areaName", event.target.value)}
                        />
                      </FormControl>
                      <FormControl className={classes.formField} style={{width: 150, float: 'right'}}>
                        <Tooltip placement="right" title={clientData.tooltips.avyFormDate}>
                          <InputLabel className={classes.fieldLabel} shrink={true}>Avalanche Date</InputLabel>
                        </Tooltip>
                        <Input
                          type="date"
                          value={avalanche.areaName}
                          onChange={(event) => this.updateAvalanche("date", event.target.value)}
                        />
                      </FormControl>
                    </TableCell>
                  </TableRow>
                  <TableRow className={classes.swagTableRow}>
                    <TableCell className={classes.swagTableCell}>
                      <FormControl className={classes.formField} style={{width: 350}}>
                        <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterEmail}>
                          <InputLabel className={classes.fieldLabel} shrink={true}>Submitter Email</InputLabel>
                        </Tooltip>
                        <Input
                          type="text"
                          value={avalanche.submitterEmail}
                          onChange={(event) => this.updateAvalanche("submitterEmail", event.target.value)}
                        />
                      </FormControl>
                      <FormControl className={classes.formField} style={{width: 300, float: 'right'}}>
                        <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterExp}>
                          <InputLabel className={classes.fieldLabel} shrink={true}>Submitter Experience Level</InputLabel>
                        </Tooltip>
                        <Select
                          value={avalanche.submitterExp}
                          onChange={(event) => this.updateAvalanche("submitterExp", event.target.value)}
                        >
                          { clientData.codes.experienceLevel.map(expLevel => <MenuItem key={expLevel.value} value={expLevel.value}>{expLevel.label}</MenuItem>) }
                        </Select>
                      </FormControl>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </form>
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

ReportForm.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object,
  openReport: PropTypes.bool.isRequired,
  reportExtId: PropTypes.string,
  drawing: PropTypes.object,
  callback: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportForm);