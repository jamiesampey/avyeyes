![AvyEyes screenshot](/src/main/webapp/images/avyeyes.jpg?raw=true =800x)

**AvyEyes** is a web application for reporting and sharing avalanches in a 3D, Google Earth-like interactive view. 

The typical AvyEyes workflow is as follows:

1. I'm out backcountry skiing with my friends and I trigger an avalanche
2. I come home and go to <a href="https://avyeyes.com" target="_blank">https://avyeyes.com</a> to create an avalanche report to share with fellow skiers/riders
3. I create an AvyEyes report by drawing the avalanche path perimeter on the 3D map view and entering the avalanche details (standard <a href="http://www.americanavalancheassociation.org/swag/" target="_blank">SWAG</a> fields and free form comments), including any pictures I took of the slide
4. I submit the report and receive a confirmation email with the unique eight character report identifier
5. I then email/link/post/message/tweet/share the report with anyone on the internet using the report URL (https<nolink>://avyeyes.com/[8charUID]) so that everyone can see the avalanche online in an interactive 3D view

<br/>AvyEyes is online at <a href="https://avyeyes.com" target="_blank">https://avyeyes.com</a>

A few example reports:

- <a href="https://avyeyes.com/vsik4e4n" target="_blank">May 11, 2008: Coon Hill, SE Face</a>
- <a href="https://avyeyes.com/ktqfgc3h" target="_blank">May 24, 2015: Red Peak, Gore Range</a>
- <a href="https://avyeyes.com/u60ektle" target="_blank">Apr 04, 2016: Straight Creek</a>

##Code:

Server side code is 100% Scala running in the <a href="https://liftweb.net/" target="_blank">Lift web framework</a> and deployed in <a href="http://tomcat.apache.org/" target="_blank">Apache Tomcat</a>. AvyEyes runs as an <a href="https://aws.amazon.com/" target="_blank">AWS</a> Elastic Beanstalk app with a Postgres RDS instance providing persistence and S3 for image storage. Tests are written in <a href="http://etorreborre.github.io/specs2/" target="_blank">Specs2</a> with <a href="http://site.mockito.org/" target="_blank">Mockito</a> for mocking.

Client side code is Javascript (vanilla JS along with <a href="https://jquery.com/" target="_blank">jQuery</a>, <a href="https://jqueryui.com/" target="_blank">jQueryUI</a>, and a few jQuery plugins) organized via AMD and compiled/injected with <a href="http://requirejs.org/" target="_blank">RequireJS</a>. The 3D view is provided by <a href="http://cesiumjs.org/" target="_blank">CesiumJS</a>. Tests are written in <a href="https://jasmine.github.io/" target="_blank">Jasmine</a> with <a href="http://sinonjs.org/" target="_blank">Sinon</a> and <a href="https://github.com/iammerrick/Squire.js/" target="_blank">Squire</a> for mocking.

##Compile, Test, Run:

*Pre-step:* create `src/main/resources/props/default.props` with all the necessary config info. The `.props` files are excluded from the git repo to protect sensitive deployment keys/URLs/passwords

```
$ sbt
> compile
> test
> tomcat:start
```

AvyEyes should now be running locally at https<nolink>://localhost:8443
