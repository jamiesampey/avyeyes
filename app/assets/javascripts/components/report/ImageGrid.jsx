import React from 'react';
import { DragDropContextProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from "prop-types";
import {withStyles} from "@material-ui/core";
import ImageGridCell from "./ImageGridCell";
import DraggableImageTile from "./DraggableImage";
import AWS from 'aws-sdk/dist/aws-sdk';
import {checkStatus, getCSRFTokenFromCookie, getRequestParam} from "../../Util";

const styles = theme => ({
  root: {
    width: '100%',
    display: 'flex',
    flexWrap: 'wrap',
    paddingBottom: 20,
  },
});

const MaxImages = 20;

class ImagesGrid extends React.Component {

  constructor(props) {
    super(props);

    this.handleImageMove = this.handleImageMove.bind(this);
    this.handleCaptionChange = this.handleCaptionChange.bind(this);
    this.handleImageDelete = this.handleImageDelete.bind(this);

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
      editKey: getRequestParam("edit"),
      csrfToken: getCSRFTokenFromCookie(),
      extId: this.props.avalanche.extId,
      images: this.props.avalanche.images.map(image => {
        return {
          filename: image.filename,
          caption: image.caption,
        }
      }),
    };
  }

  handleImageMove(cellIndex, movedImage) {
    let { extId, images, editKey, csrfToken } = this.state;

    console.info(`moved image is ${JSON.stringify(movedImage)}`);

    let prevImageIndex = images.findIndex(image => image.filename === movedImage.filename);

    images.splice(prevImageIndex, 1);
    images.splice(cellIndex, 0, movedImage);

    this.setState({ images: images });

    fetch(`/api/avalanche/${extId}/images?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ order: images.map(image => image.filename) }),
    })
    .then(response => checkStatus(response))
    .catch(error => console.error(`ERROR updating image order for avalanche ${extId}. Error is: ${error}`))
  }

  handleCaptionChange(index, newCaption) {
    let { extId, images, editKey, csrfToken } = this.state;

    console.info(`changing caption for ${extId}/${images[index].filename}`);

    fetch(`/api/avalanche/${extId}/images/${images[index].filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ caption: newCaption }),
    })
      .then(response => checkStatus(response))
      .catch(error => console.error(`ERROR updating caption for image ${extId}/${images[index].filename}. Error is: ${error}`))
  }

  handleImageDelete(index) {
    let { extId, images, editKey, csrfToken } = this.state;

    console.info(`deleting ${extId}/${images[index].filename}`);

    fetch(`/api/avalanche/${extId}/images/${filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'DELETE',
    })
      .then(response => checkStatus(response))
      .catch(error => console.error(`ERROR deleting image ${extId}/${images[index].filename}. Error is: ${error}`))
  }

  render() {
    let { classes } = this.props;
    let { s3Client, images, extId } = this.state;

    let imageGridCells = images.map((image, index) =>
      <ImageGridCell key={index} order={index}
        onImageDrop={this.handleImageMove}
        onCaptionChange={this.handleCaptionChange}
        onDelete={this.handleImageDelete}
      >
        <DraggableImageTile
          imageUrl={s3Client.getSignedUrl('getObject', { Key: 'avalanches/' + extId + '/images/' + image.filename })}
          filename={image.filename}
          caption={image.caption}
        />
      </ImageGridCell>
    );

    for (let i = images.length; i < MaxImages; i++) {
      imageGridCells.push(<ImageGridCell key={i} order={i} />)
    }

    return (
      <DragDropContextProvider backend={HTML5Backend}>
        <div className={classes.root}>
          { imageGridCells }
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