import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import classnames from 'classnames';
import Card from '@material-ui/core/Card';
import CardHeader from '@material-ui/core/CardHeader';
import CardMedia from '@material-ui/core/CardMedia';
import CardContent from '@material-ui/core/CardContent';
import CardActions from '@material-ui/core/CardActions';
import Collapse from '@material-ui/core/Collapse';
import IconButton from '@material-ui/core/IconButton';
import Typography from '@material-ui/core/Typography';
import CloseIcon from "@material-ui/icons/Close";
import ShareIcon from '@material-ui/icons/Share';
import ImagesIcon from '@material-ui/icons/Collections';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import LandscapeIcon from '@material-ui/icons/Landscape';
import ViewListIcon from "@material-ui/icons/ViewList";
import CommentsIcon from "@material-ui/icons/InsertComment";
import PersonIcon from "@material-ui/icons/Person";
import Dialog from "@material-ui/core/Dialog";
import Button from "@material-ui/core/Button";
import List from "@material-ui/core/List";
import ListItem from "@material-ui/core/ListItem";
import ListItemText from "@material-ui/core/ListItemText";
import ListItemIcon from "@material-ui/core/ListItemIcon";
import Divider from "@material-ui/core/Divider";
import Table from "@material-ui/core/Table";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import TableBody from "@material-ui/core/TableBody";

import {parseApiDateString, labelForDataCode, compositeLabelForDataCode, metersToFeet} from "../Util";

const styles = theme => ({
  card: {
    minWidth: 450,
    maxWidth: 600,
  },
  media: {
    height: 0,
    paddingTop: '56.25%', // 16:9
  },
  introTextContent: {
    marginBottom: 0,
    paddingBottom: 0,
  },
  actions: {
    display: 'flex',
    paddingBottom: 0,
  },
  moreInfoButton: {
    marginLeft: 'auto',
    [theme.breakpoints.up('sm')]: {
      marginRight: -8,
    },
  },
  expand: {
    transform: 'rotate(0deg)',
    transition: theme.transitions.create('transform', {
      duration: theme.transitions.duration.shortest,
    }),
  },
  expandedCardContent: {
    paddingTop: 10,
    paddingBottom: 0,
  },
  expandedCardList: {
    '& li': {
      paddingBottom: 0,
    },
    '& div': {
      paddingLeft: 5,
      paddingRight: 0,
    },
    '& p': {
      marginBottom: 0,
    },
    '& svg': {
      marginBottom: 'auto',
    },
  },
  swagTable: {
    marginLeft: 5,
    '& tr': {
      height: 30,
    }
  },
});

const imageRotateInverval = 5000;
const introWordCount = 50;

class AvyCard extends React.Component {

  constructor() {
    super();
    this.toggleExpanded = this.toggleExpanded.bind(this);
    this.handleClose = this.handleClose.bind(this);
    this.constructImageUrl = this.constructImageUrl.bind(this);
    this.startCardMediaRotation = this.startCardMediaRotation.bind(this);
  }

  componentWillMount() {
    this.setState({
      expanded: false,
      rotatingImageIdx: 0,
    });
  }

  toggleExpanded() {
    this.setState(prevState => ({expanded: !prevState.expanded}));
  }

  constructImageUrl(avalanche, image) {
    return `//${this.props.clientData.s3.bucket}.s3.amazonaws.com/avalanches/${avalanche.extId}/images/${image.filename}`;
  }

  startCardMediaRotation(avalanche) {
    if (!this.state.rotatingCardMedia && avalanche.images.length > 1) {
      let imageUrls = avalanche.images.map(image => { return this.constructImageUrl(avalanche, image); });

      imageUrls.forEach(url => {
        let img = new Image();
        img.src = url;
        img.addEventListener("load", function() {
          // remove portrait-oriented images from the rotating CardMedia
          if (this.naturalHeight > this.naturalWidth) {
            imageUrls.splice(imageUrls.indexOf(url), 1);
          }
        });
      });

      this.cardMediaInterval = setInterval(() => {
        let newIndex = ++this.state.rotatingImageIdx % imageUrls.length;
//        console.info(`changing to image ${newIndex}: ${landscapeImageUrls[newIndex]}`);

        this.setState({
          rotatingImageIdx: newIndex,
          rotatingCardMedia: <CardMedia className={this.props.classes.media} image={imageUrls[newIndex]} />,
        });
      }, imageRotateInverval);
    }
  }

  static introText(text) {
    if (!text || text.length === 0) return <i>no description</i>;
    let words = text.split(' ');
    return words.length <= introWordCount ? text : `${words.slice(0, introWordCount).join(' ')}...`;
  }

