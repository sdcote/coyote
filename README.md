
# Coyote DX
[![Build Status](https://travis-ci.org/sdcote/coyote.svg?branch=develop)](https://travis-ci.org/sdcote/coyote) [![codecov](https://codecov.io/gh/sdcote/coyote/branch/develop/graph/badge.svg)](https://codecov.io/gh/sdcote/coyote) [![Code Climate](https://codeclimate.com/github/sdcote/coyote/badges/gpa.svg)](https://codeclimate.com/github/sdcote/coyote)


This is a lightweight toolkit for performing basic data exchange (integration) tasks.

The goal has evolved into creating a data exchange tool along the line of build tools like `Maven`, `Gradle` and `Ant`, where a configuration file (`pom.xml`, `build.gradle` and `build.xml` respectively) is written and run (`mvn`, `gradle` and `ant` respectively). The result being data read from one system and written to another.

Using CoyoteDX, it is possible to craft an "exchange" file (e.g. newcontacts.json) and calling Coyote DX to run that exchange (e.g. `cdx newcontacts.json`). No coding necessary; all the components are either contained in Coyote DX or a library dropped into its path.

The primary use case involves running this "exchange" as required, most likely in cron, a scheduler, or a dedicated service running exchanges on the host. An exchange is run every 15 minutes for example. This models the well known batch integration pattern.

A related use case involves the exchange running continually as a background process executing when some event occurs. The exchange blocks until the event is detected at which time the exchange processes the event. For example, the exchange job may wait until new record has been added to the source system and when that record is detected, it is read in and passed through to the destination system(s). In this manner, it operates less like a batch exchange and more like a real time exchange handling time sensitive data as it becomes available.

So far, it has been useful in integrating applictions, connecting field devices to the cloud, performing load, performance, and integration testing, modeling new data exchanges, keeping systems in-synch during migrations, testing service APIs and connecting hardware prototypes to test systems. This tool is helpful in more cases than integration; is can be used to exchange data between any network connected system actor. One application involved monitoring systems, regularly polling system metrics and writing events when metrics exceeded threshholds.

New components are being added regularly and its design supports third-party contributions without recompilation. Just add your library to the path and your "exchange" file can reference the new components in that library.

## Documentation

There is a [project wiki](https://github.com/sdcote/coyotedx/wiki) with is constantly updated with the latest information about the toolkit, how to use it, examples and best practices. It is the primary source for infromation on the toolkit.

## Development Status

This library is currently past prototyping in initial development and well into testing. Documentation is being generated and the code is in use by integration teams uncovering issues for resolution and new use cases for toolkit expansion.

No broken builds are checked-in, so what is in this repository should build and provide value. This project is indirectly supporting integration efforts and is being tested using real world scenarios. As new use cases are discovered the toolkit is updated to support them. 

Feel free to copy whatever you find useful and please consider contributing so others may benefit as you may have. 

## Project Goals

This project has a simple goal: make executing data exchange jobs quick and simple.

 * Configuration file based, for easy operation of many tasks (i.e "exchange" file),
 * Support command-line operations with no coding (just an "exchange" file),
 * Do not require complicated frameworks or facilities (e.g. containers)
 * Enable integrations prototyping, and development operations
 * Provide utilities to assist in the reading, writing and transformation of data,
 * Simple Configuration; if we need a GUI to configure the tools, we are not simple enough.
