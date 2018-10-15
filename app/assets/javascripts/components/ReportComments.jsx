import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import TextField from "@material-ui/core/TextField";

const styles = theme => ({
  instructions: {
    marginBottom: 20,
    color: theme.palette.text.primary,
  },
});

const ReportComments = props => {
  const { classes, clientData, avalanche, updateAvalanche } = props;

  if (!avalanche) return null;

  return (
    <div>
      <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyFormCommentsInstr}} />
      <TextField
        fullWidth
        multiline
        label="Comments"
        value={avalanche.comments}
        onChange={(event) => updateAvalanche("comments", event.target.value)}
        rows={21}
        variant="outlined"
      />
    </div>
  );
};

ReportComments.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
  updateAvalanche: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportComments);