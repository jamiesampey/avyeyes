define(["squire", "sinon"], function (Squire, sinon) {

    var cesiumViewerStub = sinon.stub();
    cesiumViewerStub.returns({
        scene: {
            canvas: {}
        }
    });

    var cesiumEventHandlerStub = sinon.stub();
    cesiumEventHandlerStub.returns({
        setInputAction: sinon.spy()
    });

    var cesiumSpy = {
        Viewer: cesiumViewerStub,
        SceneMode: sinon.spy(),
        CesiumTerrainProvider: sinon.spy(),
        BingMapsImageryProvider: sinon.spy(),
        BingMapsStyle: sinon.spy(),
        ScreenSpaceEventHandler: cesiumEventHandlerStub,
        ScreenSpaceEventType: sinon.spy(),
        reset: function() {
            this.Viewer.reset();
            this.SceneMode.reset();
            this.CesiumTerrainProvider.reset();
            this.BingMapsImageryProvider.reset();
            this.BingMapsStyle.reset();
            this.ScreenSpaceEventHandler.reset();
            this.ScreenSpaceEventType.reset();
        }
    };

    var amdScope = new Squire()
        .mock("avyeyes.form", sinon.spy())
        .mock("lib/Cesium/Cesium", cesiumSpy);

    describe("AvyEyesView constructor", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();

            amdScope.require(["avyeyes.view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("sets Bing key", function() {
            expect(avyEyesView.bingKey).toBeDefined();
        });

        it("instantiates the Cesium Viewer", function() {
            expect(cesiumViewerStub.callCount).toBe(1);
            expect(avyEyesView.cesiumViewer).toBeDefined();
            expect(cesiumSpy.CesiumTerrainProvider.callCount).toBe(1);
            expect(cesiumSpy.BingMapsImageryProvider.callCount).toBe(1);
        });

        it("sets the Cesium LEFT_CLICK screen space event handler", function() {
            expect(cesiumSpy.ScreenSpaceEventHandler.callCount).toBe(1);
        });
    });

    describe("AvyEyesView modal dialogs", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();

            amdScope.require(["avyeyes.view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("opens the jQuery UI utility dialog with the correct title and message", function() {
            var title = "A Dialog Title"
            var msg = "Some important information"

            var jQueryMock = sinon.mock($.fn)
            jQueryMock.expects("html").withArgs(msg).once();
            jQueryMock.expects("dialog").withArgs("option", "title", title).once();
            jQueryMock.expects("dialog").withArgs("open").once();

            avyEyesView.showModalDialog(title, msg);
            jQueryMock.verify();
        });

        it("opens the jQuery UI help dialog to the correct tab", function() {
            var tab = Math.floor((Math.random() * 5) + 1);

            var jQueryMock = sinon.mock($.fn)
            jQueryMock.expects("tabs").withArgs("option", "active", tab).once();
            jQueryMock.expects("dialog").withArgs("open").once();

            avyEyesView.showHelp(tab);
            jQueryMock.verify();
        });
    });
});