import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import ImageGrid from "./ImageGrid";
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
import {checkStatusAndParseJson} from "../../Util";

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
});

const ReportImages = props => {
  let { classes, clientData, avalanche, editKey, csrfToken } = props;

  let fileInputRef = React.createRef();

  const uploadImages = (files) => {
    console.info(`uploading ${files}`);

    let formData  = new FormData();

    for (let i = 0; i < files.length; i++) {
      let file = files[i];
      if (file instanceof File) {
        console.info(`appending ${file.name} to formData`);
        formData.append(file.name, file)
      }
    }

    fetch(`/api/avalanche/${avalanche.extId}/images?edit=${editKey}&csrfToken=${csrfToken}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'multipart/form-data',
        'Accept': 'application/json',
      },
      body: formData,
    })
    .then(response => {
      return checkStatusAndParseJson(response)
    })
    .then(data => {
      console.info(`Server response is ${JSON.stringify(data)}`);
    })
    .catch(error => console.error(`ERROR uploading images for avalanche ${avalanche.extId}. Error is: ${error}`))
  };

  return (
    <Table className={classes.table}>
      <TableBody>
        <TableRow>
          <TableCell className={classes.updateButtonCell}>
            <Button
              variant="contained"
              size="small"
              className={classes.uploadButton}
              onClick={() => fileInputRef.current.click()}
            >
              <AddIcon className={classes.addIcon} />
              Upload Images
            </Button>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              multiple
              style={{visibility: 'hidden', width: 0, height: 0 }}
              onChange={(e) => uploadImages(e.target.files)}
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
            <ImageGrid
              s3config={clientData.s3}
              avalanche={avalanche}
              editKey={editKey}
              csrfToken={csrfToken}
            />
          </TableCell>
        </TableRow>
      </TableBody>
    </Table>
  );
};

ReportImages.propTypes = {
  classes: PropTypes.object.isRequired,
  clientData: PropTypes.object.isRequired,
  avalanche: PropTypes.object.isRequired,
};

export default withStyles(styles)(ReportImages);