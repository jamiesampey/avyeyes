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
import { FilterMenuPanel, ReportMenuPanel, HelpMenuPanel } from "../Constants";

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
  closeMenuButton: {
    marginLeft: 'auto',
  },
  drawerSectionHeading: {
    fontSize: theme.typography.pxToRem(15),
    fontWeight: theme.typography.fontWeightRegular,
  },
});

class MenuDrawer extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const {classes, menuPanel, changeMenuPanel} = this.props;

    return (
      <div className={classes.root}>
        <Drawer
          variant="temporary"
          anchor="left"
          open={Boolean(menuPanel)}
          ModalProps={{onBackdropClick: (event) => changeMenuPanel(null)}}
          classes={{
            paper: classes.drawerPaper,
          }}
        >
          <div className={classes.drawerAppTitle}>
            AvyEyes
            <IconButton className={classes.closeMenuButton} onClick={(event) => changeMenuPanel(null)}>
              <ChevronLeftIcon/>
            </IconButton>
          </div>
          <ExpansionPanel expanded={menuPanel === FilterMenuPanel} onClick={(event) => changeMenuPanel(FilterMenuPanel)}>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
              <Typography className={classes.drawerSectionHeading}>Avalanche Filter</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
              <Typography>
                a bunch of filter controls
              </Typography>
            </ExpansionPanelDetails>
          </ExpansionPanel>
          <ExpansionPanel expanded={menuPanel === ReportMenuPanel} onClick={(event) => changeMenuPanel(ReportMenuPanel)}>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
              <Typography className={classes.drawerSectionHeading}>Report an Avalanche</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
              <Typography>
                Vertical stepper for creating a report
              </Typography>
            </ExpansionPanelDetails>
          </ExpansionPanel>
          <ExpansionPanel expanded={menuPanel === HelpMenuPanel} onClick={(event) => changeMenuPanel(HelpMenuPanel)}>
            <ExpansionPanelSummary expandIcon={<ExpandMoreIcon/>}>
              <Typography className={classes.drawerSectionHeading}>AvyEyes Help</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails>
              <Typography>
                help text
              </Typography>
            </ExpansionPanelDetails>
          </ExpansionPanel>
        </Drawer>
      </div>
    );
  }
}

MenuDrawer.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(MenuDrawer);
