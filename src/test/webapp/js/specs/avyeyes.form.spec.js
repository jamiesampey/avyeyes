define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

    var avalanche = {
      "extId":"pzlmnecq",
      "extUrl":"http://dev.avyeyes.com:8080/pzlmnecq",
      "areaName":"north side of jones",
      "avyDate":"07-04-2015",
      "submitterExp":{
        "value":"PE",
        "label":"Professional avalanche forecaster or technician"
      },
      "scene":{
        "skyCoverage":{
          "value":"FEW",
          "label":"FEW - Few clouds"
        },
        "precipitation":{
          "value":"SN",
          "label":"SN - Snow"
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
        "size":3206315
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
                avyForm = new AvyForm();
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

            expect($("#roAvyFormTitle")).toHaveText(avalanche.avyDate + ": " + avalanche.areaName);
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

            expect($("#roAvyFormElevation")).toHaveText(avalanche.slope.elevation);
            expect($("#roAvyFormElevationFt")).toHaveText(Math.round(avalanche.slope.elevation * 3.28084));
            expect($("#roAvyFormAspect")).toHaveText(avalanche.slope.aspect.label);
            expect($("#roAvyFormAngle")).toHaveText(avalanche.slope.angle);
        });

        it("sets classification fields", function() {
            setFixtures("<span id='roAvyFormType'></span>"
                + "<span id='roAvyFormTrigger'></span>"
                + "<span id='roAvyFormInterface'></span>"
                + "<span id='roAvyFormRSize'></span>"
                + "<span id='roAvyFormDSize'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormType")).toHaveText(avalanche.classification.avyType.label);
            expect($("#roAvyFormTrigger")).toHaveText(avalanche.classification.trigger.label);
            expect($("#roAvyFormInterface")).toHaveText(avalanche.classification.interface.label);
            expect($("#roAvyFormRSize")).toHaveText(avalanche.classification.rSize);
            expect($("#roAvyFormDSize")).toHaveText(avalanche.classification.dSize);
        });

        it("sets scene fields", function() {
            setFixtures("<span id='roAvyFormSky'></span>"
                + "<span id='roAvyFormPrecip'></span>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormSky")).toHaveText(avalanche.scene.skyCoverage.label);
            expect($("#roAvyFormPrecip")).toHaveText(avalanche.scene.precipitation.label);
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
            expect($("#roAvyFormNumKilled")).toHaveText("Unknown");
            expect($("#roAvyFormModeOfTravel")).toHaveText(avalanche.humanNumbers.modeOfTravel.label);
        });

        it("sets comments and images", function() {
            setFixtures("<table><tr id='roAvyFormCommentsRow'><td>"
                + "<div id='roAvyFormComments'></div></td></tr></table>"
                + "<table><tr id='roAvyFormImageRow'><td>"
                + "<ul id='roAvyFormImageList'></ul></td></tr></table>");

            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormComments")).toHaveText(avalanche.comments.trim());
            expect($("#roAvyFormImageList li").length).toBe(3);
        });

        it("sets up social buttons", function() {
            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect(fbParseStub.callCount).toBe(1);
            expect(twttrLoadStub.callCount).toBe(1);
        });
    });

});