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
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Dialog from "@material-ui/core/Dialog";
import Button from "@material-ui/core/Button";

import {parseApiDateString, parseApiResponse} from "../Util";


const styles = theme => ({
  card: {
    maxWidth: 600,
  },
  media: {
    height: 0,
    paddingTop: '56.25%', // 16:9
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

  handleClose() {
    if (this.cardMediaInterval) clearInterval(this.cardMediaInterval);
    this.setState({
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
            <CardContent>
              <Typography component="p">
                This impressive paella is a perfect party dish and a fun meal to cook together with
                your guests. Add 1 cup of frozen peas along with the mussels, if you like.
              </Typography>
            </CardContent>
            <CardActions className={classes.actions} disableActionSpacing>
              <Button size="small" color="primary" className={classes.shareButton}>
                Share
              </Button>
              <Button size="small" color="primary" className={classes.moreInfoButton} onClick={this.toggleExpanded}>
                {this.state.expanded ? "Less Info" : "More Info"}
                <ExpandMoreIcon
                  className={classnames(classes.expand, { [classes.expandedIcon]: this.state.expanded, })}
                />
              </Button>
            </CardActions>
            <Collapse in={this.state.expanded} timeout="auto" unmountOnExit>
              <CardContent>
                <Typography paragraph variant="body2">
                  Method:
                </Typography>
                <Typography paragraph>
                  Heat 1/2 cup of the broth in a pot until simmering, add saffron and set aside for 10
                  minutes.
                </Typography>
                <Typography paragraph>
                  Heat oil in a (14- to 16-inch) paella pan or a large, deep skillet over medium-high
                  heat. Add chicken, shrimp and chorizo, and cook, stirring occasionally until lightly
                  browned, 6 to 8 minutes. Transfer shrimp to a large plate and set aside, leaving
                  chicken and chorizo in the pan. Add pimentón, bay leaves, garlic, tomatoes, onion,
                  salt and pepper, and cook, stirring often until thickened and fragrant, about 10
                  minutes. Add saffron broth and remaining 4 1/2 cups chicken broth; bring to a boil.
                </Typography>
                <Typography paragraph>
                  Add rice and stir very gently to distribute. Top with artichokes and peppers, and
                  cook without stirring, until most of the liquid is absorbed, 15 to 18 minutes.
                  Reduce heat to medium-low, add reserved shrimp and mussels, tucking them down into
                  the rice, and cook again without stirring, until mussels have opened and rice is
                  just tender, 5 to 7 minutes more. (Discard any mussels that don’t open.)
                </Typography>
                <Typography>
                  Set aside off of the heat to let rest for 10 minutes, and then serve.
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
