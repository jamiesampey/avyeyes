import React from 'react';
import { DragDropContextProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from "prop-types";
import {withStyles} from "@material-ui/core";
import ImageGridCell from "./ImageGridCell";
import DraggableImageTile from "./DraggableImage";
import AWS from 'aws-sdk/dist/aws-sdk';

const styles = theme => ({
  root: {
    width: '100%',
    height: '100%',
    display: 'flex',
    flexWrap: 'wrap',
  },
});

class ImagesGrid extends React.Component {

  constructor(props) {
    super(props);

    this.reorderImages = this.reorderImages.bind(this);

    // aws-sdk doesn't play well with webpack, so we need to import the build distro
    // and reference the global window.AWS
    let client = new window.AWS.S3({
      accessKeyId: this.props.s3config.accessKeyId,
      secretAccessKey: this.props.s3config.secretAccessKey,
      params: {
        Bucket: this.props.s3config.bucket
      }
    });

    this.state = {
      s3Client: client,
    };
  }

  componentDidMount() {
    this.populateImageCells();
  }

  populateImageCells() {
    let extId = this.props.avalanche.extId;
    let images = this.props.avalanche.images;
    let imageCells = [];
    for (let i = 0; i < 20; i++) {

      let cellContent = null;
      if (i < images.length) {
        cellContent =
          <DraggableImageTile
            imageUrl={this.signedImageUrl(extId, images[i].filename)}
            filename={images[i].filename}
            caption={images[i].caption}
          />
      }

      imageCells.push(
        <ImageGridCell key={i} order={i} onImageDrop={this.reorderImages}>
          {cellContent}
        </ImageGridCell>
      )
    }

    this.setState({ imageCells: imageCells });
  }

  signedImageUrl(extId, filename) {
    return this.state.s3Client.getSignedUrl('getObject', { Key: 'avalanches/' + extId + '/images/' + filename });
  }

  reorderImages(cellIndex, filename) {
    console.info(`image ${filename} dropped into ImageGridCell ${i}`);
    this.populateImageCells();
  }

  render() {
    let { classes } = this.props;

    return (
      <DragDropContextProvider backend={HTML5Backend}>
        <div className={classes.root}>
          { this.state.imageCells }
        </div>
      </DragDropContextProvider>
    )
  }
}

ImagesGrid.propTypes = {
  classes: PropTypes.object.isRequired,
  s3config: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
};

export default withStyles(styles)(ImagesGrid);