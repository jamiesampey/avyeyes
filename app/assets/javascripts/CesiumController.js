import Cesium from 'cesium/Cesium';

class CesiumController {

  constructor(cesiumViewer) {
    this.viewer = cesiumViewer;
    this.CreatedByAvyEyes = "AvyEyes";
  }

  get viewer() {
    return this._viewer;
  }

  set viewer(cesiumViewer) {
    this._viewer = cesiumViewer;
  }

  addEntity(entity) {
    entity.createdBy = this.CreatedByAvyEyes;
    return this.viewer.entities.add(entity);
  }

  removeEntity(entity) {
    return this.viewer.entities.remove(entity);
  }

  removeAllEntities() {
    let entitiesToRemove = [];
    this.viewer.entities.values.forEach(entity => {
      if (entity.createdBy && entity.createdBy === this.CreatedByAvyEyes) entitiesToRemove.push(entity);
    });

    entitiesToRemove.forEach(entity => { this.removeEntity(entity); });
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

    let finishUp = flyToEntity => {
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
      navigator.geolocation.getCurrentPosition(pos => {
        let geolocatedTarget = this.targetEntityFromCoords(pos.coords.longitude, pos.coords.latitude);
        this.flyTo(geolocatedTarget, 0.0, pitch, range).then(() => {
          finishUp(geolocatedTarget);
        });
      }, flyToDefaultView, {timeout:8000, enableHighAccuracy:false});
    } else {
      flyToDefaultView();
    }
  }

  addAvalancheAndFlyTo(a) {
    this.addAvalanche(a);
    let boundingSphere = Cesium.BoundingSphere.fromPoints(Cesium.Cartesian3.fromDegreesArrayHeights(a.coords));

    this.viewer.camera.flyToBoundingSphere(boundingSphere, {
      duration: 4.0,
      offset: this.toHeadingPitchRange(0, -89.9, 4000),
      complete: () => {
        this.viewer.camera.flyToBoundingSphere(boundingSphere, {
          duration: 4.0,
          offset: this.toHeadingPitchRange(this.flyToHeadingFromAspect(a.slope.aspect), -35, 1200),
          complete: () => {
            // TODO show clues
            // showClickPathClue("Click on the red avalanche path for the details").then(() => {
            //   showCesiumHelpClue();
            // });

            // TODO is the below state needed?
            // this.clickPathClueShown = true;
            // setTimeout(() => {
            //   this.avalancheSpotlight = false;
            // }, 5000);
          }
        });}
    });
  }

  addAvalanches(avalanches) {
    let newAvalancheIds = avalanches.map(avalanche => { return avalanche.extId; });

    // remove any old avalanches that do not exist in the new set
    this.viewer.entities.values.filter(entity => {
      return entity.createdBy === this.CreatedByAvyEyes && newAvalancheIds.indexOf(entity.id) < 0;
    }).forEach(oldEntityToRemove => {
      this.removeEntity(oldEntityToRemove);
    });

    // does an avalanche entity need to be replaced? I.e. pin->path or path->pin?
    let replaceEntity = (entity, avalanche) => {
      if (!entity) return true;
      return (entity.polygon && !avalanche.coords) || (!entity.polygon && avalanche.coords);
    };

    avalanches.forEach(avalanche => {
      let existingEntity = this.viewer.entities.getById(avalanche.extId);

      if (replaceEntity(existingEntity, avalanche)) {
        this.removeEntity(existingEntity);
        this.addAvalanche(avalanche);
        // TODO show the 'click on the red path' notify
        // if (avalanche.coords) {
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

  getBoundingBox() {
    let getCoordsAtWindowPos = (x, y) => {
      let ray = this.viewer.camera.getPickRay(new Cesium.Cartesian2(x, y));
      let cart3 = this.viewer.scene.globe.pick(ray, this.viewer.scene);
      if (cart3) {
        return Cesium.Ellipsoid.WGS84.cartesianToCartographic(cart3);
      }
    };

    let UL = getCoordsAtWindowPos(0, 0);
    let UR = getCoordsAtWindowPos(this.viewer.canvas.clientWidth, 0);
    let LR = getCoordsAtWindowPos(this.viewer.canvas.clientWidth, this._viewer.canvas.clientHeight);
    let LL = getCoordsAtWindowPos(0, this.viewer.canvas.clientHeight);

    if (!UL || !UR || !LR || !LL) {
      throw new Error("Could not calculate bounding box");
    }

    return [
      Cesium.Math.toDegrees(Math.max(UL.latitude, UR.latitude, LR.latitude, LL.latitude)),
      Cesium.Math.toDegrees(Math.min(UL.latitude, UR.latitude, LR.latitude, LL.latitude)),
      Cesium.Math.toDegrees(Math.max(UL.longitude, UR.longitude, LR.longitude, LL.longitude)),
      Cesium.Math.toDegrees(Math.min(UL.longitude, UR.longitude, LR.longitude, LL.longitude))
    ];
  }

  toHeadingPitchRange(heading, pitch, range) {
    return new Cesium.HeadingPitchRange(Cesium.Math.toRadians(heading), Cesium.Math.toRadians(pitch), range);
  }

  flyToHeadingFromAspect(aspect) {
    if (aspect === "N") return 180.0;
    else if (aspect === "NE") return 225.0;
    else if (aspect === "E") return 270.0;
    else if (aspect === "SE") return 315.0;
    else if (aspect === "SW") return 45.0;
    else if (aspect === "W") return 90.0;
    else if (aspect === "NW") return 135.0;
    else return 0.0;
  }
}

export default CesiumController;