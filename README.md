ZIO Fullstack
=============

* Push release to heroku? aws fargate?
  * host under ziofullstack.markkegel.com
* Dockerize
* zio-app?
* source maps
* Javascript logging
* frontend tests
* re-org sbt settings, imports
* docs site, full SPA, boilerplay?
  * zio metrics
  * tracing

* put together modern SPA docs
* zio-test integration


# Packaging and Release

For now our release and package process is fairly straightforward:

1. Run 'sbt release' from the command line in the master branch

# Services

## Api Token

Generates API tokens for users to hit the SaaS with.

# Tech Stach

* TSec to add login / authentication using JWT
* ScalaJS+React+Bootstrap front end
* HTTP4S back end

# Development Loop

Start up sbt:

```
> sbt
```

Once sbt has loaded, enter the server project and start up the application

```
> project server
> ~reStart
```

This uses revolver, which is a great way to develop and test the application.  Doing things this way the application
will be automatically rebuilt when you make code changes

To stop the app in sbt, hit the `Enter` key and then type:

```
> reStop
```
