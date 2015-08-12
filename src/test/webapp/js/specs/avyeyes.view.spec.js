define(["squire", "sinon"], function (Squire, sinon) {

    var cesiumViewerStub = sinon.stub();
    var removeAllStub = sinon.stub();
    cesiumViewerStub.returns({
        scene: {
            canvas: {}
        },
        entities: {
            removeAll: removeAllStub
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

    describe("AvyEyesView constructor", function () {
        var avyEyesView;

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes.form", sinon.spy())
            .mock("lib/Cesium/Cesium", cesiumSpy)
            .require(["avyeyes.view"], function (AvyEyesView) {
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

            new Squire()
            .mock("avyeyes.form", sinon.spy())
            .mock("lib/Cesium/Cesium", cesiumSpy)
            .require(["avyeyes.view"], function (AvyEyesView) {
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

    describe("AvyEyesView start/end report", function () {
        var avyEyesView;

        var reportStub = sinon.stub();
        var beginReportStub = sinon.stub();
        reportStub.returns({
            beginReport: beginReportStub
        });

        var closeReportDialogsStub = sinon.stub();
        var clearReportFieldsStub = sinon.stub();
        var formStub = {
            closeReportDialogs: closeReportDialogsStub,
            clearReportFields: clearReportFieldsStub
        };

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes.report", reportStub)
            .mock("avyeyes.form", formStub)
            .mock("lib/Cesium/Cesium", cesiumSpy)
            .require(["avyeyes.view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("Starts a new report", function() {
            var cancelReportStub = sinon.stub(avyEyesView, "cancelReport");
            avyEyesView.doReport();
            expect(cancelReportStub.callCount).toBe(1);
            expect(reportStub.calledWithNew()).toBe(true);
            expect(avyEyesView.currentReport).toBeDefined();
            expect(beginReportStub.callCount).toBe(1);
        });

        it("Cancels a report", function() {
            avyEyesView.currentReport = {};
            avyEyesView.cancelReport();
            expect(closeReportDialogsStub.callCount).toBe(1);
            expect(clearReportFieldsStub.callCount).toBe(1);
            expect(avyEyesView.currentReport).toBeNull();
        });
    });

    describe("AvyEyesView reset view", function () {
        var avyEyesView;

        var showSearchDivStub = sinon.stub();
        var avyEyesUiStub = {
            showSearchDiv: showSearchDivStub
        };

        beforeEach(function (done) {
            cesiumSpy.reset();

            new Squire()
            .mock("avyeyes.form", sinon.spy())
            .mock("avyeyes.ui", avyEyesUiStub)
            .mock("lib/Cesium/Cesium", cesiumSpy)
            .require(["avyeyes.view"], function (AvyEyesView) {
                avyEyesView = new AvyEyesView();
                done();
            });
        });

        it("works", function() {
            var cancelReportStub = sinon.stub(avyEyesView, "cancelReport");
            avyEyesView.resetView();
            expect(removeAllStub.callCount).toBe(1);
            expect(cancelReportStub.callCount).toBe(1);
            expect(showSearchDivStub.callCount).toBe(1);
        });
    });
});