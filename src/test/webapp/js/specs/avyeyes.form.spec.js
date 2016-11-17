define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var viewStub = {
        getRequestParam: function(param) {
            return param;
        }
    };

    var avalanche = {
      "extId":"pzlmnecq",
      "extUrl":"http://dev.avyeyes.com:8080/pzlmnecq",
      "areaName":"north side of jones",
      "date":"07-04-2015",
      "submitterEmail": "joe.bob@here.com",
      "submitterExp":{
        "value":"P2",
        "label":"Professional avalanche forecaster or technician"
      },
      "weather":{
        "recentSnow":15,
        "recentWindSpeed":{
          "value":"ModerateBreeze",
          "label":"Moderate breeze"
        },
        "recentWindDirection":{
          "value":"NW",
          "label":"NW"
        }
      },
      "slope":{
        "aspect":{
          "value":"SW",
          "label":"SW"
        },
        "angle":28,
        "elevation":3866
      },
      "classification":{
        "avyType":{
          "value":"WL",
          "label":"WL - Wet loose-snow avalanche"
        },
        "trigger":{
          "value":"NE",
          "label":"NE - Earthquake"
        },
        "triggerModifier":{
          "value":"c",
          "label":"c - A controlled or intentional release by the indicated trigger"
        },
        "interface":{
          "value":"G",
          "label":"G - Ground, glacial ice, or firm"
        },
        "rSize":1.5,
        "dSize":3.0
      },
      "humanNumbers":{
        "modeOfTravel":{
          "value":"Snowmobiler",
          "label":"Snowmobiler"
        },
        "caught":0,
        "partiallyBuried":1,
        "fullyBuried":2,
        "injured":3,
        "killed":-1
      },
      "comments":"North side of jones with pics",
      "images":[{
        "filename":"f2c4b446-53c7-432f-838a-fe46d4bff987.jpg",
        "mimeType":"image/jpeg",
        "size":3206315,
        "caption": "some text abount the image"
      },{
        "filename":"216c68e5-6c97-4706-813f-abeadd61bc99.jpg",
        "mimeType":"image/jpeg",
        "size":3849622
      },{
        "filename":"a68e3b05-3745-42de-974b-a2acf4cc2275.jpg",
        "mimeType":"image/jpeg",
        "size":3757995
      }]
    };


    describe("Display read-only form", function() {
        var avyForm;

        var mousePos = {x: 523, y: 527};
        var fbParseStub = sinon.stub();
        window.FB = {
            XFBML: {
                parse: fbParseStub
            }
        };
        var twttrLoadStub = sinon.stub();
        window.twttr = {
            widgets: {
                load: twttrLoadStub
            }
        };

        beforeEach(function(done) {
            fbParseStub.reset();
            twttrLoadStub.reset();

            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                done();
            });
        });

        it("sets title fields", function() {
            setFixtures("<span id='roAvyFormTitle'></span>"
                + "<span id='roAvyFormSubmitterExp'></span>"
                + "<a id='roAvyFormExtLink'/>"
                + "<td id='roAvyFormSocialFacebookContainer'></td>"
                + "<td id='roAvyFormSocialTwitterContainer'></td>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormTitle")).toHaveText(avalanche.date + ": " + avalanche.areaName);
            expect($("#roAvyFormSubmitterExp")).toHaveText(avalanche.submitterExp.label);
            expect($("#roAvyFormExtLink")).toHaveAttr("href", avalanche.extUrl);
            expect($("#roAvyFormExtLink")).toHaveText(avalanche.extUrl);
            expect($("#roAvyFormSocialFacebookContainer")).not.toBeEmpty();
            expect($("#roAvyFormSocialTwitterContainer")).not.toBeEmpty();
        });

        it("sets slope characteristic fields", function() {
            setFixtures("<span id='roAvyFormElevation'></span>"
                + "<span id='roAvyFormElevationFt'></span>"
                + "<span id='roAvyFormAspect'></span>"
                + "<span id='roAvyFormAngle'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormElevation")).toHaveText(avalanche.slope.elevation + " m");
            expect($("#roAvyFormElevationFt")).toHaveText(Math.round(avalanche.slope.elevation * 3.28084) + " ft");
            expect($("#roAvyFormAspect")).toHaveText(avalanche.slope.aspect.label);
            expect($("#roAvyFormAngle")).toHaveText(avalanche.slope.angle);
        });

        it("sets weather fields", function() {
            setFixtures("<span id='roAvyFormRecentSnow'></span>"
                + "<span id='roAvyFormRecentWindSpeed'></span>"
                + "<span id='roAvyFormRecentWindDirection'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormRecentSnow")).toHaveText(avalanche.weather.recentSnow + " cm");
            expect($("#roAvyFormRecentWindSpeed")).toHaveText(avalanche.weather.recentWindSpeed.label);
            expect($("#roAvyFormRecentWindDirection")).toHaveText(avalanche.weather.recentWindDirection.label);
        });

        it("sets classification fields", function() {
            setFixtures("<span id='roAvyFormType'></span>"
                + "<span id='roAvyFormTrigger'></span>"
                + "<span id='roAvyFormTriggerModifier'></span>"
                + "<span id='roAvyFormInterface'></span>"
                + "<span id='roAvyFormRSize'></span>"
                + "<span id='roAvyFormDSize'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormType")).toHaveText(avalanche.classification.avyType.label);
            expect($("#roAvyFormTrigger")).toHaveText(avalanche.classification.trigger.label);
            expect($("#roAvyFormTriggerModifier")).toHaveText(avalanche.classification.triggerModifier.label);
            expect($("#roAvyFormInterface")).toHaveText(avalanche.classification.interface.label);
            expect($("#roAvyFormRSize")).toHaveText(avalanche.classification.rSize);
            expect($("#roAvyFormDSize")).toHaveText(avalanche.classification.dSize);
        });

        it("sets human number fields", function() {
            setFixtures("<span id='roAvyFormNumCaught'></span>"
                + "<span id='roAvyFormNumPartiallyBuried'></span>"
                + "<span id='roAvyFormNumFullyBuried'></span>"
                + "<span id='roAvyFormNumInjured'></span>"
                + "<span id='roAvyFormNumKilled'></span>"
                + "<span id='roAvyFormModeOfTravel'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormNumCaught")).toHaveText(avalanche.humanNumbers.caught);
            expect($("#roAvyFormNumPartiallyBuried")).toHaveText(avalanche.humanNumbers.partiallyBuried);
            expect($("#roAvyFormNumFullyBuried")).toHaveText(avalanche.humanNumbers.fullyBuried);
            expect($("#roAvyFormNumInjured")).toHaveText(avalanche.humanNumbers.injured);
            expect($("#roAvyFormNumKilled")).toHaveText("unspecified");
            expect($("#roAvyFormModeOfTravel")).toHaveText(avalanche.humanNumbers.modeOfTravel.label);
        });

        it("sets comments and images", function() {
            setFixtures("<table><tr id='roAvyFormCommentsRow'><td>"
                + "<div id='roAvyFormComments'></div></td></tr></table>"
                + "<table><tr id='roAvyFormImageRow'><td>"
                + "<ul id='roAvyFormImageList'></ul></td></tr></table>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormComments")).toHaveValue(avalanche.comments);
            expect($("#roAvyFormImageList li").length).toBe(3);
        });

        it("sets up social buttons", function() {
            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect(fbParseStub.callCount).toBe(1);
            expect(twttrLoadStub.callCount).toBe(1);
        });
    });

    describe("Display read-write form", function() {
        var avyForm;

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                sinon.stub(avyForm, "setImageCellContent");
                done();
            });
        });

        it("Reset the image upload form", function() {
            var mock = sinon.mock(avyForm)
            mock.expects("resetReadWriteImageUpload").once();
            avyForm.displayReadWriteForm();
            mock.verify();

            var mock2 = sinon.mock(avyForm)
            mock2.expects("resetReadWriteImageUpload").withExactArgs(avalanche.extId).once();
            avyForm.displayReadWriteForm(avalanche);
            mock2.verify();
        });

        it("set ext ID and edit key fixture", function() {
            setFixtures("<input id='rwAvyFormExtId'/><input id='rwAvyFormEditKey'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");

            avyForm.displayReadWriteForm(avalanche);
            expect($("#rwAvyFormExtId")).toHaveValue(avalanche.extId);
            expect($("#rwAvyFormEditKey")).toHaveValue("edit");
        });

        it("set submitter info", function() {
            setFixtures("<input id='rwAvyFormSubmitterEmail'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var spy = sinon.spy(avyForm, "setReadWriteAutocompleteVal").withArgs("#rwAvyFormSubmitterExp", avalanche.submitterExp);

            avyForm.displayReadWriteForm(avalanche);

            expect($("#rwAvyFormSubmitterEmail")).toHaveValue(avalanche.submitterEmail);
            expect(spy.callCount).toBe(1);
        });

        it("set area name and date info", function() {
            setFixtures("<input id='rwAvyFormAreaName'/><input id='rwAvyFormDate'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");

            avyForm.displayReadWriteForm(avalanche);

            expect($("#rwAvyFormDate")).toHaveValue(avalanche.date);
            expect($("#rwAvyFormAreaName")).toHaveValue(avalanche.areaName);
        });

        it("set auto complete fields", function() {
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var mock = sinon.mock(avyForm);
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormSubmitterExp", avalanche.submitterExp).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormRecentWindSpeed", avalanche.weather.recentWindSpeed).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormRecentWindDirection", avalanche.weather.recentWindDirection).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormType", avalanche.classification.avyType).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormTrigger", avalanche.classification.trigger).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormTriggerModifier", avalanche.classification.triggerModifier).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormInterface", avalanche.classification.interface).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormAspect", avalanche.slope.aspect).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormModeOfTravel", avalanche.humanNumbers.modeOfTravel).once();

            avyForm.displayReadWriteForm(avalanche);
            mock.verify();
        });

        it("set slider fields", function() {
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var mock = sinon.mock(avyForm);
            mock.expects("setReadWriteSliderVal").withExactArgs("#rwAvyFormRsizeValue", avalanche.classification.rSize).once();
            mock.expects("setReadWriteSliderVal").withExactArgs("#rwAvyFormDsizeValue", avalanche.classification.dSize).once();

            avyForm.displayReadWriteForm(avalanche);
            mock.verify();
        });

        it("set spinner fields", function() {
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var mock = sinon.mock(avyForm);
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormRecentSnow", avalanche.weather.recentSnow).once();
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormNumCaught", avalanche.humanNumbers.caught).once();
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormNumPartiallyBuried", avalanche.humanNumbers.partiallyBuried).once();
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormNumFullyBuried", avalanche.humanNumbers.fullyBuried).once();
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormNumInjured", avalanche.humanNumbers.injured).once();
            mock.expects("setReadWriteSpinnerVal").withExactArgs("#rwAvyFormNumKilled", avalanche.humanNumbers.killed).once();

            avyForm.displayReadWriteForm(avalanche);
            mock.verify();
        });

        it("set slope info", function() {
            setFixtures("<input id='rwAvyFormElevation'/>"
                + "<input id='rwAvyFormElevationFt'/>"
                + "<input id='rwAvyFormAngle'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");

            avyForm.displayReadWriteForm(avalanche);

            expect($("#rwAvyFormElevation")).toHaveValue(avalanche.slope.elevation.toString());
            expect($("#rwAvyFormElevationFt")).toHaveValue(Math.round(avalanche.slope.elevation * 3.28084).toString());
            expect($("#rwAvyFormAngle")).toHaveValue(avalanche.slope.angle.toString());
        });

        it("set comments, images and delete binding", function() {
            setFixtures("<textarea id='rwAvyFormComments'></textarea>"
                + "<div id='rwAvyFormImageGrid'></div>"
                + "<div id='rwAvyFormDeleteBinding'></div>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var sortableFnStub = sinon.stub($.fn, "sortable");

            avyForm.displayReadWriteForm(avalanche);

            expect($("#rwAvyFormComments")).toHaveValue(avalanche.comments);
            expect($("#rwAvyFormImageGrid .rwAvyFormImageCell").length).toBe(3);
            expect(sortableFnStub.callCount).toBe(3);
            expect($("#rwAvyFormDeleteBinding")).toHaveValue(avalanche.extId);
        });
    });

    describe("Reset image upload form", function() {
        var avyForm;

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                done();
            });
        });

        it("Empties the current image grid", function() {
            setFixtures("<div id='rwAvyFormImageGrid'><div id='child1'/><div id='child2'/></div>");
            avyForm.resetReadWriteImageUpload("49f94e0d");
            expect($('#rwAvyFormImageGrid').html().length).toBe(0);
        });

        it("Sets sortable and fileupload on the grid", function() {
            setFixtures("<div id='rwAvyFormImageGrid'></div>");
            var sortableFnStub = sinon.stub($.fn, "sortable");
            var fileuploadFnStub = sinon.stub($.fn, "fileupload");

            avyForm.resetReadWriteImageUpload("49f94e0d");
            expect(sortableFnStub.callCount).toBe(1);
            expect(fileuploadFnStub.callCount).toBe(1);
        });
    });

    describe("Append image cell and set content", function() {
        var avyForm;

        var getFileBaseName = function(filename) {
            return filename.substring(0, filename.lastIndexOf('.'));
        }

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                sinon.stub(avyForm, "getSignedImageUrl");
                done();
            });
        });

        it("Adds the anchor, image, edit, and delete tags", function() {
            setFixtures("<div id='rwAvyFormImageGrid'></div>");
            var sortableFnStub = sinon.stub($.fn, "sortable");
            var fancyboxFnStub = sinon.stub($.fn, "fancybox");

            var imageZeroFilenameBase = getFileBaseName(avalanche.images[0].filename);

            avyForm.appendImageCellToReadWriteForm(imageZeroFilenameBase);
            avyForm.setImageCellContent(imageZeroFilenameBase, avalanche.extId, avalanche.images[0]);

            var imageWrapperDiv = $("#rwAvyFormImageGrid .rwAvyFormImageCell .rwAvyFormImageWrapper");
            expect(imageWrapperDiv.length).toBe(1);
            expect(imageWrapperDiv.children('a[id=' + imageZeroFilenameBase + '-anchor]').length).toBe(1);
            expect(imageWrapperDiv.children('img[id=' + imageZeroFilenameBase + '-edit]').length).toBe(1);
            expect(imageWrapperDiv.children('img[id=' + imageZeroFilenameBase + '-delete]').length).toBe(1);

            expect(sortableFnStub.callCount).toBe(1);
            expect(fancyboxFnStub.callCount).toBe(1);
        });

    });

    describe("Field validation", function() {
        var avyForm;

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                done();
            });
        });

        it("highlight erroneous fields", function() {
            setFixtures("<input id='rwAvyFormSubmitterEmail'/>"
                        + "<input id='rwAvyFormAreaName'/>"
                        + "<input id='rwAvyFormDate'/>");
            var errorFields = ["rwAvyFormSubmitterEmail", "rwAvyFormAreaName"];

            avyForm.highlightReportErrorFields(errorFields);

            expect($("#rwAvyFormSubmitterEmail")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormAreaName")).toHaveCss({border: "1px solid red"});
        });
    });

    describe("Field handling", function() {
        var avyForm;

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub);
                done();
            });
        });

        it("clear fields", function() {
            var mock = sinon.mock(avyForm);
            mock.expects("resetReportErrorFields").once();
            mock.expects("setReportDrawingInputs").withExactArgs('', '', '', '', '', '').once();

            avyForm.clearReportFields();

            mock.verify();
        });

        it("set drawing inputs", function() {
            setFixtures("<input id='rwAvyFormLng'/>"
                        + "<input id='rwAvyFormLat'/>"
                        + "<input id='rwAvyFormElevation'/>"
                        + "<input id='rwAvyFormElevationFt'/>"
                        + "<input id='rwAvyFormAspectAC'/>"
                        + "<input id='rwAvyFormAspect'/>"
                        + "<input id='rwAvyFormAngle'/>"
                        + "<input id='rwAvyFormCoords'/>");

            var lng = "-104.94964"; var lat = "39.940303"; var elevation = 3456.49;
            var aspect = "NE"; var angle = "35"; var coordStr = "-104.94964,39.940303,3456.49 -104.94964,39.940303,3456.49 -104.94964,39.940303,3456.49";
            avyForm.setReportDrawingInputs(lng, lat, elevation, aspect, angle, coordStr)

            expect($("#rwAvyFormLng")).toHaveValue(lng);
            expect($("#rwAvyFormLat")).toHaveValue(lat);
            expect($("#rwAvyFormElevation")).toHaveValue(elevation.toString());
            expect($("#rwAvyFormElevationFt")).toHaveValue(Math.round(elevation * 3.28084).toString());
            expect($("#rwAvyFormAspectAC")).toHaveValue(aspect);
            expect($("#rwAvyFormAspect")).toHaveValue(aspect);
            expect($("#rwAvyFormAngle")).toHaveValue(angle);
            expect($("#rwAvyFormCoords")).toHaveValue(coordStr);
        });
    });
});