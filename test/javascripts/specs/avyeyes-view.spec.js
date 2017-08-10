define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var cesiumCameraMoveStartSpy = sinon.spy();
    var cesiumCameraMoveEndSpy = sinon.spy();
    var cesiumViewerStub = sinon.stub();
    cesiumViewerStub.resetAll = function() {
        this.reset();
        cesiumCameraMoveStartSpy.reset();
        cesiumCameraMoveEndSpy.reset();
    };
    cesiumViewerStub.returns({
        scene: {
            canvas: {}
        },
        camera: {
            moveStart: {addEventListener: cesiumCameraMoveStartSpy},
            moveEnd: {addEventListener: cesiumCameraMoveEndSpy}
        },
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
            this.Viewer.resetAll();
            this.SceneMode.reset();
            this.CesiumTerrainProvider.reset();
            this.BingMapsImageryProvider.reset();
            this.BingMapsStyle.reset();
            this.ScreenSpaceEventHandler.reset();
            this.ScreenSpaceEventType.reset();
        }
    };
    window.Cesium = cesiumSpy;

    var fbSpy = {
        init: function(params) {}
    };
    window.FB = fbSpy;

    var avyEyesUiStub = sinon.stub().returns({
        loaded: { then: function() {} }
    });

    describe("AvyEyesView constructor", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();
            avyEyesUiStub.reset();

            new Squire()
            .mock("avyeyes-ui", avyEyesUiStub)
            .mock("avyeyes-form", sinon.stub())
            .mock("notify", sinon.stub())
            .require(["avyeyes-view"], function (AvyEyesView) {
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

        it("sets the Cesium camera move event listeners to set eye altitude", function() {
            expect(cesiumCameraMoveStartSpy.callCount).toBe(1);
            expect(cesiumCameraMoveEndSpy.callCount).toBe(1);
        });

        it("sets the Cesium LEFT_CLICK screen space event handler", function() {
            expect(cesiumSpy.ScreenSpaceEventHandler.callCount).toBe(1);
        });

        it("wires the UI", function() {
            expect(avyEyesUiStub.callCount).toBe(1);
            expect(avyEyesUiStub.alwaysCalledWith(avyEyesView)).toBe(true);
        });
    });

    describe("AvyEyesView modal dialogs", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes-ui", avyEyesUiStub)
            .mock("avyeyes-form", sinon.spy())
            .mock("notify", sinon.stub())
            .require(["avyeyes-view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("opens the jQuery UI utility dialog with the correct title and message", function() {
            var htmlMsg = "<h1>Some important information</h1>"
            setFixtures("<div id='multiDialog'></div>");

            var jQueryMock = sinon.mock($.fn)
            jQueryMock.expects("dialog").withArgs("option", "title", "Info").once();
            jQueryMock.expects("dialog").withArgs("open").once();

            avyEyesView.showModalDialog(htmlMsg);

            expect($("#multiDialog")).toHaveHtml(htmlMsg);
            jQueryMock.verify();
        });
    });

    describe("AvyEyesView start/end report", function () {
        var avyEyesView;

        var reportStub = sinon.stub();
        var beginReportStub = sinon.stub();
        reportStub.returns({
            beginReport: beginReportStub
        });

        var closeReportFormStub = sinon.stub();
        var clearReportFieldsStub = sinon.stub();
        var formStub = sinon.stub();
        formStub.returns({
            closeReportForm: closeReportFormStub,
            clearReportFields: clearReportFieldsStub
        });

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes-ui", avyEyesUiStub)
            .mock("avyeyes-report", reportStub)
            .mock("avyeyes-form", formStub)
            .mock("notify", sinon.stub())
            .require(["avyeyes-view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("Starts a new report", function() {
            var removeAllEntitiesStub = sinon.stub(avyEyesView, "removeAllEntities");
            var cancelReportStub = sinon.stub(avyEyesView, "cancelReport");
            window.onbeforeunload = null;

            avyEyesView.doReport();

            expect(removeAllEntitiesStub.callCount).toBe(1);
            expect(cancelReportStub.callCount).toBe(1);
            expect(reportStub.calledWithNew()).toBe(true);
            expect(avyEyesView.currentReport).toBeDefined();
            expect(beginReportStub.callCount).toBe(1);
            expect(window.onbeforeunload).toBeDefined();
        });

        it("Cancels a report", function() {
            window.onbeforeunload = function(e) {};
            avyEyesView.currentReport = {};

            avyEyesView.cancelReport();

            expect(closeReportFormStub.callCount).toBe(1);
            expect(clearReportFieldsStub.callCount).toBe(1);
            expect(avyEyesView.currentReport).toBeNull();
            expect(window.onbeforeunload).toBeNull();
        });
    });

    describe("AvyEyesView reset view", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes-ui", avyEyesUiStub)
            .mock("avyeyes-form", sinon.stub())
            .mock("notify", sinon.stub())
            .require(["avyeyes-view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("works", function() {
            var removeAllEntitiesStub = sinon.stub(avyEyesView, "removeAllEntities");
            var cancelReportStub = sinon.stub(avyEyesView, "cancelReport");
            var showControlsStub = sinon.stub(avyEyesView, "showControls");

            avyEyesView.resetView();

            expect(removeAllEntitiesStub.callCount).toBe(1);
            expect(cancelReportStub.callCount).toBe(1);
            expect(showControlsStub.callCount).toBe(1);
        });
    });
});