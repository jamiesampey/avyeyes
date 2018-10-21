import React from 'react';
import PropTypes from 'prop-types';
import {withStyles} from '@material-ui/core/styles';
import classNames from 'classnames';
import ImageLightbox from './ImageLightbox';
import SocialMenu from "./SocialMenu";
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
import CloudIcon from '@material-ui/icons/FilterDrama';
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
import Tooltip from "@material-ui/core/Tooltip";

import {
  notSpecified,
  parseApiDateString,
  labelForDataCode,
  compositeLabelForDataCode,
  metersToFeet,
  constructImageUrl
} from "../Util";

const styles = theme => ({
  dialog: {
    zIndex: 100,
  },
  card: {
    minWidth: 450,
    maxWidth: 600,
  },
  media: {
    height: 0,
    paddingTop: '56.25%', // 16:9
    '&:hover': {
      cursor: 'pointer',
    },
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
  expandedIcon: {
    transform: 'rotate(180deg)',
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
  linkCopySnackbarRoot: {
    backgroundColor: 'red',
  },
});

const imageRotateInverval = 5000;
const introWordCount = 50;

class AvyCard extends React.Component {

  constructor(props) {
    super(props);

    this.toggleExpanded = this.toggleExpanded.bind(this);
    this.handleClose = this.handleClose.bind(this);
    this.weatherDesc = this.weatherDesc.bind(this);
    this.renderCardMedia = this.renderCardMedia.bind(this);
    this.renderLightbox = this.renderLightbox.bind(this);

    this.state = {
      expanded: false,
      rotatingImageIdx: 0,
      rotatingCardMedia: null,
      cardMediaInterval: null,
      lightboxOpen: false,
    };
  }

  componentDidMount() {
    this.setState({
      expanded: false,
      rotatingImageIdx: 0,
      rotatingCardMedia: null,
      cardMediaInterval: null,
      socialMenuAnchor: null,
      lightboxOpen: false,
    });

    let { avalanche } = this.props;

    if (avalanche.images.length > 1) {
      let imageUrls = avalanche.images.map(image => { return constructImageUrl(this.props.clientData.s3.bucket, avalanche, image); });

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

      this.setState({
        cardMediaInterval: setInterval(() => {
          if (!this.state.lightboxOpen) {
            let newIndex = ++this.state.rotatingImageIdx % imageUrls.length;
            //console.info(`changing to image ${newIndex}: ${imageUrls[newIndex]}`);

            this.setState({
              rotatingImageIdx: newIndex,
              rotatingCardMedia: this.renderCardMedia(avalanche, imageUrls[newIndex]),
            });
          }
        }, imageRotateInverval)
      })
    }
  }

  static introText(text) {
    if (!text || text.length === 0) return <i>no description</i>;
    let words = text.split(' ');
    return words.length <= introWordCount ? text : `${words.slice(0, introWordCount).join(' ')}...`;
  }

  weatherDesc(weather) {
    let { windSpeed, direction } = this.props.clientData.codes;
    let desc = '';
    const Empty = 'empty';

    if (weather.recentSnow !== -1) {
      desc += `${weather.recentSnow} cm of new snow`;
    }

    if (weather.recentWindSpeed !== Empty) {
      if (desc.length > 0) desc += ' and ';
      desc += `"${labelForDataCode(windSpeed, weather.recentWindSpeed)}" winds`;
      if (weather.recentWindDirection !== Empty) desc += ` from the ${labelForDataCode(direction, weather.recentWindDirection)}`;
    }

    return desc.length > 0 ? desc : notSpecified;
  }

  toggleExpanded() {
    this.setState(prevState => ({expanded: !prevState.expanded}));
  }

  handleClose() {
    if (this.state.cardMediaInterval) {
      clearInterval(this.state.cardMediaInterval);
    }

    this.props.closeCallback();
  }

  renderCardMedia(avalanche, imageUrl) {
    return (
      <CardMedia
        className={this.props.classes.media}
        image={imageUrl}
        onClick={() => { this.setState({ lightboxOpen: true }) }}
      />
    );
  }

  renderLightbox(avalanche) {
    return (
      <ImageLightbox
        avalanche={avalanche}
        s3Bucket={this.props.clientData.s3.bucket}
        closeCallback={() => { this.setState({ lightboxOpen: false }) }}
      />
    )
  }

  render() {
    const { classes, clientData, avalanche, setCursorStyle } = this.props;
    const { rotatingCardMedia, socialMenuAnchor } = this.state;

    //console.info(`Showing card for avalanche:\n${JSON.stringify(avalanche)}`);
    //console.info(`clientData is: ${JSON.stringify(this.props.clientData)}`);

    let currentCardMedia = null;
    if (rotatingCardMedia) {
      currentCardMedia = rotatingCardMedia;
    } else if (avalanche.images.length > 0 && !rotatingCardMedia) {
      // Single image, or image rotation hasn't yet started
      currentCardMedia = this.renderCardMedia(avalanche, constructImageUrl(this.props.clientData.s3.bucket, avalanche, avalanche.images[0]));
    }

    return (
      <div>
        {this.state.lightboxOpen && this.renderLightbox(avalanche)}
        <SocialMenu
          avalanche={avalanche}
          clientData={clientData}
          anchorEl={socialMenuAnchor}
          closeCallback={() => { this.setState({ socialMenuAnchor: null }) }}
        />
        <Dialog
          className={classes.dialog}
          open={avalanche !== null}
          onClose={this.handleClose}
          disableEnforceFocus={true} // needed to enable arrow keys on lightbox
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
                <IconButton title="Share" onClick={event => { this.setState({ socialMenuAnchor: event.currentTarget }) }}>
                  <ShareIcon/>
                </IconButton>
                <IconButton title="Images" onClick={() => { this.setState({ lightboxOpen: true }) }}>
                  <ImagesIcon/>
                </IconButton>
                <Button size="small" color="primary" className={classes.moreInfoButton} onClick={this.toggleExpanded}>
                  {this.state.expanded ? "Less Info" : "More Info"}
                  <ExpandMoreIcon
                    className={classNames(classes.expand, { [classes.expandedIcon]: this.state.expanded, })}
                  />
                </Button>
              </CardActions>
              <Collapse in={this.state.expanded} timeout="auto" unmountOnExit>
                <Divider/>
                <CardContent className={classes.expandedCardContent}>
                  <List disablePadding className={classes.expandedCardList}>
                    <ListItem disableGutters>
                      <Tooltip placement="top-start" title={clientData.tooltips.avyCardSlope}>
                        <ListItemIcon>
                            <LandscapeIcon/>
                        </ListItemIcon>
                      </Tooltip>
                      <ListItemText disableTypography>
                        <Typography paragraph>
                          {avalanche.slope.angle}&deg; {avalanche.slope.aspect} aspect at {avalanche.slope.elevation.toLocaleString()} meters ({metersToFeet(avalanche.slope.elevation).toLocaleString()} ft)
                        </Typography>
                      </ListItemText>
                    </ListItem>
                    <ListItem disableGutters>
                      <Tooltip placement="top-start" title={clientData.tooltips.avyCardSWAG}>
                        <ListItemIcon>
                          <ViewListIcon/>
                        </ListItemIcon>
                      </Tooltip>
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
                      <Tooltip placement="top-start" title={clientData.tooltips.avyCardWeather}>
                        <ListItemIcon>
                          <CloudIcon/>
                        </ListItemIcon>
                      </Tooltip>
                      <ListItemText disableTypography>
                        <Typography paragraph>
                          {this.weatherDesc(avalanche.weather)}
                        </Typography>
                      </ListItemText>
                    </ListItem>
                    <ListItem disableGutters>
                      <Tooltip placement="top-start" title={clientData.tooltips.avyCardComments}>
                        <ListItemIcon>
                          <CommentsIcon/>
                        </ListItemIcon>
                      </Tooltip>
                      <ListItemText disableTypography>
                        <Typography paragraph>
                          {avalanche.comments}
                        </Typography>
                      </ListItemText>
                    </ListItem>
                    <ListItem disableGutters>
                      <Tooltip placement="top-start" title={clientData.tooltips.avyCardSubmitter}>
                        <ListItemIcon>
                          <PersonIcon/>
                        </ListItemIcon>
                      </Tooltip>
                      <ListItemText disableTypography>
                        <Typography paragraph>
                          <i>{labelForDataCode(this.props.clientData.codes.experienceLevel, avalanche.submitterExp)} </i>
                        </Typography>
                      </ListItemText>
                    </ListItem>
                  </List>
                </CardContent>
              </Collapse>
            </Card>
          </div>
        </Dialog>
      </div>
    );
  }
}

AvyCard.propTypes = {
  classes: PropTypes.object.isRequired,
  theme: PropTypes.object.isRequired,
};

export default withStyles(styles, { withTheme: true })(AvyCard);
