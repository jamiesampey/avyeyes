import React from 'react';
import AWS from 'aws-sdk/dist/aws-sdk';
import { DragDropContextProvider } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from "prop-types";
import {withStyles} from "@material-ui/core";
import Dialog from "@material-ui/core/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent";
import DialogActions from "@material-ui/core/DialogActions";
import Typography from "@material-ui/core/Typography";
import TextField from "@material-ui/core/TextField";
import Button from "@material-ui/core/Button";
import ImageGridCell from "./ImageGridCell";
import DraggableImageTile from "./DraggableImage";

import {checkStatus, getCSRFTokenFromCookie, getRequestParam} from "../../Util";

const styles = theme => ({
  imageGrid: {
    width: '100%',
    display: 'flex',
    flexWrap: 'wrap',
    paddingBottom: 20,
  },
  captionDialogPaper: {
    width: 650,
  },
  captionDialogContent: {
    padding: '12px 24px',
  },
});

const MaxImages = 20;

class ImagesGrid extends React.Component {

  constructor(props) {
    super(props);

    this.handleImageMove = this.handleImageMove.bind(this);
    this.handleCaptionChange = this.handleCaptionChange.bind(this);
    this.handleImageDelete = this.handleImageDelete.bind(this);
    this.refreshImageGrid = this.refreshImageGrid.bind(this);

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
      captionImage: null,
      deleteImage: null,
    };
  }

  componentDidMount() {
    this.setState({
      images: this.props.avalanche.images.map(image => {
        return {
          filename: image.filename,
          caption: image.caption,
        }
      }),
    }, this.refreshImageGrid);
  }

  handleImageMove(cellIndex, movedImage) {
    let { avalanche, editKey, csrfToken } = this.props;
    let { images } = this.state;

    let prevImageIndex = images.findIndex(image => image.filename === movedImage.filename);
    images.splice(prevImageIndex, 1);
    images.splice(cellIndex, 0, movedImage);

    fetch(`/api/avalanche/${avalanche.extId}/images?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ order: images.map(image => image.filename) }),
    })
    .then(response => {
      checkStatus(response);
      this.setState({images: images}, this.refreshImageGrid);
    })
    .catch(error => console.error(`ERROR updating image order for avalanche ${avalanche.extId}. Error is: ${error}`))
  }

  handleCaptionChange() {
    let { avalanche, editKey, csrfToken } = this.props;
    let { captionImage } = this.state;

    fetch(`/api/avalanche/${avalanche.extId}/images/${captionImage.filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ caption: captionImage.caption }),
    })
    .then(response => checkStatus(response))
    .catch(error => console.error(`ERROR updating caption for image ${avalanche.extId}/${captionImage.filename}. Error is: ${error}`))
    .finally(() => this.setState({ captionImage: null }))
  }

  handleImageDelete() {
    let { avalanche, editKey, csrfToken } = this.props;
    let { images, deleteImage } = this.state;

    let prevImageIndex = images.findIndex(image => image.filename === deleteImage.filename);
    images.splice(prevImageIndex, 1);

    fetch(`/api/avalanche/${avalanche.extId}/images/${deleteImage.filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'DELETE',
    })
    .then(response => {
      checkStatus(response);
      this.setState({images: images}, this.refreshImageGrid);
    })
    .catch(error => console.error(`ERROR deleting image ${avalanche.extId}/${deleteImage.filename}. Error is: ${error}`))
    .finally(() => this.setState({ deleteImage: null }))
  }

  refreshImageGrid() {
    let { classes, avalanche } = this.props;
    let { s3Client, images } = this.state;

    let imageGridCells = images.map((image, index) =>
      <ImageGridCell key={index} index={index}
                     onImageDrop={this.handleImageMove}
                     onDelete={() => this.setState({ deleteImage: image })}
                     onCaptionChange={() => this.setState({ captionImage: image })}
      >
        <DraggableImageTile
          image={image}
          imageUrl={s3Client.getSignedUrl('getObject', { Key: 'avalanches/' + avalanche.extId + '/images/' + image.filename })}
        />
      </ImageGridCell>
    );

    for (let i = images.length; i < MaxImages; i++) {
      imageGridCells.push(<ImageGridCell key={i} index={i} />)
    }

    this.setState({
      imageGrid: <div className={classes.imageGrid}>{imageGridCells}</div>,
    });
  }

  render() {
    let { classes } = this.props;
    let { imageGrid, captionImage, deleteImage } = this.state;

    return (
      <div>
        <DragDropContextProvider backend={HTML5Backend}>
          { imageGrid }
        </DragDropContextProvider>

        <Dialog
          open={Boolean(captionImage)}
          disableBackdropClick
          disableEscapeKeyDown
          maxWidth={false}
          classes={{paper: classes.captionDialogPaper}}
        >
          <DialogTitle>Image Caption</DialogTitle>
          <DialogContent className={classes.captionDialogContent}>
            <TextField
              fullWidth
              variant="outlined"
              label="Caption"
              value={Boolean(captionImage) ? captionImage.caption : ''}
              onChange={(event) => {
                captionImage.caption = event.target.value;
                this.setState({captionImage: captionImage});
              }}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => this.setState({ captionImage: null })} color="primary">
              Cancel
            </Button>
            <Button onClick={this.handleCaptionChange} color="primary">
              Ok
            </Button>
          </DialogActions>
        </Dialog>

        <Dialog
          open={Boolean(deleteImage)}
          disableBackdropClick
          disableEscapeKeyDown
          maxWidth="xs"
        >
          <DialogTitle>Delete Image</DialogTitle>
          <DialogContent>
            <Typography variant="body1" color="textPrimary">
              Are you sure you want to delete this image?
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => this.setState({ deleteImage: null })} color="primary">
              Cancel
            </Button>
            <Button onClick={this.handleImageDelete} color="primary">
              Delete
            </Button>
          </DialogActions>
        </Dialog>

      </div>
    )
  }
}

ImagesGrid.propTypes = {
  classes: PropTypes.object.isRequired,
  s3config: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
};

export default withStyles(styles)(ImagesGrid);