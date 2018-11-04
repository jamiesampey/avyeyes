import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';

const styles = theme => ({

});

const ReportImages = props => {
  const { classes, clientData, avalanche, updateAvalanche } = props;

  if (!avalanche) return null;

  return (
    <div>
      Images placeholder
    </div>
  );
};

ReportImages.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
  updateAvalanche: PropTypes.func.isRequired,
};

export default withStyles(styles)(ReportImages);