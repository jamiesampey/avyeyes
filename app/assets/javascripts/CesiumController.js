import Cesium from 'cesium/Cesium';

class CesiumController {

  constructor(cesiumViewer) {
    this.viewer = cesiumViewer
  }

  get viewer() {
    return this._viewer;
  }

  set viewer(cesiumViewer) {
    this._viewer = cesiumViewer;
  }

  addEntity(entity) {
    entity.createdBy = "AvyEyes";
    return this.viewer.entities.add(entity);
  }

  removeEntity(entity) {
    return this.viewer.entities.remove(entity);
  }

  flyTo(targetEntity, heading, pitch, range) {
    return this.viewer.flyTo(targetEntity, {
      duration: 4.0,
      offset: this.toHeadingPitchRange(heading, pitch, range)
    });
  }

  geolocateAndFlyTo() {
    let heading = 0.0;
    let pitch = -89.9; // work around -90 degree problem in flyToBoundingSphere
    let range = 1000000;

    let finishUp = (flyToEntity) => {
      this.removeEntity(flyToEntity);
      // TODO show clues
      // showZoomInClue().then(function() {
      //   showCesiumHelpClue();
      // })
    };

    let flyToDefaultView = () => {
      let defaultTarget = this.targetEntityFromCoords(-105.5, 39.0); // colorado
      this.flyTo(defaultTarget, heading, pitch, range).then(() => {
        finishUp(defaultTarget);
      });
    };

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        let geolocatedTarget = this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude);
        this.flyTo(geolocatedTarget, 0.0, pitch, range).then(() => {
          finishUp(geolocatedTarget);
        });
      }, flyToDefaultView, {timeout:8000, enableHighAccuracy:false});
    } else {
      flyToDefaultView();
    }
  }

  addAvalanches(avalancheArray) {
    let self = this;
    let viewer = this.viewer;

    let oldAvalancheIds = viewer.entities.values.map(function(entity) {
      return entity.id;
    });
    let newAvalancheIds = avalancheArray.map(function(a) {
      return a.extId;
    });

    // remove any old avalanches that do not exist in the new set
    oldAvalancheIds.filter((oldId) => {
      return newAvalancheIds.indexOf(oldId) < 0;
    }).forEach((oldIdToRemove) => {
      viewer.entities.removeById(oldIdToRemove);
    });

    // does the entity need to be updated? I.e. pin->path or path->pin?
    let updateEntity = (entity, avalanche) => {
      let noUpdateNeeded = (entity && entity.polygon && avalanche.coords) || (entity && entity.billboard && avalanche.location);
      return !noUpdateNeeded;
    };

    avalancheArray.forEach((a) => {
      let existingEntity = viewer.entities.getById(a.extId);

      if (updateEntity(existingEntity, a)) {
        self.removeEntity(existingEntity);
        self.addAvalanche(a);
        // TODO show the 'click on the red path' notify
        // if (a.coords) {
        //   showClickPathClue("Click on any red avalanche path for the details");
        //   this.clickPathClueShown = true;
        // }
      }
    });
  }

  addAvalanche(a) {
    let getEntity = () => {
      if (a.coords) {
        return {
          id: a.extId,
          name: a.title,
          polygon: {
            material: Cesium.Color.RED.withAlpha(0.4),
            hierarchy: Cesium.Cartesian3.fromDegreesArrayHeights(a.coords),
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND
          }
        };
      } else {
        return {
          id: a.extId,
          name: a.title,
          position: Cesium.Cartesian3.fromDegrees(a.location.longitude, a.location.latitude, a.location.altitude),
          billboard: { image: "/assets/images/poi-pin.png" }
        };
      }
    };

    this.addEntity(getEntity());
  }

  targetEntityFromCoords(lng, lat) {
    let alt = this.viewer.scene.globe.getHeight(Cesium.Cartographic.fromDegrees(lng, lat));

    return this.addEntity({
      position: Cesium.Cartesian3.fromDegrees(lng, lat, alt),
      billboard: {image: "/assets/images/flyto-pixel.png"}
    });
  }

  toHeadingPitchRange(heading, pitch, range) {
    return new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range);
  }
}

export default CesiumController;