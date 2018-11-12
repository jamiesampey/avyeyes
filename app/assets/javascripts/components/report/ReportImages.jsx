import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import ImageGrid from "./ImageGrid";

const styles = theme => ({
  root: {
    width: '100%',
    height: '100%',
  },
  instructions: {
    marginBottom: 20,
    color: theme.palette.text.primary,
  },
});

const ReportImages = props => {
  let { classes, clientData, avalanche } = props;

  return (
    <div className={classes.root}>
      {/*<div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportImagesInstr}} />*/}
      <ImageGrid s3config={clientData.s3} avalanche={avalanche} />
    </div>
  );
};

ReportImages.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
};

export default withStyles(styles)(ReportImages);