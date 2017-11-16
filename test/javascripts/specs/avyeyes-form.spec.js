define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var viewStub = {
        getRequestParam: function(param) {
            return param;
        },
        showModalDialog: function(msg) { },
        csrfTokenFromCookie: function() { return "1234" }
    };

    var avalanche = {
      "extId":"pzlmnecq",
      "extUrl":"http://dev.avyeyes.com:8080/pzlmnecq",
      "title":"July 4, 2015: north side of jones",
      "areaName":"north side of jones",
      "date":"07-04-2015",
      "submitterEmail": "joe.bob@here.com",
      "submitterExp": "P2",
      "weather":{
        "recentSnow":15,
        "recentWindSpeed": "ModerateBreeze",
        "recentWindDirection": "NW",
      },
      "slope":{
        "aspect": "SW",
        "angle":28,
        "elevation":3866
      },
      "classification":{
        "avyType": "WL",
        "trigger": "NE",
        "triggerModifier": "c",
        "interface": "G",
        "rSize": 1.5,
        "dSize": 3.0
      },
      "humanNumbers":{
        "modeOfTravel": "Snowmobiler",
        "caught": 0,
        "partiallyBuried": 1,
        "fullyBuried": 2,
        "injured": 3,
        "killed": -1
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

    var acSources = {
       AvalancheInterface: [{"label":"S - Layer of recent storm snow","value":"S"},{"label":"I - New/old snow interface","value":"I"},{"label":"O - Within old snow","value":"O"},{"label":"G - Ground, glacial ice, or firm","value":"G"},{"label":"U - Unknown","value":"U"}],
       ExperienceLevel: [{"label":"Amateur with little or no avalanche education","value":"A0"},{"label":"Amateur with Level 1 education","value":"A1"},{"label":"Amateur with Level 2 education","value":"A2"},{"label":"On-snow pro with little or no avalanche education","value":"P0"},{"label":"On-snow pro with significant avalanche education","value":"P1"},{"label":"Professional avalanche forecaster or technician","value":"P2"}],
       WindSpeed: [{"label":"Calm","value":"Calm"},{"label":"Light breeze","value":"LightBreeze"},{"label":"Moderate breeze","value":"ModerateBreeze"},{"label":"Strong breeze","value":"StrongBreeze"},{"label":"Gale","value":"Gale"},{"label":"Storm","value":"Storm"}],
       AvalancheTriggerModifier: [{"label":"r - Remote release","value":"r"},{"label":"y - Sympathetic release","value":"y"},{"label":"c - Controlled or intentional release","value":"c"},{"label":"u - Unintentional release","value":"u"}],
       AvalancheType: [{"label":"L - Loose-snow avalanche","value":"L"},{"label":"WL - Wet loose-snow avalanche","value":"WL"},{"label":"SS - Soft slab avalanche","value":"SS"},{"label":"HS - Hard slab avalanche","value":"HS"},{"label":"WS - Wet slab avalanche","value":"WS"},{"label":"I - Ice fall or avalanche","value":"I"},{"label":"SF - Slush flow","value":"SF"},{"label":"C - Cornice fall (w/o avalanche)","value":"C"},{"label":"R - Roof avalanche","value":"R"}],
       ModeOfTravel: [{"label":"Skier","value":"Skier"},{"label":"Snowboarder","value":"Snowboarder"},{"label":"Snowmobiler","value":"Snowmobiler"},{"label":"Snowshoer","value":"Snowshoer"},{"label":"Hiker","value":"Hiker"},{"label":"Climber","value":"Climber"},{"label":"Motorist","value":"Motorist"},{"label":"Other","value":"Other"}],
       AvalancheTrigger: [{"category":"Natural Triggers","label":"N - Natural avalanche trigger","value":"N"},{"category":"Natural Triggers","label":"NC - Cornice fall","value":"NC"},{"category":"Natural Triggers","label":"NE - Earthquake","value":"NE"},{"category":"Natural Triggers","label":"NI - Ice fall","value":"NI"},{"category":"Natural Triggers","label":"NL - Loose snow avalanche","value":"NL"},{"category":"Natural Triggers","label":"NS - Slab avalanche","value":"NS"},{"category":"Natural Triggers","label":"NR - Rock fall","value":"NR"},{"category":"Natural Triggers","label":"NO - Unclassified natural trigger","value":"NO"},{"category":"Artificial Triggers: Explosive","label":"AA - Artillery","value":"AA"},{"category":"Artificial Triggers: Explosive","label":"AE - Thrown or placed explosive","value":"AE"},{"category":"Artificial Triggers: Explosive","label":"AL - Avalauncher","value":"AL"},{"category":"Artificial Triggers: Explosive","label":"AB - Above snow air blast","value":"AB"},{"category":"Artificial Triggers: Explosive","label":"AC - Cornice fall from human or explosive","value":"AC"},{"category":"Artificial Triggers: Explosive","label":"AX - Gas exploder","value":"AX"},{"category":"Artificial Triggers: Explosive","label":"AH - Explosive placed from helicopter","value":"AH"},{"category":"Artificial Triggers: Explosive","label":"AP - Pre-placed remote explosive","value":"AP"},{"category":"Artificial Triggers: Misc","label":"AW - Wildlife","value":"AW"},{"category":"Artificial Triggers: Misc","label":"AU - Unknown artificial trigger","value":"AU"},{"category":"Artificial Triggers: Misc","label":"AO - Unclassified artificial trigger","value":"AO"},{"category":"Artificial Triggers: Vehicle","label":"AM - Snowmobile","value":"AM"},{"category":"Artificial Triggers: Vehicle","label":"AK - Snowcat","value":"AK"},{"category":"Artificial Triggers: Vehicle","label":"AV - Vehicle","value":"AV"},{"category":"Artificial Triggers: Human","label":"AS - Skier","value":"AS"},{"category":"Artificial Triggers: Human","label":"AR - Snowboarder","value":"AR"},{"category":"Artificial Triggers: Human","label":"AI - Snowshoer","value":"AI"},{"category":"Artificial Triggers: Human","label":"AF - Foot penetration","value":"AF"}],
       Direction: [{"label":"N","value":"N"},{"label":"NE","value":"NE"},{"label":"E","value":"E"},{"label":"SE","value":"SE"},{"label":"S","value":"S"},{"label":"SW","value":"SW"},{"label":"W","value":"W"},{"label":"NW","value":"NW"}]
    };

    window.AutoCompleteSources = acSources;

    var s3Promise = {
        then: function() {
            return {
                bucket: "testBucket",
                accessKeyId: "1234",
                secretAccessKey: "abcd"
            }
        }
    };

    describe("Display read-only form", function() {
        var avyForm;

        var mousePos = {x: 523, y: 527};

        var twttrLoadStub = sinon.stub();
        window.twttr = {
            widgets: {
                load: twttrLoadStub
            }
        };

        beforeEach(function(done) {
            twttrLoadStub.reset();

            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
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

            expect($("#roAvyFormTitle")).toHaveText(avalanche.title);
            expect($("#roAvyFormSubmitterExp")).toHaveText("Professional avalanche forecaster or technician");
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
            expect($("#roAvyFormAspect")).toHaveText("SW");
            expect($("#roAvyFormAngle")).toHaveText(avalanche.slope.angle);
        });

        it("sets weather fields", function() {
            setFixtures("<span id='roAvyFormRecentSnow'></span>"
                + "<span id='roAvyFormRecentWindSpeed'></span>"
                + "<span id='roAvyFormRecentWindDirection'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormRecentSnow")).toHaveText(avalanche.weather.recentSnow + " cm");
            expect($("#roAvyFormRecentWindSpeed")).toHaveText("Moderate breeze");
            expect($("#roAvyFormRecentWindDirection")).toHaveText("NW");
        });

        it("sets classification fields", function() {
            setFixtures("<span id='roAvyFormType'></span>"
                + "<span id='roAvyFormTrigger'></span>"
                + "<span id='roAvyFormTriggerModifier'></span>"
                + "<span id='roAvyFormInterface'></span>"
                + "<span id='roAvyFormRSize'></span>"
                + "<span id='roAvyFormDSize'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormType")).toHaveText("WL - Wet loose-snow avalanche");
            expect($("#roAvyFormTrigger")).toHaveText("NE - Earthquake");
            expect($("#roAvyFormTriggerModifier")).toHaveText("c - Controlled or intentional release");
            expect($("#roAvyFormInterface")).toHaveText("G - Ground, glacial ice, or firm");
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
            expect($("#roAvyFormModeOfTravel")).toHaveText("Snowmobiler");
        });

        it("sets comments", function() {
            setFixtures("<table><tr id='roAvyFormCommentsRow'><td>"
                + "<div id='roAvyFormComments'></div></td></tr></table>"
                + "<table><tr id='roAvyFormImageRow'><td>"
                + "<ul id='roAvyFormImageList'></ul></td></tr></table>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormComments")).toHaveValue(avalanche.comments);
        });

        it("sets up social buttons", function() {
            avyForm.displayReadOnlyForm(mousePos, avalanche);
            expect(twttrLoadStub.callCount).toBe(1);
        });
    });

    describe("Display read-write form", function() {
        var avyForm;

        window.AutoCompleteSources = acSources;

        var getTestAutoCompleteObj = function(enumObjArray, code) {
            return $(enumObjArray).filter(function() { return this.value == code; })[0];
        };

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
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

        it("set ext ID fixture", function() {
            setFixtures("<input id='rwAvyFormExtId'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");

            avyForm.displayReadWriteForm(avalanche);
            expect($("#rwAvyFormExtId")).toHaveValue(avalanche.extId);
        });

        it("set submitter info", function() {
            setFixtures("<input id='rwAvyFormSubmitterEmail'/>");
            sinon.stub(avyForm, "resetReadWriteImageUpload");
            var spy = sinon.spy(avyForm, "setReadWriteAutocompleteVal").withArgs("#rwAvyFormSubmitterExp", getTestAutoCompleteObj(acSources["ExperienceLevel"], "P2"));

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
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormSubmitterExp", getTestAutoCompleteObj(acSources["ExperienceLevel"], "P2")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormRecentWindSpeed", getTestAutoCompleteObj(acSources["WindSpeed"], "ModerateBreeze")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormRecentWindDirection", getTestAutoCompleteObj(acSources["Direction"], "NW")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormType", getTestAutoCompleteObj(acSources["AvalancheType"], "WL")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormTrigger", getTestAutoCompleteObj(acSources["AvalancheTrigger"], "NE")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormTriggerModifier", getTestAutoCompleteObj(acSources["AvalancheTriggerModifier"], "c")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormInterface", getTestAutoCompleteObj(acSources["AvalancheInterface"], "G")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormAspect", getTestAutoCompleteObj(acSources["Direction"], "SW")).once();
            mock.expects("setReadWriteAutocompleteVal").withExactArgs("#rwAvyFormModeOfTravel", getTestAutoCompleteObj(acSources["ModeOfTravel"], "Snowmobiler")).once();

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
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
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
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
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
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
                done();
            });
        });

        it("highlight erroneous fields", function() {
            setFixtures("<input id='rwAvyFormSubmitterEmail' value='joebob'/>"
                + "<input id='rwAvyFormSubmitterExp'/>"
                + "<input id='rwAvyFormSubmitterExpAC'/>"
                + "<input id='rwAvyFormAreaName'/>"
                + "<input id='rwAvyFormDate'/>"
                + "<input id='rwAvyFormAngle' value='abc'/>"
                + "<input id='rwAvyFormAspect'/>"
                + "<input id='rwAvyFormAspectAC'/>");

            avyForm.validateReportFields();

            expect($("#rwAvyFormSubmitterEmail")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormSubmitterExpAC")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormAreaName")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormDate")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormAngle")).toHaveCss({border: "1px solid red"});
            expect($("#rwAvyFormAspectAC")).toHaveCss({border: "1px solid red"});
        });
    });

    describe("Field handling", function() {
        var avyForm;

        beforeEach(function(done) {
            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes-form"], function(AvyForm) {
                avyForm = new AvyForm(viewStub, s3Promise);
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