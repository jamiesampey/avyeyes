import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogContentText from "@material-ui/core/DialogContentText";
import DialogActions from "@material-ui/core/DialogActions";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";

const styles = theme => ({
  root: {
    background: 'white',
    color: 'black',
    borderRadius: 5,
  },
  hiddenForm: {
    display: 'none',
  }
});

class AvyFormDialog extends React.Component {

  render() {
    const { classes, avalanche, closeCallback } = this.props;
    if (avalanche === null) return (<div className={classes.hiddenForm} />);

    this.props.setCursorStyle("default");

    if (avalanche.hasOwnProperty("viewable")) {
      console.info(`Received ADMIN details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.enableAdminControls().then(() => {
      // this.form.displayReadWriteForm(data);
      // });
    } else if (avalanche.hasOwnProperty("submitterEmail")) {
      console.info(`Received READ-WRITE details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.displayReadWriteForm(data);
    } else {
      console.info(`Received READ-ONLY details for avalanche ${JSON.stringify(avalanche)}`);
      // this.form.displayReadOnlyForm(movement.position, data);
    }

    return (
      <Dialog
        className={classes.root}
        open={avalanche !== null}
        onClose={closeCallback}
        aria-labelledby="form-dialog-title"
      >
        <DialogTitle id="form-dialog-title">{avalanche.title}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {avalanche.extId}
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            id="name"
            label="Email Address"
            type="email"
            fullWidth
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={closeCallback} color="primary">
            Cancel
          </Button>
        </DialogActions>
      </Dialog>
    );
  };
}

AvyFormDialog.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(AvyFormDialog);