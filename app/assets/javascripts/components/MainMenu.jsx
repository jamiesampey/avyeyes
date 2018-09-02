import React from 'react';
import PropTypes from 'prop-types';
import Drawer from '@material-ui/core/Drawer';
import IconButton from '@material-ui/core/IconButton';
import ChevronLeftIcon from '@material-ui/icons/ChevronLeft';
import {withStyles} from '@material-ui/core/styles';
import ExpansionPanel from "@material-ui/core/ExpansionPanel";
import ExpansionPanelSummary from "@material-ui/core/ExpansionPanelSummary";
import Typography from "@material-ui/core/Typography";
import ExpansionPanelDetails from "@material-ui/core/ExpansionPanelDetails";
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import HelpIcon from '@material-ui/icons/Help';
import FilterForm from "./FilterForm";
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
  drawerAppTitle: {
    display: 'flex',
    alignItems: 'left',
    justifyContent: 'flex-end',
    padding: '0 8px',
    fontSize: theme.typography.pxToRem(35),
    fontWeight: theme.typography.fontWeightRegular,
  },
  panelDetails: {
    paddingTop: 0,
  },
  closeMenuButton: {
    marginLeft: 'auto',
  },
  drawerSectionHeading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular,
  },
  helpIconButton: {
    height: 24,
    width: 24,
    marginLeft: 10,
  }
});

const MainMenu = props => {

  const { classes, menuPanel, changeMenuPanel, clientData, filter, applyFilter, clearFilter, showHelp } = props;

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
        <div className={classes.drawerAppTitle}>
          AvyEyes
          <IconButton className={classes.closeMenuButton} onClick={() => changeMenuPanel(null)}>
            <ChevronLeftIcon/>
          </IconButton>
        </div>
        <ExpansionPanel expanded={menuPanel === FilterMenuPanel} onClick={() => changeMenuPanel(FilterMenuPanel)}>
          <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
            <Typography className={classes.drawerSectionHeading}>
              Avalanche Filter
              <IconButton
                size="small"
                disableRipple
                className={classes.helpIconButton}
                onClick={(e) => {e.stopPropagation(); showHelp({ title: "Avalanche Filter Help", content: clientData.help.filterHelpContent })} }
              >
                <HelpIcon/>
              </IconButton>
            </Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails className={classes.panelDetails}>
            <FilterForm
              clientData={clientData}
              filter={filter}
              applyFilter={applyFilter}
              clearFilter={clearFilter}
            />
          </ExpansionPanelDetails>
        </ExpansionPanel>
        <ExpansionPanel expanded={menuPanel === ReportMenuPanel} onClick={() => changeMenuPanel(ReportMenuPanel)}>
          <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
            <Typography className={classes.drawerSectionHeading}>Report an Avalanche</Typography>
          </ExpansionPanelSummary>
          <ExpansionPanelDetails className={classes.panelDetails}>
            <Typography>
              Vertical stepper for creating a report
            </Typography>
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
