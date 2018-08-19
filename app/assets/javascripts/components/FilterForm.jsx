import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import TextField from "@material-ui/core/TextField";

const styles = theme => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  textField: {
    marginLeft: theme.spacing.unit,
    marginRight: theme.spacing.unit,
    width: 200,
  },
});

class FilterForm extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      fromDate: '',
      toDate: '',
    };
  }

  render() {
    const { classes, filterAvalanches } = this.props;

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
      </form>
    )
  }
}

FilterForm.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(FilterForm);