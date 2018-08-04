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
import Dialog from "@material-ui/core/Dialog";
import Button from "@material-ui/core/Button";

import {parseApiDateString, parseApiResponse} from "../Util";


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
  }
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
    fetch('/api/s3config')
      .then(response => {
        return parseApiResponse(response);
      })
      .then(data => {
        this.setState({ s3config: data.s3 });
      })
      .catch(error => {
        console.error(`Unable to retrieve s3 config from server. Error: ${error}`);
      });

    fetch('/api/dataCodes')
      .then(response => {
        return parseApiResponse(response);
      })
      .then(data => {
        this.setState({ dataCodes: data });
      })
      .catch(error => {
        console.error(`Unable to retrieve SWAG data codes from server. Error: ${error}`);
      });

    this.setState({
      expanded: false,
      rotatingImageIdx: 0,
    });
  }

  toggleExpanded() {
    this.setState(prevState => ({expanded: !prevState.expanded}));
  }

  constructImageUrl(avalanche, image) {
    return `//${this.state.s3config.bucket}.s3.amazonaws.com/avalanches/${avalanche.extId}/images/${image.filename}`;
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

    console.info(`data codes are: ${JSON.stringify(this.state.dataCodes)}`);

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
              <CardContent>
                <Typography variant="body2">
                  Full Comments:
                </Typography>
                <Typography paragraph>
                  {avalanche.comments}
                </Typography>
                <Typography paragraph>
                  <i>Submitter: {avalanche.submitterExp}</i>
                </Typography>
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
