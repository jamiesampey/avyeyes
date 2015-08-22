define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var resetViewStub = sinon.stub();
    var viewStub = {
        resetView: resetViewStub,
        reset: function() {
            this.resetView.reset();
        }
    };

    var cesiumViewerStub = sinon.stub();
    var cesiumSpy = {
        Viewer: cesiumViewerStub,
        reset: function() {
            this.Viewer.reset();
        }
    };

    describe("Ext ID reservation", function() {
        var avyReport;

        beforeEach(function(done) {
            viewStub.reset();

            new Squire()
            .mock("lib/Cesium/Cesium", cesiumSpy)
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
    });
});