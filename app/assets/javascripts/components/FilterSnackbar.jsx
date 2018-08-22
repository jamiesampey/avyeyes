import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Snackbar from "@material-ui/core/Snackbar";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Button from "@material-ui/core/Button";

const styles = theme => ({
  snackbar: {

  },
  filterTable: {

  },
  labelCell: {
    marginLeft: 5,
  },
});

const FilterSnackbar = props => {
  const { classes, filter, clearFilter } = props;

  let filterInEffect = () => {
    if (!filter) return false;
    return filter.fromDate !== ''
      || filter.toDate !== ''
      || filter.avyTypes.length > 0
      || filter.triggers.length > 0
      || filter.interfaces.length > 0
      || filter.rSize > 0
      || filter.dSize > 0;
  };

  let filterTable = (
    <Table className={classes.filterTable}>
      <TableBody>
        { (filter.fromDate.length > 0 || filter.toDate.length > 0) &&
          <TableRow>
            <TableCell className={classes.labelCell}>Avalanche Dates:</TableCell>
            <TableCell padding="none">
              {filter.fromDate.length > 0 ? filter.fromDate : <span>&infin;</span>} to {filter.toDate.length > 0 ? filter.toDate : <span>&infin;</span>}
            </TableCell>
          </TableRow>
        }
        { filter.avyTypes.length > 0 &&
          <TableRow>
            <TableCell padding="none" className={classes.labelCell}>Avalanche Type:</TableCell>
            <TableCell padding="none">{filter.avyTypes.join(', ')}</TableCell>
          </TableRow>
        }
        { filter.triggers.length > 0 &&
          <TableRow>
            <TableCell padding="none" className={classes.labelCell}>Avalanche Trigger:</TableCell>
            <TableCell padding="none">{filter.triggers.join(', ')}</TableCell>
          </TableRow>
        }
        { filter.interfaces.length > 0 &&
          <TableRow>
            <TableCell padding="none" className={classes.labelCell}>Avalanche Interface:</TableCell>
            <TableCell padding="none">{filter.interfaces.join(', ')}</TableCell>
          </TableRow>
        }
        { (filter.rSize > 0 || filter.dSize > 0) &&
          <TableRow>
            <TableCell padding="none" className={classes.labelCell}>Avalanche Size:</TableCell>
            <TableCell padding="none">
              {filter.rSize > 0 ? `R${filter.rSize}` : ''} {filter.dSize > 0 ? `D${filter.dSize}` : ''}
            </TableCell>
          </TableRow>
        }
      </TableBody>
    </Table>
  );

  return (
    <Snackbar
      className={classes.snackbar}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
      open={filterInEffect()}
      onClose={() => {}}
      message={filterTable}
      action={[
        <Button
          key="clearFilter"
          color="secondary"
          size="small"
          onClick={clearFilter}
        >
          Clear Filter
        </Button>
      ]}
    />
  );
};

FilterSnackbar.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(FilterSnackbar);