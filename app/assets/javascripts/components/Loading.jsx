import React from 'react';


const classes = {
  loading: {
    opacity: 1,
    position: 'absolute',
    left: 0,
    top: 0,
    bottom: 0,
    right: 0,
    zIndex: 20,
    backgroundColor: 'black',
  },
  loadingFadeOut: {
    opacity: 0,
    transition: 'opacity 0.5s 0.5s',
  },
  loadingSplash: {
    position: 'absolute',
    margin: 'auto',
    height: 200,
    top: '-100px; left: 0; bottom: 0; right: 0;',
    textAlign: 'center',
    backgroundImage: 'url("../images/spinner-init-loading.gif")',
    backgroundRepeat: 'no-repeat',
    backgroundPosition: 'center',
  },
  loadingSplashTitle: {
    width: 170,
    paddingTop: 75,
  },
};

const Loading = props => {
  return (
    <div className={props.loading ? 'loading' : 'loadingFadeOut'}>
      <div className={classes.loadingSplash}>
        <img className={classes.loadingSplashTitle} src="@routes.Assets.versioned('images/title.png')"/>
      </div>
    </div>
  );
};

export default Loading;