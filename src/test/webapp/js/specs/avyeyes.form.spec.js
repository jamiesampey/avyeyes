define(["squire", "sinon", "jasmine-jquery"], function (Squire, sinon, jas$) {

//    jasmine.getFixtures().fixturesPath = 'src/main/webapp'
//    loadFixtures("index.html")

    describe("AvyForm.displayReadOnlyForm", function() {
        var avyForm;

        var mousePos = {x: 523, y: 527};

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
            "killed":4
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

        beforeEach(function(done) {
            window.FB = {
                XFBML: {
                    parse: sinon.stub()
                }
            };

            window.twttr = {
                widgets: {
                    load: sinon.stub()
                }
            };

            new Squire()
            .mock("//sdk.amazonaws.com/js/aws-sdk-2.1.34.min.js", sinon.stub())
            .require(["avyeyes.form"], function(AvyForm) {
                avyForm = new AvyForm();
                done();
            });
        });

        it("sets all read-only avy fields", function() {
            setFixtures("<span id='roAvyFormAngle'></span>");
            avyForm.displayReadOnlyForm(mousePos, avalanche);

            expect($("#roAvyFormAngle")).toHaveText(avalanche.slope.angle);
        });

    });

});