import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from "@material-ui/core/TextField";
import MenuItem from "@material-ui/core/MenuItem";
import FormControl from "@material-ui/core/FormControl";
import InputLabel from "@material-ui/core/InputLabel";
import Select from "@material-ui/core/Select";
import Input from "@material-ui/core/Input";
import Chip from "@material-ui/core/Chip";
import { compositeLabelForDataCode } from "../Util";

const styles = theme => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  formControl: {
    margin: theme.spacing.unit,
    width: 240,
  },
  chips: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  chip: {
    margin: theme.spacing.unit / 4,
  }
});

const ITEM_HEIGHT = 48;
const ITEM_PADDING_TOP = 8;
const MenuProps = {
  PaperProps: {
    style: {
      maxHeight: ITEM_HEIGHT * 4.5 + ITEM_PADDING_TOP,
      width: 250,
    },
  },
};

class FilterForm extends React.Component {
  constructor(props) {
    super(props);

    this.dataCodeChips = this.dataCodeChips.bind(this);
    this.applyFilterToView = this.applyFilterToView.bind(this);
  }

  static dataCodeMenuItems(dataCodeObjects) {
    dataCodeObjects.sort((a, b) => { return a.value.localeCompare(b.value) });
    return dataCodeObjects.map(obj => (
      <MenuItem key={obj.value} value={obj.value}>{`${obj.value} - ${obj.label}`}</MenuItem>
    ));
  }

  dataCodeChips(dataCodes, dataCodeObjects) {
    let { classes } = this.props;

    const maxChipLength = 30;

    if (dataCodes.length === 1) {
      let compositeLabel = compositeLabelForDataCode(dataCodeObjects, dataCodes[0]);
      if (compositeLabel.length > maxChipLength) compositeLabel = `${compositeLabel.substring(0, maxChipLength)}...`;
      return (
        <div className={classes.chips}>
          <Chip key={dataCodes[0]} label={compositeLabel} className={classes.chip}/>
        </div>
      );
    } else {
      return (
        <div className={classes.chips}>
          {dataCodes.map(dataCode => (
            <Chip key={dataCode} label={dataCode} className={classes.chip} />
          ))}
        </div>
      );
    }
  }

  applyFilterToView(field, value) {
    let { filter, applyFilter } = this.props;
    filter[field] = value;
    applyFilter(filter);
  }

  render() {
    const { classes, clientData, filter } = this.props;

    if (typeof clientData === 'undefined' || typeof filter === 'undefined') return null;

    //console.info(`client.filter is ${JSON.stringify(filter)}`);

    return (
      <form className={classes.root} noValidate>
        <TextField
          id="fromDate"
          label="From"
          type="date"
          value={filter.fromDate}
          className={classes.textField}
          InputLabelProps={{
            shrink: true,
          }}
          onChange={e => { this.applyFilterToView('fromDate', e.target.value) }}
        />
        <TextField
          id="toDate"
          label="To"
          type="date"
          className={classes.textField}
          value={filter.toDate}
          InputLabelProps={{
            shrink: true,
          }}
          onChange={e => { this.applyFilterToView('toDate', e.target.value) }}
        />
        <FormControl className={classes.formControl}>
          <InputLabel htmlFor="multi-select-avytype" shrink={true}>Avalanche Type</InputLabel>
          <Select
            multiple
            value={filter.avyType}
            onChange={e => this.applyFilterToView('avyType', e.target.value)}
            input={<Input id="multi-select-avytype" />}
            renderValue={selected => this.dataCodeChips(selected, clientData.codes.avalancheType)}
            MenuProps={MenuProps}
          >
            {FilterForm.dataCodeMenuItems(clientData.codes.avalancheType)}
          </Select>
        </FormControl>
        <FormControl className={classes.formControl}>
          <InputLabel htmlFor="multi-select-trigger" shrink={true}>Avalanche Trigger</InputLabel>
          <Select
            multiple
            value={filter.trigger}
            onChange={e => this.applyFilterToView('trigger', e.target.value)}
            input={<Input id="multi-select-trigger" />}
            renderValue={selected => this.dataCodeChips(selected, clientData.codes.avalancheTrigger)}
            MenuProps={MenuProps}
          >
            {FilterForm.dataCodeMenuItems(clientData.codes.avalancheTrigger)}
          </Select>
        </FormControl>
        <FormControl className={classes.formControl}>
          <InputLabel htmlFor="multi-select-interface" shrink={true}>Avalanche Interface</InputLabel>
          <Select
            multiple
            value={filter.interface}
            onChange={e => this.applyFilterToView('interface', e.target.value)}
            input={<Input id="multi-select-interface" />}
            renderValue={selected => this.dataCodeChips(selected, clientData.codes.avalancheInterface)}
            MenuProps={MenuProps}
          >
            {FilterForm.dataCodeMenuItems(clientData.codes.avalancheInterface)}
          </Select>
        </FormControl>
      </form>
    )
  }
}

FilterForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(FilterForm);