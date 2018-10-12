import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import FormControl from "@material-ui/core/FormControl";
import Tooltip from "@material-ui/core/Tooltip";
import InputLabel from "@material-ui/core/InputLabel";
import Input from "@material-ui/core/Input";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";

import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import Table from "@material-ui/core/Table";
import TableCell from "@material-ui/core/TableCell";
import Slider from "@material-ui/lab/Slider/Slider";

const styles = theme => ({
  tableRow: {
    height: 30,
  },
  tableCell: {
    border: 0,
    // borderColor: theme.palette.divider,
    // borderRadius: 10,
    paddingTop: 0,
    paddingRight: 0,
    paddingBottom: 10,
    paddingLeft: 0,
  },
  formField: {
    margin: 10,
  },
  slider: {
    marginLeft: 0,
    marginTop: 12,
    width: 310,
  },
});

const ReportDetails = props => {
  const { classes, clientData, avalanche, updateAvalanche } = props;

  if (!avalanche) return null;

  return (
    <form>
      <Table>
        <TableBody>
          <TableRow className={classes.tableRow}>
            <TableCell colSpan={2} className={classes.tableCell} style={{paddingRight: 0}}>
              <FormControl required={true} className={classes.formField} style={{width: 450}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormAreaName}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Area Name</InputLabel>
                </Tooltip>
                <Input
                  type="text"
                  value={avalanche.areaName}
                  onChange={(event) => updateAvalanche("areaName", event.target.value)}
                />
              </FormControl>
              <FormControl required={true} className={classes.formField} style={{width: 150, float: 'right'}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormDate}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Avalanche Date</InputLabel>
                </Tooltip>
                <Input
                  type="date"
                  value={avalanche.areaName}
                  onChange={(event) => updateAvalanche("date", event.target.value)}
                />
              </FormControl>
            </TableCell>
          </TableRow>
          <TableRow className={classes.tableRow}>
            <TableCell className={classes.tableCell}>
              <FormControl required={true} className={classes.formField} style={{width: 320}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterEmail}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Submitter Email</InputLabel>
                </Tooltip>
                <Input
                  type="text"
                  value={avalanche.submitterEmail}
                  onChange={(event) => updateAvalanche("submitterEmail", event.target.value)}
                />
              </FormControl>
            </TableCell>
            <TableCell className={classes.tableCell} style={{paddingRight: 0}}>
              <FormControl required={true} className={classes.formField} style={{width: 320, float: 'right'}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterExp}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Submitter Experience Level</InputLabel>
                </Tooltip>
                <Select
                  value={avalanche.submitterExp}
                  onChange={(event) => updateAvalanche("submitterExp", event.target.value)}
                >
                  { clientData.codes.experienceLevel.map(expLevel => <MenuItem key={expLevel.value} value={expLevel.value}>{expLevel.label}</MenuItem>) }
                </Select>
              </FormControl>
            </TableCell>
          </TableRow>
          <TableRow className={classes.tableRow}>
            <TableCell className={classes.tableCell} style={{borderRadius: 20,}}>
              <FormControl className={classes.formField} style={{width: 320}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormType}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Avalanche Type</InputLabel>
                </Tooltip>
                <Select
                  value={avalanche.classification.avyType}
                  onChange={(event) => updateAvalanche("classification.avyType", event.target.value)}
                >
                  { clientData.codes.avalancheType.map(avyType => <MenuItem key={avyType.value} value={avyType.value}>{avyType.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField} style={{width: 320}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormTrigger}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Avalanche Trigger</InputLabel>
                </Tooltip>
                <Select
                  value={avalanche.classification.trigger}
                  onChange={(event) => updateAvalanche("classification.trigger", event.target.value)}
                >
                  { clientData.codes.avalancheTrigger.map(trigger => <MenuItem key={trigger.value} value={trigger.value}>{trigger.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField} style={{width: 320}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormInterface}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Avalanche Interface</InputLabel>
                </Tooltip>
                <Select
                  value={avalanche.classification.interface}
                  onChange={(event) => updateAvalanche("classification.interface", event.target.value)}
                >
                  { clientData.codes.avalancheInterface.map(avyInterface => <MenuItem key={avyInterface.value} value={avyInterface.value}>{avyInterface.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField} style={{width: 320}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormTriggerModifier}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Trigger Modifier</InputLabel>
                </Tooltip>
                <Select
                  value={avalanche.classification.triggerModifier}
                  onChange={(event) => updateAvalanche("classification.triggerModifier", event.target.value)}
                >
                  { clientData.codes.avalancheTriggerModifier.map(triggerModifier => <MenuItem key={triggerModifier.value} value={triggerModifier.value}>{triggerModifier.label}</MenuItem>) }
                </Select>
              </FormControl>
              <FormControl className={classes.formField} style={{marginTop: 16}}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormRsizeValue}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Relative Size</InputLabel>
                </Tooltip>
                <Slider
                  className={classes.slider}
                  value={avalanche.classification.rSize}
                  min={0}
                  max={5}
                  step={1}
                  onChange={(e, v) => { updateAvalanche('classification.rSize', v) }}
                />
              </FormControl>
              <FormControl className={classes.formField}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormDsizeValue}>
                  <InputLabel className={classes.fieldLabel} shrink={true}>Destructive Size</InputLabel>
                </Tooltip>
                <Slider
                  className={classes.slider}
                  value={avalanche.classification.dSize}
                  min={0}
                  max={5}
                  step={.5}
                  onChange={(e, v) => { updateAvalanche('classification.dSize', v) }}
                />
              </FormControl>
            </TableCell>
            <TableCell className={classes.tableCell} style={{paddingRight: 0}}>
              cell for slope and weather
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
};

export default withStyles(styles)(ReportDetails);