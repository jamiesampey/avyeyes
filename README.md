![AvyEyes screenshot](/src/main/webapp/images/avyeyes.jpg?raw=true =800x)

**AvyEyes** is a web application for reporting and sharing avalanches in a 3D, Google Earth-like interactive view. 

The typical AvyEyes workflow is as follows:

1. I'm out backcountry skiing with my friends and I trigger an avalanche
2. I come home and go to https://avyeyes.com to create an avalanche report to share with fellow skiers/riders
3. I create an AvyEyes report by drawing the avalanche path perimeter on the 3D map view and entering the avalanche details (standard [SWAG](http://www.americanavalancheassociation.org/swag/) fields and free form comments), including any pictures I took of the slide
4. I submit the report and receive a confirmation email with the unique eight character report identifier
5. I then email/link/post/message/tweet/share the report with anyone on the internet using the report URL (https<nolink>://avyeyes.com/[8charUID]) so that everyone can see the avalanche online in an interactive 3D view

<br/>AvyEyes is online at https://avyeyes.com

A few example reports:

- [May 11, 2008: Coon Hill, SE Face](https://avyeyes.com/vsik4e4n)
- [May 24, 2015: Red Peak, Gore Range](https://avyeyes.com/ktqfgc3h)
- [Apr 04, 2016: Straight Creek](https://avyeyes.com/u60ektle)

##Code:

Server side code is 100% Scala running in the [Lift web framework](https://liftweb.net/) and deployed in [Apache Tomcat](http://tomcat.apache.org/). AvyEyes runs as an [AWS](https://aws.amazon.com/) Elastic Beanstalk app with a Postgres RDS instance providing persistence and S3 for image storage. Tests are written in [Specs2](http://etorreborre.github.io/specs2/) with [Mockito](http://site.mockito.org/) for mocking.

Client side code is Javascript (vanilla JS along with [jQuery](https://jquery.com/), [jQueryUI](https://jqueryui.com/), and a few jQuery plugins) organized via AMD and compiled/injected with [RequireJS](http://requirejs.org/) . The 3D view is provided by [CesiumJS](http://cesiumjs.org/). Tests are written in [Jasmine](https://jasmine.github.io/) with [Sinon](http://sinonjs.org/) and [Squire](https://github.com/iammerrick/Squire.js/) for mocking.

##Compile, Test, Run:

*Pre-step:* create `src/main/resources/props/default.props` with all the necessary config info. The `.props` files are excluded from the git repo to protect sensitive deployment keys/URLs/passwords

```
$ sbt
> compile
> test
> tomcat:start
```

AvyEyes should now be running locally at https<nolink>://localhost:8443
