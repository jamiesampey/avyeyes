import React from 'react';
import AWS from 'aws-sdk/dist/aws-sdk';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import Button from "@material-ui/core/Button";
import AddIcon from '@material-ui/icons/Add';
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import List from "@material-ui/core/List/List";
import ListItem from "@material-ui/core/ListItem/ListItem";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import ListItemText from "@material-ui/core/ListItemText";
import Typography from "@material-ui/core/Typography";
import ImagesIcon from '@material-ui/icons/Collections';
import CaptionIcon from "@material-ui/icons/InsertComment";
import DeleteIcon from "@material-ui/icons/Delete";
import MoveIcon from "@material-ui/icons/OpenWith";
import {checkStatus, checkStatusAndParseJson, fetchAvalanche} from "../../Util";
import {DragDropContextProvider} from "react-dnd";
import HTML5Backend from "react-dnd-html5-backend";
import Dialog from "@material-ui/core/Dialog/Dialog";
import DialogTitle from "@material-ui/core/DialogTitle/DialogTitle";
import DialogContent from "@material-ui/core/DialogContent/DialogContent";
import TextField from "@material-ui/core/TextField/TextField";
import DialogActions from "@material-ui/core/DialogActions/DialogActions";
import ImageGridCell from "./ImageGridCell";
import DraggableImageTile from "./DraggableImage";

