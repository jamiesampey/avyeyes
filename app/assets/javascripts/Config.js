import Cesium from 'cesium/Cesium';

const Config = {
  cesiumViewerOptions: {
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
  },
  facebookAppId: "541063359326610",
  cesiumAccessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3MjNkZTk5Yy1iMGViLTQ1M2YtOGFkZS1kZDIwNzNkMDE4YzUiLCJpZCI6MjE1NSwiaWF0IjoxNTMxNzk1MjM1fQ.A7eUkBj1ZtZHoOtyWJyGixTAXs7mXE6SVCA_lJod_-c',
};

export default Config;