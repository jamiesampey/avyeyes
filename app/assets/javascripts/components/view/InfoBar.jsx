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

  let { classes, open, message, duration, closeable, onClose } = props;

  return (
    <Snackbar
      anchorOrigin={{vertical: 'top', horizontal: 'center'}}
      open={open}
      autoHideDuration={duration ? duration * 1000 : null}
      onClose={onClose}
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
          onClick={onClose}
        >
          <CloseIcon/>
        </IconButton>
      ]}
    />
  )
};

InfoBar.propTypes = {
  classes: PropTypes.object.isRequired,
  open: PropTypes.bool.isRequired,
  message: PropTypes.string,
  duration: PropTypes.number,
  closeable: PropTypes.bool,
  onClose: PropTypes.func,
};

export default withStyles(styles)(InfoBar);