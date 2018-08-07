import React from 'react';
import Lightbox from 'react-image-lightbox';

import 'react-image-lightbox/style.css';

export default class ImageLightbox extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      imageIdx: 0,
    }
  }

  componentDidMount() {
    this.setState({
      imageIdx: 0,
    });
  }

  render() {
    const { images, closeCallback } = this.props;
    const { imageIdx } = this.state;

    return (
        <Lightbox
          mainSrc={images[imageIdx]}
          nextSrc={images[(imageIdx + 1) % images.length]}
          prevSrc={images[(imageIdx + images.length - 1) % images.length]}
          onCloseRequest={closeCallback}
          onMovePrevRequest={() =>
            this.setState({
              imageIdx: (imageIdx + images.length - 1) % images.length,
            })
          }
          onMoveNextRequest={() =>
            this.setState({
              imageIdx: (imageIdx + 1) % images.length,
            })
          }
        />
    )
  }
}
