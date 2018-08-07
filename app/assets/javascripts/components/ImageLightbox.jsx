import React from 'react';
import Lightbox from 'react-image-lightbox';

import 'react-image-lightbox/style.css';

export default class ImageLightbox extends React.Component {
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
    const { images } = this.props;
    const { imageIndex } = this.state;

    return (
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
    )
  }
}
