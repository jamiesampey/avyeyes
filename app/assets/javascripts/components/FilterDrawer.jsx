import React from 'react';
import PropTypes from 'prop-types';

import Drawer from '@material-ui/core/Drawer';
import IconButton from '@material-ui/core/IconButton';
import {withStyles} from '@material-ui/core/styles';
import Typography from "@material-ui/core/Typography";
import HelpIcon from '@material-ui/icons/Help';
import MenuItem from "@material-ui/core/MenuItem";
import Chip from "@material-ui/core/Chip";
import FormControl from "@material-ui/core/FormControl";
import Tooltip from "@material-ui/core/Tooltip";
import InputLabel from "@material-ui/core/InputLabel";
import Input from "@material-ui/core/Input";
import Select from "@material-ui/core/Select";
import Slider from "@material-ui/lab/Slider";
import Button from "@material-ui/core/Button";


const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    width: 270,
    backgroundColor: '#EAEAEA',
  },
  filterHeading: {
    marginLeft: 16,
    marginTop: 16,
    marginBottom: 0,
    fontSize: '1.2rem',
    fontWeight: theme.typography.fontWeightRegular,
  },
  formRoot: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  formField: {
    margin: 16,
    width: 260,
  },
  fieldLabel: {
    '&:hover': {
      cursor: 'help',
    },
  },
  dateInput: {
    marginLeft: 0,
  },
  chips: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  chip: {
    margin: theme.spacing.unit / 4,
  },
  slider: {
    marginLeft: 0,
    marginTop: 12,
    width: 230,
  },
  clearButton: {
    margin: 16,
    marginLeft: 'auto',
  },
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

class FilterDrawer extends React.Component {

  constructor(props) {
    super(props);

    this.dataCodeChips = this.dataCodeChips.bind(this);
    this.applyFilterToView = this.applyFilterToView.bind(this);

    this.state = {
      open: false,
    }
  }

  static dataCodeMenuItems(dataCodeObjects) {
    dataCodeObjects.sort((a, b) => { return a.value.localeCompare(b.value) });
    return dataCodeObjects.map(obj => (
      <MenuItem key={obj.value} value={obj.value}>{`${obj.value} - ${obj.label}`}</MenuItem>
    ));
  }

  dataCodeChips(dataCodes) {
    let { classes } = this.props;

    return (
      <div className={classes.chips}>
        {dataCodes.map(dataCode => (
          <Chip key={dataCode} label={dataCode} className={classes.chip} />
        ))}
      </div>
    );
  }

  applyFilterToView(field, value) {
    let { filter, applyFilter } = this.props;
    filter[field] = value;
    applyFilter(filter);
  }

  render() {
    const {
      classes,
      drawerOpen,
      drawerClose,
      clientData,
      filter,
      clearFilter,
      showHelp,
    } = this.props;

    if (!filter) return null;

    return (
      <div className={classes.root}>
        <Drawer
          variant="temporary"
          anchor="left"
          open={drawerOpen}
          ModalProps={{onBackdropClick: drawerClose}}
          classes={{
            paper: classes.drawerPaper,
          }}
        >
          <Typography className={classes.filterHeading}>
            Avalanche Filter
            <IconButton
              size="small"
              disableRipple
              onClick={(e) => {
                e.stopPropagation();
                drawerClose();
                showHelp({
                  title: "Avalanche Filter Help",
                  content: clientData.help.filterHelpContent
                });
              }}
            >
              <HelpIcon/>
            </IconButton>
          </Typography>
          <form className={classes.formRoot} noValidate>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterFromDate}>
                <InputLabel className={classes.fieldLabel} htmlFor="textfield-fromDate" shrink={true}>From</InputLabel>
              </Tooltip>
              <Input
                id="textfield-fromDate"
                type="date"
                className={classes.dateInput}
                value={filter.fromDate}
                onChange={e => { this.applyFilterToView('fromDate', e.target.value) }}
              />
            </FormControl>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterToDate}>
                <InputLabel className={classes.fieldLabel} htmlFor="textfield-toDate" shrink={true}>To</InputLabel>
              </Tooltip>
              <Input
                id="textfield-toDate"
                type="date"
                className={classes.dateInput}
                value={filter.toDate}
                onChange={e => { this.applyFilterToView('toDate', e.target.value) }}
              />
            </FormControl>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterType}>
                <InputLabel className={classes.fieldLabel} htmlFor="multi-select-avytype" shrink={true}>Avalanche Type</InputLabel>
              </Tooltip>
              <Select
                multiple
                value={filter.avyTypes}
                onChange={e => this.applyFilterToView('avyTypes', e.target.value)}
                input={<Input id="multi-select-avytype" />}
                renderValue={selected => this.dataCodeChips(selected)}
                MenuProps={MenuProps}
              >
                {FilterDrawer.dataCodeMenuItems(clientData.codes.avalancheType)}
              </Select>
            </FormControl>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterTrigger}>
                <InputLabel className={classes.fieldLabel} htmlFor="multi-select-trigger" shrink={true}>Avalanche Trigger</InputLabel>
              </Tooltip>
              <Select
                multiple
                value={filter.triggers}
                onChange={e => this.applyFilterToView('triggers', e.target.value)}
                input={<Input id="multi-select-trigger" />}
                renderValue={selected => this.dataCodeChips(selected)}
                MenuProps={MenuProps}
              >
                {FilterDrawer.dataCodeMenuItems(clientData.codes.avalancheTrigger)}
              </Select>
            </FormControl>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterInterface}>
                <InputLabel className={classes.fieldLabel} htmlFor="multi-select-interface" shrink={true}>Avalanche Interface</InputLabel>
              </Tooltip>
              <Select
                multiple
                value={filter.interfaces}
                onChange={e => this.applyFilterToView('interfaces', e.target.value)}
                input={<Input id="multi-select-interface" />}
                renderValue={selected => this.dataCodeChips(selected)}
                MenuProps={MenuProps}
              >
                {FilterDrawer.dataCodeMenuItems(clientData.codes.avalancheInterface)}
              </Select>
            </FormControl>
            <FormControl className={classes.formField} style={{marginTop: 16}}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterRsize}>
                <InputLabel className={classes.fieldLabel} shrink={true}>Relative Size</InputLabel>
              </Tooltip>
              <Slider
                className={classes.slider}
                value={filter.rSize}
                min={0}
                max={5}
                step={1}
                onChange={(e, v) => { this.applyFilterToView('rSize', v) }}
              />
            </FormControl>
            <FormControl className={classes.formField}>
              <Tooltip placement="right" title={clientData.tooltips.avyFilterDsize}>
                <InputLabel className={classes.fieldLabel} shrink={true}>Destructive Size</InputLabel>
              </Tooltip>
              <Slider
                className={classes.slider}
                value={filter.dSize}
                min={0}
                max={5}
                step={.5}
                onChange={(e, v) => { this.applyFilterToView('dSize', v) }}
              />
            </FormControl>
            <Button
              variant="contained"
              color="primary"
              size="small"
              className={classes.clearButton}
              onClick={clearFilter}
            >
              Clear Filter
            </Button>
          </form>
        </Drawer>
      </div>
    )
  }
}

FilterDrawer.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  drawerOpen: PropTypes.bool.isRequired,
  drawerClose: PropTypes.func.isRequired,
  clientData: PropTypes.object,
  filter: PropTypes.object.isRequired,
  applyFilter: PropTypes.func.isRequired,
  clearFilter: PropTypes.func.isRequired,
  showHelp: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(FilterDrawer);
