import Cesium from 'cesium/Cesium';

class CesiumController {

  constructor(cesiumViewer) {
    this.cesiumViewer = cesiumViewer
  }

  get viewer() {
    return this.cesiumViewer;
  }

  addEntity(entity) {
    entity.createdBy = "AvyEyes";
    return this.cesiumViewer.entities.add(entity);
  }

  targetEntityFromCoords(lng, lat) {
    let alt = this.cesiumViewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));

    return this.addEntity({
      position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
      billboard: {image: "/assets/images/flyto-pixel.png"}
    });
  }

  flyTo(targetEntity, heading, pitch, range) {
    return this.cesiumViewer.flyTo(targetEntity, {
      duration: 4.0,
      offset: this.toHeadingPitchRange(heading, pitch, range)
    });
  }

  toHeadingPitchRange(heading, pitch, range) {
    return new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range);
  }
}

export default CesiumController;