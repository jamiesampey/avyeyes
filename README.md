![AvyEyes screenshot](/public/images/avyeyes.jpg?raw=true)

**AvyEyes** is a web application for reporting and sharing avalanches in a three-dimensional, interactive view. 

The typical AvyEyes use case is as follows:

1. Backcounty Bob is out skiing with his friends (in the backcountry of course) and triggers an avalanche
2. Bob comes home and goes to http://avyeyes.com where he creates an avalanche report by drawing the avalanche path perimeter in the 3D view and entering the avalanche details (standard [SWAG](http://www.americanavalancheassociation.org/swag/) fields and free form comments), including pictures he took of the slide
3. Bob submits the report and receives a confirmation email with the unique eight-character report identifier
4. Bob then shares the unique report URL (`http://avyeyes.com/8charUID`) with anyone he wants to inform of the slide

<br/>AvyEyes is online at http://avyeyes.com

A few example reports:

- [May 11, 2008: Coon Hill, SE Face](http://avyeyes.com/vsik4e4n)
- [May 24, 2015: Red Peak, Gore Range](http://avyeyes.com/ktqfgc3h)
- [Apr 04, 2016: Straight Creek](http://avyeyes.com/u60ektle)

## The Code:

Server-side API code is written in [Scala](http://www.scala-lang.org/) and runs in the [Play Framework](https://www.playframework.com/). Tests are written in [Specs2](http://etorreborre.github.io/specs2/) with [Mockito](http://site.mockito.org/) for mocking and [ScalaCheck](http://www.scalacheck.org/) for test object generation.

The AvyEyes client is a pure ES6 Javascript [React](https://reactjs.org/) project which makes heavy use of Google's [Material-UI](https://material-ui.com/) library. The 3D view is provided by [CesiumJS](http://cesiumjs.org/). The AvyEyes client is an [NPM](https://www.npmjs.com/) project that uses [Babel](https://babeljs.io/) to transpile React JSX, and [Webpack](https://webpack.js.org/) to organize/build/optimize the Javascript.

AvyEyes is deployed in [AWS](https://aws.amazon.com/) as an Elastic Beanstalk app with a [Postgres](https://www.postgresql.org/) RDS instance providing persistence and S3 providing file storage.