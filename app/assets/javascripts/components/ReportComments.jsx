import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import OutlinedInput from '@material-ui/core/OutlinedInput';

const styles = theme => ({
  instructions: {
    marginBottom: 16,
  },
  textInput: {

  }
});

const ReportComments = props => {
  const { classes, clientData, avalanche, updateAvalanche } = props;

  if (!avalanche) return null;

  return (
    <div>
      <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyFormCommentsInstr}} />
      <OutlinedInput
        className={classes.textInput}
        fullWidth
        multiline
        rows={21}
        rowsMax={21}
        name="my name"
        label="my label"
        id="my-id"
        labelWidth={150}
        value={avalanche.comments}
        onChange={(event) => updateAvalanche("comments", event.target.value)}
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