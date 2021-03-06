import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import classNames from 'classnames';
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Input from "@material-ui/core/Input";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";

import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import Table from "@material-ui/core/Table";
import TableCell from "@material-ui/core/TableCell";
import Slider from "@material-ui/lab/Slider";
import Typography from "@material-ui/core/Typography";
import Divider from "@material-ui/core/Divider";
import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel";
import Checkbox from "@material-ui/core/Checkbox/Checkbox";

const styles = theme => ({
  root: {
    maxHeight: '100%',
  },
  table: {
    borderCollapse: 'separate',
  },
  tableRow: {
    height: 30,
  },
  tableCell: {
    border: 0,
    margin: 10,
    paddingTop: 0,
    paddingRight: 0,
    paddingBottom: 0,
    paddingLeft: 0,
  },
  borderedTableCell: {
    borderWidth: 1,
    borderStyle: 'solid',
    borderColor: theme.palette.divider,
    borderRadius: 10,
  },
  adminTableCell: {
    borderColor: 'red',
  },
  borderedTableCellLabel: {
    marginLeft: 8,
    marginTop: -16,
    padding: 5,
    width: 'fit-content',
    color: theme.palette.text.secondary,
    background: theme.palette.background.default,
  },
  formField: {
    width: 320,
    marginTop: 12,
    marginRight: 10,
    marginBottom: 0,
    marginLeft: 10,
  },
  slopeFormField: {
    width: 80,
    marginTop: 10,
    marginRight: 10,
    marginBottom: 0,
    marginLeft: 10,
  },
  slider: {
    marginLeft: 0,
    marginTop: 32,
    paddingLeft: 6,
    width: 310,
  },
  sliderThumb: {
    position: 'absolute',
    zIndex: 10,
    borderRadius: '50%',
    backgroundColor: '#3f51b5',
  },
  sliderThumbValue: {
    marginTop: 14,
    color: theme.palette.text.primary,
    fontSize: '.8rem',
  },
});

const ReportDetails = props => {
  const { classes, clientData, avalanche, updateAvalanche, isAdminView } = props;

  const NoneMenuItem = <MenuItem key='none' value=''><i>None</i></MenuItem>;

  return (
    <form className={classes.root}>
      <Table className={classes.table}>
        <TableBody>
          <TableRow className={classes.tableRow}>
            <TableCell colSpan={2} className={classes.tableCell} style={{paddingRight: 0}}>
              <FormControl required={true} className={classes.formField} style={{width: 500}}>
                <InputLabel error={props.errorFields.includes('areaName')} shrink={true}>Area Name</InputLabel>
                <Input
                  inputProps={{id: "area-name"}}
                  type="text"
                  error={props.errorFields.includes('areaName')}
                  value={avalanche.areaName}
                  onChange={(event) => updateAvalanche("areaName", event.target.value)}
                />
              </FormControl>
              <FormControl required={true} className={classes.formField} style={{width: 150, float: 'right'}}>
                <InputLabel error={props.errorFields.includes('date')} shrink={true}>Avalanche Date</InputLabel>
                <Input
                  inputProps={{id: "avalanche-date"}}
                  type="date"
                  error={props.errorFields.includes('date')}
                  value={avalanche.date}
                  onChange={(event) => updateAvalanche("date", event.target.value)}
                />
              </FormControl>
            </TableCell>
          </TableRow>
          <TableRow className={classes.tableRow}>
            <TableCell className={classes.tableCell} style={{paddingTop: 16, paddingBottom: 32}}>
              <FormControl required={true} className={classes.formField}>
                <InputLabel error={props.errorFields.includes('submitterEmail')} shrink={true}>Submitter Email</InputLabel>
                <Input
                  inputProps={{id: "submitter-email"}}
                  type="text"
                  error={props.errorFields.includes('submitterEmail')}
                  value={avalanche.submitterEmail}
                  onChange={(event) => updateAvalanche("submitterEmail", event.target.value)}
                />
              </FormControl>
            </TableCell>
            <TableCell className={classes.tableCell} style={{paddingTop: 16, paddingRight: 0, paddingBottom: 32}}>
              <FormControl required={true} className={classes.formField} style={{float: 'right'}}>
                <InputLabel error={props.errorFields.includes('submitterExp')} shrink={true}>Submitter Experience Level</InputLabel>
                <Select
                  value={avalanche.submitterExp}
                  error={props.errorFields.includes('submitterExp')}
                  onChange={(event) => updateAvalanche("submitterExp", event.target.value)}
                >
                  { clientData.codes.experienceLevel.map(expLevel => <MenuItem key={expLevel.value} value={expLevel.value}>{expLevel.label}</MenuItem>) }
                </Select>
              </FormControl>
            </TableCell>
          </TableRow>
          <TableRow className={classes.tableRow}>
            <TableCell className={classNames(classes.tableCell, classes.borderedTableCell)} style={{paddingRight: 10, paddingBottom: 10}}>
              <Typography className={classes.borderedTableCellLabel}>SWAG Classification</Typography>
              <FormControl className={classes.formField} style={{marginTop: 6}}>
                <InputLabel htmlFor="avalanche-type">Avalanche Type</InputLabel>
                <Select
                  inputProps={{id: 'avalanche-type'}}
                  value={avalanche.classification.avyType}
                  onChange={(event) => updateAvalanche("classification.avyType", event.target.value)}
                >
                  { NoneMenuItem }
                  { clientData.codes.avalancheType.map(avyType => <MenuItem key={avyType.value} value={avyType.value}>{avyType.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField}>
                <InputLabel htmlFor="trigger">Avalanche Trigger</InputLabel>
                <Select
                  inputProps={{id: 'trigger'}}
                  value={avalanche.classification.trigger}
                  onChange={(event) => updateAvalanche("classification.trigger", event.target.value)}
                >
                  { NoneMenuItem }
                  { clientData.codes.avalancheTrigger.map(trigger => <MenuItem key={trigger.value} value={trigger.value}>{trigger.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField}>
                <InputLabel htmlFor="trigger-modifier">Trigger Modifier</InputLabel>
                <Select
                  inputProps={{id: 'trigger-modifier'}}
                  value={avalanche.classification.triggerModifier}
                  onChange={(event) => updateAvalanche("classification.triggerModifier", event.target.value)}
                >
                  { NoneMenuItem }
                  { clientData.codes.avalancheTriggerModifier.map(triggerModifier => <MenuItem key={triggerModifier.value} value={triggerModifier.value}>{triggerModifier.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField}>
                <InputLabel htmlFor="interface">Avalanche Interface</InputLabel>
                <Select
                  inputProps={{id: 'interface'}}
                  value={avalanche.classification.interface}
                  onChange={(event) => updateAvalanche("classification.interface", event.target.value)}
                >
                  { NoneMenuItem }
                  { clientData.codes.avalancheInterface.map(avyInterface => <MenuItem key={avyInterface.value} value={avyInterface.value}>{avyInterface.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField} style={{marginTop: 24}}>
                <InputLabel shrink={true}>Relative (R) Size</InputLabel>
                <Slider
                  className={classes.slider}
                  value={avalanche.classification.rSize}
                  min={0}
                  max={5}
                  step={1}
                  onChange={(e, v) => { updateAvalanche('classification.rSize', v) }}
                  thumb={<div className={classes.sliderThumb}><div className={classes.sliderThumbValue}>{avalanche.classification.rSize}</div></div>}
                />
              </FormControl>
              <FormControl className={classes.formField} style={{marginTop: 24, paddingBottom: 16}}>
                <InputLabel shrink={true}>Destructive (D) Size</InputLabel>
                <Slider
                  className={classes.slider}
                  value={avalanche.classification.dSize}
                  min={0}
                  max={5}
                  step={.5}
                  onChange={(e, v) => { updateAvalanche('classification.dSize', v) }}
                  thumb={<div className={classes.sliderThumb}><div className={classes.sliderThumbValue}>{avalanche.classification.dSize}</div></div>}
                />
              </FormControl>
            </TableCell>
            <TableCell className={classes.tableCell} style={{verticalAlign: 'top', paddingLeft: 10, paddingRight: 0}}>
              <Table className={classes.table}>
                <TableBody>
                  <TableRow className={classes.tableRow} style={{verticalAlign: 'top'}}>
                    <TableCell className={classNames(classes.tableCell, classes.borderedTableCell)} style={{paddingBottom: 16, paddingRight: 0}}>
                      <Typography className={classes.borderedTableCellLabel}>Slope</Typography>
                      <FormControl className={classes.slopeFormField} style={{width: 100}}>
                        <InputLabel shrink={true}>Elevation (m)</InputLabel>
                        <Input
                          inputProps={{id: "elevation"}}
                          type="text"
                          readOnly={true}
                          value={avalanche.slope.elevation}
                        />
                      </FormControl>
                      <FormControl className={classes.slopeFormField}>
                        <InputLabel shrink={true}>Aspect</InputLabel>
                        <Select
                          value={avalanche.slope.aspect}
                          onChange={(event) => updateAvalanche("slope.aspect", event.target.value)}
                        >
                          { clientData.codes.direction.map(dir => <MenuItem key={dir.value} value={dir.value}>{dir.label}</MenuItem>) }
                        </Select>
                      </FormControl>
                      <FormControl className={classes.slopeFormField}>
                        <InputLabel shrink={true} error={props.errorFields.includes('angle')}>Angle</InputLabel>
                        <Input
                          inputProps={{id: "angle"}}
                          type="text"
                          error={props.errorFields.includes('angle')}
                          value={avalanche.slope.angle}
                          onChange={(event) => updateAvalanche("slope.angle", parseInt(event.target.value) || 0)}
                        />
                      </FormControl>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
              <Divider style={{height: 0, margin: 10, border: 0}}/>
              <Table className={classes.table}>
                <TableBody>
                  <TableRow className={classes.tableRow} style={{verticalAlign: 'top'}}>
                    <TableCell className={classNames(classes.tableCell, classes.borderedTableCell)} style={{paddingBottom: 16, paddingRight: 0}}>
                      <Typography className={classes.borderedTableCellLabel}>Weather</Typography>
                      <FormControl className={classes.formField} style={{width: 90, paddingRight: 50}}>
                        <InputLabel htmlFor="recent-snow">Recent Snow (cm)</InputLabel>
                        <Input
                          inputProps={{id: "recent-snow"}}
                          type="text"
                          value={avalanche.weather.recentSnow}
                          onChange={(event) => updateAvalanche("weather.recentSnow", parseInt(event.target.value) || 0)}
                        />
                      </FormControl>
                      <FormControl className={classes.formField} style={{width: 160}}>
                        <InputLabel htmlFor="recent-wind-speed">Recent Wind</InputLabel>
                        <Select
                          inputProps={{id: 'recent-wind-speed'}}
                          value={avalanche.weather.recentWindSpeed}
                          onChange={(event) => updateAvalanche("weather.recentWindSpeed", event.target.value)}
                        >
                          { NoneMenuItem }
                          { clientData.codes.windSpeed.map(speed => <MenuItem key={speed.value} value={speed.value}>{speed.label}</MenuItem>) }
                        </Select>
                      </FormControl>
                      <FormControl className={classes.formField} style={{width: 80}}>
                        <InputLabel htmlFor="recent-wind-direction">from the</InputLabel>
                        <Select
                          inputProps={{id: 'recent-wind-direction'}}
                          value={avalanche.weather.recentWindDirection}
                          onChange={(event) => updateAvalanche("weather.recentWindDirection", event.target.value)}
                        >
                          { NoneMenuItem }
                          { clientData.codes.direction.map(dir => <MenuItem key={dir.value} value={dir.value}>{dir.label}</MenuItem>) }
                        </Select>
                      </FormControl>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
              <Divider style={{height: 0, margin: 10, border: 0}}/>

              { isAdminView &&
                <Table className={classes.table}>
                  <TableBody>
                    <TableRow className={classes.tableRow} style={{verticalAlign: 'top'}}>
                      <TableCell className={classNames(classes.tableCell, classes.borderedTableCell, classes.adminTableCell)} style={{paddingBottom: 16, paddingRight: 0}}>
                        <Typography className={classes.borderedTableCellLabel}>Admin</Typography>
                        <FormControlLabel style={{paddingLeft: 10}} label="Viewable"
                          control={
                            <Checkbox
                              checked={avalanche.viewable}
                              onChange={(event) => updateAvalanche("viewable", event.target.checked)}
                            />
                          }
                        />
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              }

            </TableCell>
          </TableRow>
        </TableBody>
      </Table>
    </form>
  );
};

ReportDetails.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
  updateAvalanche: PropTypes.func.isRequired,
  errorFields: PropTypes.array,
  isAdminView: PropTypes.bool.isRequired,
};

export default withStyles(styles)(ReportDetails);