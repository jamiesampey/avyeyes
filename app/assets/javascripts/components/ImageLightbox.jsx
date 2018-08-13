import React from 'react';
import Lightbox from 'react-image-lightbox';

import 'react-image-lightbox/style.css';

import { constructImageUrl } from '../Util';

export default class ImageLightbox extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      imageIdx: 0,
      images: [],
    }
  }

  componentDidMount() {
    let { avalanche, s3Bucket } = this.props;

    this.setState({
      imageIdx: 0,
      images: avalanche.images.map(imageObj => {
        imageObj.url = constructImageUrl(s3Bucket, avalanche, imageObj);
        return imageObj;
      }),
    });
  }

  render() {
    const { closeCallback } = this.props;
    const { images, imageIdx } = this.state;

    if (images.length === 0) return null;

    return (
        <Lightbox
          mainSrc={images[imageIdx].url}
          nextSrc={images[(imageIdx + 1) % images.length].url}
          prevSrc={images[(imageIdx + images.length - 1) % images.length].url}
          imageTitle={<span style={{fontSize: 16}}>Image {imageIdx + 1} of {images.length}</span>}
          imageCaption={images[imageIdx].caption}
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
