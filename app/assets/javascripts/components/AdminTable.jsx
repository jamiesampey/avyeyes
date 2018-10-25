import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import TableBody from "@material-ui/core/TableBody";
import {checkStatusAndParseJson} from "../Util";
import Toolbar from "@material-ui/core/Toolbar";
import TextField from "@material-ui/core/TextField";
import InputAdornment from "@material-ui/core/InputAdornment";
import FilterListIcon from "@material-ui/icons/FilterList";
import Typography from "@material-ui/core/Typography";

const styles = theme => ({
  filterInput: {
    marginLeft: 'auto',
  },
  filterIndicator: {
    marginRight: 5,
  },
});

const OrderByFields = {
  Created: { field: 'CreateTime', label: 'Created' },
  Updated: { field: 'UpdateTime', label: 'Updated' },
  ExtId: { field: 'ExtId', label: 'External ID' },
  Viewable: { field: 'Viewable', label: 'Viewable' },
  AreaName: { field: 'AreaName', label: 'Area Name' },
  Submitter: { field: 'SubmitterEmail', label: 'Submitter' },
};

const Order = {
  Asc: 'asc',
  Desc: 'desc',
};

class AdminTable extends React.Component {

  constructor(props) {
    super(props);

    this.requestTableData = this.requestTableData.bind(this);
    this.handleColumnSort = this.handleColumnSort.bind(this);

    this.state = {
      orderBy: OrderByFields.Created,
      order: Order.Desc,
      filter: '',
      totalRows: 0,
      filteredRows: 0,
      rows: [],
    };

    this.requestTableData();
  }

  requestTableData() {
    let queryParams = `start=0&length=100&orderBy=${this.state.orderBy.field}&order=${this.state.order}&filter=${this.state.filter}`;

    fetch(`/api/avalanche/table?${queryParams}`)
      .then(response => {
        return checkStatusAndParseJson(response);
      })
      .then(data => {
        console.info(`setting state.rows to ${JSON.stringify(data.records)}`);
        this.setState({
          totalRows: data.recordsTotal,
          filteredRows: data.recordsFiltered,
          rows: data.records,
        });
      })
      .catch(error => {
        console.error(`Error retrieving avalanche table data: ${error}`);
      });
  }

  handleColumnSort(orderByField) {
    let order = this.state.orderBy === orderByField && this.state.order === Order.Desc ? Order.Asc : Order.Desc;
    this.setState({ orderBy: orderByField, order: order }, this.requestTableData);
  }

  render() {
    const { classes } = this.props;
    const { orderBy, order } = this.state;

    return (
      <div>
        <Toolbar>
          <Typography variant="h5">Avalanches</Typography>
          <TextField
            className={classes.filterInput}
            placeholder="Filter"
            value={this.state.filter}
            onChange={(event) => this.setState({filter: event.target.value}, this.requestTableData)}
            InputProps={{
              startAdornment: <InputAdornment position="start"><FilterListIcon/></InputAdornment>,
            }}
          />
        </Toolbar>
        <Table>
          <TableHead>
            <TableRow>
              { Object.entries(OrderByFields).map(arr => arr[1]).map(orderByEntry => {
                let fieldKey = orderByEntry.field, fieldLabel = orderByEntry.label;

                let filterIndicator = this.state.filter && (
                  orderByEntry === OrderByFields.ExtId ||
                  orderByEntry === OrderByFields.AreaName ||
                  orderByEntry === OrderByFields.Submitter
                ) ? <FilterListIcon color="secondary" className={classes.filterIndicator}/> : null;

                return (
                  <TableCell key={fieldKey} padding="dense">
                    <TableSortLabel
                      active={orderBy.field === fieldKey}
                      direction={order}
                      onClick={() => this.handleColumnSort(orderByEntry)}
                    >
                      {filterIndicator}{fieldLabel}
                    </TableSortLabel>
                  </TableCell>
                );
              })}
            </TableRow>
          </TableHead>
          <TableBody>
            { this.state.rows.map(row => {
                return (
                  <TableRow key={row.extId}>
                    <TableCell padding="dense">{row.created}</TableCell>
                    <TableCell padding="dense">{row.updated}</TableCell>
                    <TableCell padding="dense">{row.extId}</TableCell>
                    <TableCell padding="dense">{row.viewable ? "Yes" : "No"}</TableCell>
                    <TableCell padding="dense">{row.areaName}</TableCell>
                    <TableCell padding="dense">{row.submitter}</TableCell>
                  </TableRow>
                );
              })
            }
          </TableBody>
        </Table>
      </div>
    );
  }
}

AdminTable.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AdminTable);