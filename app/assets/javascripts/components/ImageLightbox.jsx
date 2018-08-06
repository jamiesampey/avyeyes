import React from 'react';
import Lightbox from 'react-image-lightbox';

import 'react-image-lightbox/style.css';
import {withStyles} from "@material-ui/core/styles";

const styles = () => ({
  root: {
    zIndex: 9999,
    position: 'absolute',
    top: 0,
    left: 0,
    bottom: 0,
    right: 0,
  },
});

class ImageLightbox extends React.Component {
  constructor(props) {
    super(props);

    this.handleClose = this.handleClose.bind(this);

    this.state = {
      imageIndex: 0,
    };
  }

  handleClose() {
    this.setState({
      imageIndex: 0,
    });

    this.props.closeCallback();
  }

  render() {
    const { classes, images } = this.props;
    const { imageIndex } = this.state;

    return (
      <div className={classes.root}>
        <Lightbox
          mainSrc={images[imageIndex]}
          nextSrc={images[(imageIndex + 1) % images.length]}
          prevSrc={images[(imageIndex + images.length - 1) % images.length]}
          onCloseRequest={this.handleClose}
          onMovePrevRequest={() =>
            this.setState({
              imageIndex: (imageIndex + images.length - 1) % images.length,
            })
          }
          onMoveNextRequest={() =>
            this.setState({
              imageIndex: (imageIndex + 1) % images.length,
            })
          }
        />
      </div>
    )
  }
}

export default withStyles(styles)(ImageLightbox);