import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Table from "@material-ui/core/Table";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableSortLabel from "@material-ui/core/TableSortLabel";
import TableBody from "@material-ui/core/TableBody";
import { checkStatusAndParseJson, avalancheUrl } from "../Util";
import Toolbar from "@material-ui/core/Toolbar";
import TextField from "@material-ui/core/TextField";
import InputAdornment from "@material-ui/core/InputAdornment";
import FilterListIcon from "@material-ui/icons/FilterList";
import Typography from "@material-ui/core/Typography";
import TablePagination from "@material-ui/core/TablePagination";
import Paper from "@material-ui/core/Paper";
import AdminUserChip from "./AdminUserChip";

const styles = theme => ({
  root: {
    width: '100%',
  },
  userChip: {
    marginLeft: 'auto',
  },
  filterInput: {
    marginLeft: 30,
  },
  filterIndicator: {
    marginRight: 5,
  },
});

const OrderByFields = {
  Created: { field: 'CreateTime', label: 'Created', filterable: false },
  Updated: { field: 'UpdateTime', label: 'Updated', filterable: false },
  ExtId: { field: 'ExtId', label: 'External ID', filterable: true },
  Viewable: { field: 'Viewable', label: 'Viewable', filterable: false },
  AreaName: { field: 'AreaName', label: 'Area Name', filterable: true },
  Submitter: { field: 'SubmitterEmail', label: 'Submitter', filterable: true },
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
      page: 0,
      rowsPerPage: 10,
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
    let queryParams = [
      `start=${this.state.page * this.state.rowsPerPage}`,
      `length=${this.state.rowsPerPage}`,
      `orderBy=${this.state.orderBy.field}`,
      `order=${this.state.order}`,
      `filter=${this.state.filter}`
    ];

    fetch(`/api/avalanche/table?${queryParams.join('&').trim()}`)
      .then(response => {
        return checkStatusAndParseJson(response);
      })
      .then(data => {
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

  static avalancheLink(extId, editKey, linkText) {
    return <a href={avalancheUrl(extId, editKey)} target="avyeyesAdminViewer">{linkText}</a>
  }

  render() {
    const { classes } = this.props;

    return (
      <Paper className={classes.root}>
        <Toolbar>
          <Typography variant="h5">Avalanches</Typography>
          <div className={classes.userChip}>
            <AdminUserChip/>
          </div>
          <TextField
            className={classes.filterInput}
            placeholder="Filter"
            value={this.state.filter}
            onChange={event => this.setState({filter: event.target.value}, this.requestTableData)}
            InputProps={{
              startAdornment: <InputAdornment position="start"><FilterListIcon/></InputAdornment>,
            }}
          />
        </Toolbar>
        <div>
          <Table>
            <TableHead>
              <TableRow>
                { Object.entries(OrderByFields).map(arr => arr[1]).map(orderByEntry => {
                  let fieldKey = orderByEntry.field, fieldLabel = orderByEntry.label;

                  let filterIndicator = this.state.filter && orderByEntry.filterable ?
                    <FilterListIcon color="secondary" className={classes.filterIndicator}/> : null;

                  return (
                    <TableCell key={fieldKey} padding="dense">
                      <TableSortLabel
                        active={this.state.orderBy.field === fieldKey}
                        direction={this.state.order}
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
                      <TableCell padding="dense">{AdminTable.avalancheLink(row.extId, row.editKey, row.extId)}</TableCell>
                      <TableCell padding="dense">{row.viewable ? "Yes" : "No"}</TableCell>
                      <TableCell padding="dense">{AdminTable.avalancheLink(row.extId, row.editKey, row.areaName)}</TableCell>
                      <TableCell padding="dense">{row.submitter}</TableCell>
                    </TableRow>
                  );
                })
              }
            </TableBody>
          </Table>
        </div>
        <TablePagination
          component="div"
          count={this.state.filteredRows}
          rowsPerPage={this.state.rowsPerPage}
          page={this.state.page}
          backIconButtonProps={{
            'aria-label': 'Previous Page',
          }}
          nextIconButtonProps={{
            'aria-label': 'Next Page',
          }}
          onChangePage={(event, page) => this.setState({ page: page }, this.requestTableData)}
          onChangeRowsPerPage={event => this.setState({ rowsPerPage: event.target.value }, this.requestTableData)}
        />
      </Paper>
    );
  }
}

AdminTable.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AdminTable);