  handleClose() {
    if (this.cardMediaInterval) clearInterval(this.cardMediaInterval);
    this.setState({
      expanded: false,
      rotatingImageIdx: 0,
      rotatingCardMedia: null,
    });
    this.props.closeCallback();
  }

  render() {
    const {classes, avalanche, setCursorStyle} = this.props;
    if (avalanche === null) return null;

    setCursorStyle("default");

    //console.info(`Showing card for avalanche:\n${JSON.stringify(avalanche)}`);
    // console.info(`clientData is: ${JSON.stringify(this.props.clientData)}`);

    const {rotatingCardMedia} = this.state;
    this.startCardMediaRotation(avalanche);
    let currentCardMedia = null;
    if (rotatingCardMedia) {
      currentCardMedia = rotatingCardMedia;
    } else if (avalanche.images.length > 0 && !rotatingCardMedia) {
      // Single image, or image rotation hasn't yet started
      currentCardMedia = <CardMedia className={classes.media} image={this.constructImageUrl(avalanche, avalanche.images[0])} />;
    }

    return (
      <Dialog
        className={classes.dialog}
        open={avalanche !== null}
        onClose={this.handleClose}
        aria-labelledby="form-dialog-title"
      >
        <div>
          <Card className={classes.card}>
            <CardHeader
              action={
                <IconButton onClick={this.handleClose}>
                  <CloseIcon/>
                </IconButton>
              }
              title={avalanche.areaName}
              subheader={parseApiDateString(avalanche.date)}
            />
            {currentCardMedia}
            <CardContent className={classes.introTextContent}>
              <Typography paragraph>
                {AvyCard.introText(avalanche.comments)}
              </Typography>
            </CardContent>
            <CardActions className={classes.actions} disableActionSpacing>
              <IconButton title="Share">
                <ShareIcon/>
              </IconButton>
              <IconButton title="Images">
                <ImagesIcon/>
              </IconButton>
              <Button size="small" color="primary" className={classes.moreInfoButton} onClick={this.toggleExpanded}>
                {this.state.expanded ? "Less Info" : "More Info"}
                <ExpandMoreIcon
                  className={classnames(classes.expand, { [classes.expandedIcon]: this.state.expanded, })}
                />
              </Button>
            </CardActions>
            <Collapse in={this.state.expanded} timeout="auto" unmountOnExit>
              <Divider/>
              <CardContent className={classes.expandedCardContent}>
                <List disablePadding className={classes.expandedCardList}>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <LandscapeIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {avalanche.slope.angle}&deg; {avalanche.slope.aspect} aspect at {avalanche.slope.elevation} meters ({metersToFeet(avalanche.slope.elevation)} ft)
                      </Typography>
                    </ListItemText>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <ViewListIcon/>
                    </ListItemIcon>
                    <Table className={classes.swagTable}>
                      <TableBody>
                        <TableRow>
                          <TableCell padding="none">Type</TableCell>
                          <TableCell padding="none">{compositeLabelForDataCode(this.props.clientData.codes.avalancheType, avalanche.classification.avyType)}</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell padding="none">Trigger</TableCell>
                          <TableCell padding="none">{compositeLabelForDataCode(this.props.clientData.codes.avalancheTrigger, avalanche.classification.trigger)}</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell padding="none">Modifier</TableCell>
                          <TableCell padding="none">{compositeLabelForDataCode(this.props.clientData.codes.avalancheTriggerModifier, avalanche.classification.triggerModifier)}</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell padding="none">Interface</TableCell>
                          <TableCell padding="none">{compositeLabelForDataCode(this.props.clientData.codes.avalancheInterface, avalanche.classification.interface)}</TableCell>
                        </TableRow>
                        <TableRow>
                          <TableCell padding="none">Size</TableCell>
                          <TableCell padding="none">R{avalanche.classification.rSize} / D{avalanche.classification.dSize}</TableCell>
                        </TableRow>
                      </TableBody>
                    </Table>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <CommentsIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        {avalanche.comments}
                      </Typography>
                    </ListItemText>
                  </ListItem>
                  <ListItem disableGutters>
                    <ListItemIcon>
                      <PersonIcon/>
                    </ListItemIcon>
                    <ListItemText disableTypography>
                      <Typography paragraph>
                        <i>Submitter: {labelForDataCode(this.props.clientData.codes.experienceLevel, avalanche.submitterExp)} </i>
                      </Typography>
                    </ListItemText>
                  </ListItem>
                </List>
              </CardContent>
            </Collapse>
          </Card>
        </div>
      </Dialog>
    );
  }
}

AvyCard.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyCard);
