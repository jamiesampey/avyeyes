import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import AppBar from "@material-ui/core/AppBar";
import Typography from "@material-ui/core/Typography";
import Drawer from "@material-ui/core/Drawer";
import IconButton from "@material-ui/core/IconButton";
import FormControl from "@material-ui/core/FormControl";
import Tooltip from "@material-ui/core/Tooltip";
import InputLabel from "@material-ui/core/InputLabel";
import Input from "@material-ui/core/Input";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";

import ViewListIcon from "@material-ui/icons/ViewList";
import ImageIcon from "@material-ui/icons/Image";
import CommentsIcon from "@material-ui/icons/InsertComment";

const styles = theme => ({
  formRoot: {
    height: 600,
    width: 800,
    zIndex: 1,
    overflow: 'hidden',
    position: 'relative',
  },
  appBar: {
    zIndex: theme.zIndex.drawer + 1,
  },
  title: {
    flex: 1,
    paddingLeft: 15,
  },
  drawerPaper: {
    position: 'relative',
  },
  content: {
    backgroundColor: theme.palette.background.default,
    padding: theme.spacing.unit * 3,
    minWidth: 0, // So the Typography noWrap works
  },
  toolbar: theme.mixins.toolbar,
});

class ReportForm extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      report: {}
    };
  }

  render() {
    const { classes, clientData, openReport, drawing, closeCallback } = this.props;
    if (!clientData) return null;

    // console.info(`clientData is ${JSON.stringify(clientData)}`);

    return (
      <Dialog
        open={openReport}
        onClose={closeCallback}
        aria-labelledby="form-dialog-title"
      >
        <div className={classes.formRoot}>
          <AppBar position="absolute" className={classes.appBar}>
            <Typography variant="title" color="inherit" noWrap className={classes.title}>
              Avalanche Report
            </Typography>
          </AppBar>
          <Drawer
            variant="permanent"
            classes={{
              paper: classes.drawerPaper,
            }}
          >
            <div className={classes.toolbar} />
            <IconButton className={classes.button} aria-label="SWAG Details">
              <ViewListIcon />
            </IconButton>
            <IconButton className={classes.button} aria-label="Avalanche Images">
              <ImageIcon />
            </IconButton>
            <IconButton className={classes.button} aria-label="Submitter's Comments">
              <CommentsIcon />
            </IconButton>
          </Drawer>
          <main>
            <form className={classes.formRoot}>
              <FormControl className={classes.formField}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterEmail}>
                  <InputLabel className={classes.fieldLabel} htmlFor="submitterEmail" shrink={true}>Submitter Email</InputLabel>
                </Tooltip>
                <Input
                  id="submitterEmail"
                  type="text"
                  value={this.state.report.submitterExp}
                  // onChange={}
                />
              </FormControl>
              <FormControl className={classes.formField}>
                <Tooltip placement="right" title={clientData.tooltips.avyFormSubmitterExp}>
                  <InputLabel className={classes.fieldLabel} htmlFor="submitterExp" shrink={true}>Submitter Experience Level</InputLabel>
                </Tooltip>
                <Select
                  value={this.state.report.submitterExp}
                  // onChange={}
                  // inputProps={{
                  //   name: 'age',
                  //   id: 'age-simple',
                  // }}
                >
                  {
                    clientData.codes.experienceLevel.map( expLevel => {
                      <MenuItem value={expLevel.value}>{expLevel.label}</MenuItem>
                    })
                  }
                </Select>
              </FormControl>
            </form>
          </main>
        </div>
      </Dialog>
    );
  };
}

ReportForm.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object,
  openReport: PropTypes.bool.isRequired,
  drawing: PropTypes.object,
  closeCallback: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportForm);