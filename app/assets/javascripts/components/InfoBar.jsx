import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Snackbar from "@material-ui/core/Snackbar";
import IconButton from "@material-ui/core/IconButton";
import CloseIcon from "@material-ui/icons/Close";

const styles = theme => ({
  snackbarRoot: {
    backgroundColor: 'red',
  },
  snackbarMessage: {
    maxWidth: 420,
  },
  snackbarAction: {
    marginBottom: 'auto',
    marginRight: -20,
  },
  closeIconButton: {
    color: 'white',
  },
});


const InfoBar = props => {

  let { classes, open, message, duration, closeable, closeCallback } = props;

  return (
    <Snackbar
      anchorOrigin={{vertical: 'top', horizontal: 'center'}}
      open={open}
      autoHideDuration={duration ? duration * 1000 : null}
      onClose={closeCallback}
      ContentProps={{
        classes: {
          root: classes.snackbarRoot,
          message: classes.snackbarMessage,
          action: classes.snackbarAction,
        }
      }}
      message={message}
      action={[
        closeable &&
        <IconButton
          className={classes.closeIconButton}
          key="closeSnackbar"
          size="small"
          onClick={closeCallback}
        >
          <CloseIcon/>
        </IconButton>
      ]}
    />
  )
};

InfoBar.propTypes = {
  classes: PropTypes.object.isRequired,
  closeable: PropTypes.bool,
};

export default withStyles(styles)(InfoBar);