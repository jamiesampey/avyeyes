import React from 'react';
import ReactDOM from "react-dom";
import Cesium from 'cesium/Cesium';
import SwipeableDrawer from '@material-ui/core/SwipeableDrawer';
import Config from './Config';
import Menu from './Menu';

import 'cesium/Widgets/widgets.css';
import '../stylesheets/AvyEyesClient.scss';

class AvyEyesClient extends React.Component {

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return false;
  }

  componentWillMount() {
    Cesium.Ion.defaultAccessToken = Config.cesiumAccessToken;
    this.setState({
      menuOpen: true,
    });
  }

  componentDidMount() {
    this.cesiumViewer = new Cesium.Viewer("cesiumContainer", Config.cesiumViewerOptions);
    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
  }

  toggleMenu() {
    let previousMenuState = this.setState.menuOpen;
    this.setState({
      menuOpen: !previousMenuState,
    });
  }

  render() {
    return (
      <div id="AvyEyes" className="fullScreen">
        <div id="cesiumContainer" className="fullScreen"/>
        <SwipeableDrawer
          anchor="left"
          open={this.state.menuOpen}
          onClose={this.toggleMenu}
          onOpen={this.toggleMenu}>
            <Menu/>
        </SwipeableDrawer>
      </div>
    )
  }
}

ReactDOM.render(<AvyEyesClient/>, document.getElementById('root'));