const styles = theme => ({
  table: {
    width: '100%',
    height: '100%',
    border: 0,
    '& tr td': {
      padding: 0,
      paddingRight: 0,
      border: 0,
    }
  },
  updateButtonCell: {
    maxWidth: 200,
    verticalAlign: 'top',
  },
  uploadButton: {
    color: 'white',
    background: 'crimson',
  },
  addIcon: {
    color: 'white',
    marginRight: theme.spacing.unit,
  },
  instructionsCell: {
    color: theme.palette.text.primary,
  },
  instructionsList: {
    '& li': {
      paddingTop: 0,
      paddingBottom: 4,
    },
    '& div': {
      paddingLeft: 0,
      paddingRight: 0,
    },
    '& p': {
      marginBottom: 0,
    },
    '& svg': {
      marginBottom: 'auto',
    },
  },
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

class ReportImages extends React.Component {

  constructor(props) {
    super(props);

    this.fileInputRef = React.createRef();

    this.uploadImages = this.uploadImages.bind(this);
    this.handleImageMove = this.handleImageMove.bind(this);
    this.handleCaptionChange = this.handleCaptionChange.bind(this);
    this.handleImageDelete = this.handleImageDelete.bind(this);
    this.refreshImageGrid = this.refreshImageGrid.bind(this);

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
      captionImage: null,
      deleteImage: null,
    };
  }

  componentDidMount() {
    this.refreshImageGrid();
  }

  uploadImages(files) {
    let { extId, setInfoMessage, editKey, csrfToken } = this.props;

    let formData  = new FormData();

    for (let i = 0; i < files.length; i++) {
      let file = files[i];
      if (file instanceof File) {
        formData.append("files", file)
      }
    }

    setInfoMessage('Image upload in progress. New images will appear in the grid when the upload is complete');

    fetch(`/api/avalanche/${extId}/images?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
      },
      body: formData,
    })
      .then(response => {
        return checkStatusAndParseJson(response)
      })
      .then(data => {
        this.refreshImageGrid();
      })
      .catch(error => console.error(`ERROR uploading images for avalanche ${extId}. Error is: ${error}`))
  };

  handleImageMove(cellIndex, movedImage) {
    let { extId, editKey, csrfToken } = this.props;
    let { imageOrder } = this.state;

    let prevImageIndex = imageOrder.findIndex(filename => filename === movedImage.filename);
    imageOrder.splice(prevImageIndex, 1);
    imageOrder.splice(cellIndex, 0, movedImage.filename);

    fetch(`/api/avalanche/${extId}/images?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ order: imageOrder }),
    })
      .then(response => {
        checkStatus(response);
        this.refreshImageGrid();
      })
      .catch(error => console.error(`ERROR updating image order for avalanche ${extId}. Error is: ${error}`))
  }

  handleCaptionChange() {
    let { extId, editKey, csrfToken } = this.props;
    let { captionImage } = this.state;

    fetch(`/api/avalanche/${extId}/images/${captionImage.filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ caption: captionImage.caption }),
    })
      .then(response => checkStatus(response))
      .catch(error => console.error(`ERROR updating caption for image ${extId}/${captionImage.filename}. Error is: ${error}`))
      .finally(() => this.setState({ captionImage: null }))
  }

  handleImageDelete() {
    let { extId, editKey, csrfToken } = this.props;
    let { deleteImage } = this.state;

    fetch(`/api/avalanche/${extId}/images/${deleteImage.filename}?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'DELETE',
    })
      .then(response => {
        checkStatus(response);
        this.refreshImageGrid();
      })
      .catch(error => console.error(`ERROR deleting image ${extId}/${deleteImage.filename}. Error is: ${error}`))
      .finally(() => this.setState({ deleteImage: null }))
  }

  refreshImageGrid() {
    let { classes, extId } = this.props;
    let { s3Client } = this.state;

    let createNewImageGrid = (images) => {
      let imageGridCells = images.map((image, index) =>
        <ImageGridCell key={index} index={index}
          onImageDrop={this.handleImageMove}
          onDelete={() => this.setState({deleteImage: image})}
          onCaptionChange={() => this.setState({captionImage: image})}
        >
          <DraggableImageTile
            image={image}
            imageUrl={s3Client.getSignedUrl('getObject', {Key: `avalanches/${extId}/images/${image.filename}`})}
          />
        </ImageGridCell>
      );

      for (let i = images.length; i < MaxImages; i++) {
        imageGridCells.push(<ImageGridCell key={i} index={i}/>)
      }

      this.setState({
        imageOrder: images.map(image => image.filename),
        imageGrid: <div className={classes.imageGrid}>{imageGridCells}</div>,
      });
    };

    fetchAvalanche(extId).then(data => {
      this.setState({ imageGrid: null }, () => createNewImageGrid(data.images));
    })
    .catch(error => {
      console.error(`Unable to fetch images for avalanche ${extId}. Error: ${error}`);
    });
  }

  render() {
    let {classes, clientData } = this.props;
    let {imageGrid, captionImage, deleteImage} = this.state;

    return (
      <div>
        <Table className={classes.table}>
          <TableBody>
            <TableRow>
              <TableCell className={classes.updateButtonCell}>
                <Button
                  variant="contained"
                  size="small"
                  className={classes.uploadButton}
                  onClick={() => this.fileInputRef.current.click()}
                >
                  <AddIcon className={classes.addIcon}/>
                  Upload Images
                </Button>
                <input
                  ref={this.fileInputRef}
                  type="file"
                  accept="image/*"
                  multiple
                  style={{visibility: 'hidden', width: 0, height: 0}}
                  onChange={(e) => this.uploadImages(e.target.files)}
                />
              </TableCell>
              <TableCell className={classes.instructionsCell} style={{paddingRight: 0}}>
                <List disablePadding className={classes.instructionsList}>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <ImagesIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {clientData.help.avyReportImagesInstr1}
                      </Typography>
                    </ListItemText>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <MoveIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {clientData.help.avyReportImagesInstr2}
                      </Typography>
                    </ListItemText>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <CaptionIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {clientData.help.avyReportImagesInstr3}
                      </Typography>
                    </ListItemText>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <DeleteIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {clientData.help.avyReportImagesInstr4}
                      </Typography>
                    </ListItemText>
                  </ListItem>
                </List>
              </TableCell>
            </TableRow>
            <TableRow>
              <TableCell colSpan={2} style={{paddingTop: 16, paddingRight: 0}}>
                <DragDropContextProvider backend={HTML5Backend}>
                  {imageGrid}
                </DragDropContextProvider>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>

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
            <Button onClick={() => this.setState({captionImage: null})} color="primary">
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
            <Button onClick={() => this.setState({deleteImage: null})} color="primary">
              Cancel
            </Button>
            <Button onClick={this.handleImageDelete} color="primary">
              Delete
            </Button>
          </DialogActions>
        </Dialog>

      </div>
    );
  }
}

ReportImages.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  extId: PropTypes.string.isRequired,
  editKey: PropTypes.string,
  csrfToken: PropTypes.string.isRequired,
};

export default withStyles(styles)(ReportImages);