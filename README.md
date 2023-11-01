[![Build Status](https://api.travis-ci.org/prebid/prebid-mobile-android.svg?branch=master)](https://travis-ci.org/prebid/prebid-mobile-android)

# Prebid Mobile Android SDK

To work with Prebid Mobile, you will need access to a Prebid Server.
See [this page](https://docs.prebid.org/prebid-server/overview/prebid-server-overview.html) for options.

## Use Maven?

You can easily include the Prebid Mobile SDK using Maven. If your build script is Groovy-based, add this line to your gradle dependencies:

```
implementation 'org.prebid:prebid-mobile-sdk:2.1.6'
```

If your build script uses Kotlin DSL instead, add this line to your gradle dependencies:

```
implementation("org.prebid:prebid-mobile-sdk:2.1.6")
```

## Build from source

After cloning this repository, run the following command from the root directory to create the lib jar and package for your app:

```
scripts/buildPrebidMobile.sh
```

## Test Prebid Mobile

Call the test script to run unit tests and integration tests.

```
scripts/testPrebidMobile.sh
```
