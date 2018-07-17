import React from 'react';
import ReactDOM from "react-dom";
import Cesium from 'cesium/Cesium';

import 'cesium/Widgets/widgets.css';
import '../stylesheets/style.scss';

const cesiumViewerOptions = {
  sceneMode: Cesium.SceneMode.SCENE3D,
  imageryProvider: Cesium.createWorldImagery(),
  terrainProvider: Cesium.createWorldTerrain(),
  contextOptions: {
    webgl: { preserveDrawingBuffer: true }
  },
  animation: false,
  baseLayerPicker: false,
  fullscreenButton: true,
  geocoder: false,
  homeButton: false,
  infoBox: false,
  sceneModePicker: false,
  selectionIndicator: false,
  timeline: false,
  navigationHelpButton: true,
  navigationInstructionsInitiallyVisible: false,
  scene3DOnly: true,
  shadows: false,
  terrainShadows: false
};

class AvyEyesClient extends React.Component {

  constructor() {
    super();
    // console.info("Starting AvyEyes view. Social plugins loaded == " + socialMode);
    this.facebookAppId = "541063359326610";
    Cesium.Ion.defaultAccessToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3MjNkZTk5Yy1iMGViLTQ1M2YtOGFkZS1kZDIwNzNkMDE4YzUiLCJpZCI6MjE1NSwiaWF0IjoxNTMxNzk1MjM1fQ.A7eUkBj1ZtZHoOtyWJyGixTAXs7mXE6SVCA_lJod_-c';
  }

  componentDidMount() {
    this.cesiumViewer = new Cesium.Viewer(this.refs.cesiumContainer, cesiumViewerOptions);
    this.cesiumEventHandler = new Cesium.ScreenSpaceEventHandler(this.cesiumViewer.scene.canvas);
  }

  render() {
    return (
        <div ref="cesiumContainer" />
    )
  }
}


ReactDOM.render(<AvyEyesClient/>, document.getElementById('root'));