import React from 'react';
import PropTypes from 'prop-types';
import Drawer from '@material-ui/core/Drawer';
import IconButton from '@material-ui/core/IconButton';
import {withStyles} from '@material-ui/core/styles';
import ExpansionPanel from "@material-ui/core/ExpansionPanel";
import ExpansionPanelSummary from "@material-ui/core/ExpansionPanelSummary";
import Typography from "@material-ui/core/Typography";
import ExpansionPanelDetails from "@material-ui/core/ExpansionPanelDetails";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import HelpIcon from '@material-ui/icons/Help';
import FilterForm from "./FilterForm";
import ReportSteps from "./ReportSteps";

import { FilterMenuPanel, ReportMenuPanel } from "../Constants";

const drawerWidth = 300;

const styles = theme => ({
  root: {
    flexGrow: 1,
  },
  drawerPaper: {
    position: 'relative',
    width: drawerWidth,
    backgroundColor: '#EAEAEA',
  },
  filterPanelDetails: {
    paddingTop: 0,
  },
  reportPanelDetails: {
    paddingTop: 0,
    paddingLeft: 16,
    paddingRight: 0,
    fontSize: '.9rem',
  },
  drawerSectionHeading: {
    fontSize: '1.2rem',
    fontWeight: theme.typography.fontWeightRegular,
  },
});

const MainMenu = props => {

  const {
    classes,
    menuPanel,
    changeMenuPanel,
    clientData,
    filter,
    applyFilter,
    clearFilter,
    showHelp,
    controller,
  } = props;

  return (
    <div className={classes.root}>
      <Drawer
        variant="temporary"
        anchor="left"
        open={Boolean(menuPanel)}
        ModalProps={{onBackdropClick: () => changeMenuPanel(null)}}
        classes={{
          paper: classes.drawerPaper,
        }}
      >
        <ExpansionPanel expanded={menuPanel === FilterMenuPanel} onClick={() => changeMenuPanel(FilterMenuPanel)}>
          <ExpansionPanelSummary expandIcon={menuPanel !== FilterMenuPanel ? <ExpandMoreIcon/> : null}>
            <Typography className={classes.drawerSectionHeading}>
              Avalanche Filter
              <IconButton
                size="small"
                disableRipple
                onClick={(e) => {e.stopPropagation(); showHelp({ title: "Avalanche Filter Help", content: clientData.help.filterHelpContent })} }
              >
                <HelpIcon/>
              </IconButton>
            </Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails className={classes.filterPanelDetails}>
            <FilterForm
              clientData={clientData}
              filter={filter}
              applyFilter={applyFilter}
              clearFilter={clearFilter}
            />
          </ExpansionPanelDetails>
        </ExpansionPanel>
        <ExpansionPanel expanded={menuPanel === ReportMenuPanel} onClick={() => changeMenuPanel(ReportMenuPanel)}>
          <ExpansionPanelSummary expandIcon={menuPanel !== ReportMenuPanel ? <ExpandMoreIcon/> : null}>
            <Typography className={classes.drawerSectionHeading}>Report an Avalanche</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails className={classes.reportPanelDetails}>
            <ReportSteps
              clientData={clientData}
              cesiumController={controller}
            />
          </ExpansionPanelDetails>
        </ExpansionPanel>
      </Drawer>
    </div>
  )
};

MainMenu.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  menuPanel: PropTypes.string,
  changeMenuPanel: PropTypes.func.isRequired,
  clientData: PropTypes.object,
  filter: PropTypes.object.isRequired,
  applyFilter: PropTypes.func.isRequired,
  clearFilter: PropTypes.func.isRequired,
  showHelp: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(MainMenu);
