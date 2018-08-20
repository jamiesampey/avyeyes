import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import classNames from 'classnames';
import TextField from "@material-ui/core/TextField";
import MenuItem from "@material-ui/core/MenuItem";

const styles = theme => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    margin: theme.spacing.unit,
    width: 200,
  },
});

class FilterForm extends React.Component {
  constructor(props) {
    super(props);

    this.applyFilterToView = this.applyFilterToView.bind(this);
  }

  static dataCodeMenuItems(dataCodes) {
    dataCodes.sort((a, b) => { return a.value.localeCompare(b.value) });
    return dataCodes.map(obj => (
      <MenuItem key={obj.value} value={obj.value}>{`${obj.value} - ${obj.label}`}</MenuItem>
    ));
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
        <TextField
          select
          label="Avalanche Type"
          value={filter.avyType}
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.applyFilterToView('avyType', e.target.value) }}
          InputLabelProps={{
            shrink: true,
          }}
        >
          {FilterForm.dataCodeMenuItems(clientData.codes.avalancheType)}
        </TextField>
        <TextField
          select
          label="Avalanche Trigger"
          value={filter.trigger}
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.applyFilterToView('trigger', e.target.value) }}
          InputLabelProps={{
            shrink: true,
          }}
          SelectProps={{ MenuProps: { MenuListProps: { style: { maxHeight: 450 } } } }}
        >
          {FilterForm.dataCodeMenuItems(clientData.codes.avalancheTrigger)}
        </TextField>
        <TextField
          select
          label="Avalanche Interface"
          value={filter.interface}
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.applyFilterToView('interface', e.target.value) }}
          InputLabelProps={{
            shrink: true,
          }}
        >
          {FilterForm.dataCodeMenuItems(clientData.codes.avalancheInterface)}
        </TextField>
      </form>
    )
  }
}

FilterForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(FilterForm);