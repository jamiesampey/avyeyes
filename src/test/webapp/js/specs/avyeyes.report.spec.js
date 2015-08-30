define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var cesiumEventHandlerStub = {
        setInputAction: sinon.stub(),
        removeInputAction: sinon.stub()
    };

    var setReportDrawingInputsStub = sinon.stub();
    var formStub = {
        setReportDrawingInputs: setReportDrawingInputsStub
    };

    var resetViewStub = sinon.stub();
    var removeEntitiesStub = sinon.stub();
    var viewStub = {
        resetView: resetViewStub,
        cesiumViewer: {
            entities: {
                remove: removeEntitiesStub
            }
        },
        cesiumEventHandler: cesiumEventHandlerStub,
        form: formStub,
        reset: function() {
            this.resetView.reset();
            this.cesiumViewer.entities.remove.reset();
            this.cesiumEventHandler.setInputAction.reset();
            this.cesiumEventHandler.removeInputAction.reset();
            this.form.setReportDrawingInputs.reset();
        }
    };

    var cesiumSpy = {
        ScreenSpaceEventType: {
            LEFT_CLICK: sinon.spy(),
            MOUSE_MOVE: sinon.spy()
        },
        Color: {
            RED: {
                withAlpha: sinon.spy()
            }
        },
        reset: function() {
            this.ScreenSpaceEventType.reset();
        }
    };
    window.Cesium = cesiumSpy;


    describe("Start a new report", function() {
        var avyReport;

        beforeEach(function(done) {
            viewStub.reset();

            new Squire()
            .require(["avyeyes.report"], function(AvyReport) {
                avyReport = new AvyReport(viewStub);
                done();
            });
        });

        it("sets Ext ID fixture after successful ajax call", function() {
            setFixtures("<input id='rwAvyFormExtId'/>");
            var json = {extId: "4030jfj3"};
            var jsonAjaxStub = sinon.stub($, "getJSON").withArgs("/rest/reserveExtId").callsArgWith(1, json).returns({fail: sinon.stub()});

            avyReport.reserveExtId();
            expect($("#rwAvyFormExtId")).toHaveValue(json.extId);
        });

        it("makes the necessary calls", function() {
            setFixtures("<input id='avyReportInitLocation' value='blahblah' /><div id='avyReportDrawButtonContainer'></div>");
            var avyReportMock = sinon.mock(avyReport);
            var reserveExtIdExpectation = avyReportMock.expects("reserveExtId").once();

            avyReport.beginReport();
            reserveExtIdExpectation.verify();
            expect($("#avyReportInitLocation")).toHaveValue("");
            expect($("#avyReportDrawButtonContainer")).toHaveCss({visibility: "visible"});
        });
    });

    describe("Drawing", function() {
        var avyReport;

        beforeEach(function(done) {
            viewStub.reset();

            new Squire()
            .require(["avyeyes.report"], function(AvyReport) {
                avyReport = new AvyReport(viewStub);
                done();
            });
        });

        it("Starts a new drawing", function() {
            setFixtures("<div id='cesiumContainer'/><div id='avyReportDrawButtonContainer'></div>");

            avyReport.startDrawing();

            expect($("#avyReportDrawButtonContainer")).toHaveCss({visibility: "hidden"});
            expect($("#cesiumContainer")).toHaveCss({cursor: "crosshair"});
            expect(cesiumEventHandlerStub.removeInputAction.calledWith(cesiumSpy.ScreenSpaceEventType.LEFT_CLICK)).toBe(true);

            expect(cesiumEventHandlerStub.setInputAction.calledWith(sinon.match.func, cesiumSpy.ScreenSpaceEventType.LEFT_CLICK)).toBe(true);
            expect(cesiumEventHandlerStub.setInputAction.calledWith(sinon.match.func, cesiumSpy.ScreenSpaceEventType.MOUSE_MOVE)).toBe(true);
        });

        it("Clears a drawing", function() {
            var testPolygon = {attr: "someAttribute"}
            avyReport.drawingPolygon = testPolygon;

            avyReport.clearDrawing();

            expect(removeEntitiesStub.calledWithExactly(testPolygon)).toBe(true);
            expect(formStub.setReportDrawingInputs.calledWithExactly('', '', '', '', '', '')).toBe(true);
            expect(avyReport.drawingPolygon).toBeNull();
        });
    });
});