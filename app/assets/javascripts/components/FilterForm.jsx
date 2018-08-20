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

    this.state = {
      fromDate: '',
      toDate: '',
      avyType: '',
      trigger: '',
      interface: '',
      rSize: '',
      dSize: '',
    };
  }

  static dataCodeMenuItems(dataCodes) {
    dataCodes.sort((a, b) => { return a.value.localeCompare(b.value) });
    return dataCodes.map(obj => (
      <MenuItem key={obj.value} value={obj.value}>{`${obj.value} - ${obj.label}`}</MenuItem>
    ));
  }

  render() {
    const { classes, clientData, filterAvalanches } = this.props;

    if (typeof clientData === 'undefined') return null;

    return (
      <form className={classes.root} noValidate>
        <TextField
          id="fromDate"
          label="From"
          type="date"
          className={classes.textField}
          InputLabelProps={{
            shrink: true,
          }}
          onChange={e => { this.setState({fromDate: e.target.value}, () => filterAvalanches(this.state)) }}
        />
        <TextField
          id="toDate"
          label="To"
          type="date"
          className={classes.textField}
          InputLabelProps={{
            shrink: true,
          }}
          onChange={e => { this.setState({toDate: e.target.value}, () => filterAvalanches(this.state)) }}
        />
        <TextField
          select
          label="Avalanche Type"
          value=''
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.setState({avyType: e.target.value}, () => filterAvalanches(this.state)) }}
          InputLabelProps={{
            shrink: true,
          }}
        >
          {FilterForm.dataCodeMenuItems(clientData.codes.avalancheType)}
        </TextField>
        <TextField
          select
          label="Avalanche Trigger"
          value=''
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.setState({avyType: e.target.value}, () => filterAvalanches(this.state)) }}
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
          value=''
          className={classNames(classes.margin, classes.textField)}
          onChange={e => { this.setState({interface: e.target.value}, () => filterAvalanches(this.state)) }}
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