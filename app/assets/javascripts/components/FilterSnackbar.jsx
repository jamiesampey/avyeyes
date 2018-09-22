import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Snackbar from "@material-ui/core/Snackbar";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import Button from "@material-ui/core/Button";
import Typography from "@material-ui/core/Typography";

const styles = theme => ({
  filterTableTitle: {
    color: 'white',
    marginBottom: 10,
  },
  filterTable: {
    '& tr': {
      height: 25,
    },
    '& tr td': {
      color: 'white',
      padding: 0,
      border: 0,
      paddingRight: 10,
    }
  },
  snackbarRoot: {
    backgroundColor: "#303336",
    opacity: .85,
  },
  snackbarAction: {
    marginBottom: 'auto',
    marginRight: -20,
  },
  clearFilterButton: {
    color: 'white',
  },
});


const FilterSnackbar = props => {
  const { classes, drawerOpen, filter, clearFilter } = props;

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
    <div>
      <Typography variant="title" className={classes.filterTableTitle}>Filter</Typography>
      <Table className={classes.filterTable}>
        <TableBody>
          { (filter.fromDate.length > 0 || filter.toDate.length > 0) &&
            <TableRow>
              <TableCell>Avalanche Dates:</TableCell>
              <TableCell>
                {filter.fromDate.length > 0 ? filter.fromDate : <span>&infin;</span>} to {filter.toDate.length > 0 ? filter.toDate : <span>&infin;</span>}
              </TableCell>
            </TableRow>
          }
          { filter.avyTypes.length > 0 &&
            <TableRow>
              <TableCell>Avalanche Type:</TableCell>
              <TableCell>{filter.avyTypes.join(', ')}</TableCell>
            </TableRow>
          }
          { filter.triggers.length > 0 &&
            <TableRow>
              <TableCell>Avalanche Trigger:</TableCell>
              <TableCell>{filter.triggers.join(', ')}</TableCell>
            </TableRow>
          }
          { filter.interfaces.length > 0 &&
            <TableRow>
              <TableCell>Avalanche Interface:</TableCell>
              <TableCell>{filter.interfaces.join(', ')}</TableCell>
            </TableRow>
          }
          { (filter.rSize > 0 || filter.dSize > 0) &&
            <TableRow>
              <TableCell>Avalanche Size:</TableCell>
              <TableCell>
                {filter.rSize > 0 ? `R${filter.rSize}` : ''} {filter.dSize > 0 ? `D${filter.dSize}` : ''}
              </TableCell>
            </TableRow>
          }
        </TableBody>
      </Table>
    </div>
  );

  return (
    <Snackbar
      anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
      open={!drawerOpen && filterInEffect()}
      ContentProps={{
        classes: {
          root: classes.snackbarRoot,
          action: classes.snackbarAction,
        }
      }}
      message={filterTable}
      action={[
        <Button
          className={classes.clearFilterButton}
          key="clearFilter"
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
  drawerOpen: PropTypes.bool.isRequired,
  filter: PropTypes.object,
  clearFilter: PropTypes.func,
};

export default withStyles(styles)(FilterSnackbar);