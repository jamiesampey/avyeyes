import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import GridList from "@material-ui/core/GridList/GridList";
import GridListTile from "@material-ui/core/GridListTile/GridListTile";
import GridListTileBar from "@material-ui/core/GridListTileBar/GridListTileBar";
import IconButton from "@material-ui/core/IconButton/IconButton";
import MenuIcon from '@material-ui/icons/Menu';
import AWS from 'aws-sdk/dist/aws-sdk';
import TextField from "@material-ui/core/TextField/TextField";

const styles = theme => ({
  instructions: {
    marginBottom: 20,
    color: theme.palette.text.primary,
  },
  imageMenuIcon: {
    color: 'white',
  },
});

class ReportImages extends React.Component {

  constructor(props) {
    super(props);

    this.signedImageUrl = this.signedImageUrl.bind(this);

    // aws-sdk doesn't play well with webpack, so we need to import the build distro
    // and reference the global window.AWS
    let client = new window.AWS.S3({
      accessKeyId: this.props.clientData.s3.accessKeyId,
      secretAccessKey: this.props.clientData.s3.secretAccessKey,
      params: {
        Bucket: this.props.clientData.s3.bucket
      }
    });

    this.state = {
      s3Client: client,
      images: this.props.avalanche.images,
    }
  }

  signedImageUrl(extId, filename) {
    return this.state.s3Client.getSignedUrl('getObject', { Key: 'avalanches/' + extId + '/images/' + filename });
  }

  render() {
    const { classes, clientData, avalanche } = this.props;

    console.info(`images are ${JSON.stringify(this.state.images)}`);

    return (
      <div>
        <div className={classes.instructions} dangerouslySetInnerHTML={{__html: clientData.help.avyReportImagesInstr}} />

      </div>
    );
  }
}

ReportImages.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
};

export default withStyles(styles)(ReportImages);