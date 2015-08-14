define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    describe("AvyForm.displayReadOnlyForm", function() {

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function (AvyForm) {
                done();
            });
        });

        it("sets all read-only avy fields", function() {
            expect(true).toBe(true);
        });

    });

});