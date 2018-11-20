import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from "@material-ui/icons/Close";
import Dialog from "@material-ui/core/Dialog";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";


const styles = theme => ({
  dialog: {
    zIndex: 1000,
  },
  appBar: {
    position: 'relative',
    background: '#153570',
    color: 'white',
    padding: theme.spacing.unit,
  },
  closeButton: {
    marginLeft: 'auto',
    [theme.breakpoints.up('sm')]: {
      marginRight: -8,
    },
  },
  content: {
    padding: theme.spacing.unit,
    maxHeight: 400,
    overflowY: 'auto',
  },
});

class HelpDialog extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const { classes, title, contentString, onClose } = this.props;

    return (
        <Dialog
          className={classes.dialog}
          open={Boolean(contentString)}
          onClose={onClose}
        >
          <div>
            <AppBar className={classes.appBar}>
              <Toolbar variant="dense" disableGutters={true}>
                <Typography variant="h6" color="inherit">
                  {title}
                </Typography>
                <IconButton className={classes.closeButton} onClick={onClose} color="inherit">
                  <CloseIcon/>
                </IconButton>
              </Toolbar>
            </AppBar>
            <div
              className={classes.content}
              dangerouslySetInnerHTML={{__html: contentString}}
            />
          </div>
        </Dialog>
    );
  }
}

HelpDialog.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
  title: PropTypes.string.isRequired,
  contentString: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
};

export default withStyles(styles, { withTheme: true })(HelpDialog);
