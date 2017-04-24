
# Coyote DX

This is a lightweight toolkit for performing basic data exchange (integration) tasks.

The goal has evolved into creating a data exchange tool along the line of a build tool similar to `Maven`, `Gradle` and `Ant`, where a configuration file (`pom.xml`, `build.gradle` and `build.xml` respectively) is written and run (`mvn`, `gradle` and `ant` respectively). The result being data read from one system and written to another.

Using CoyoteDX, it is possible to craft an "exchange" file (e.g. newcontacts.json) and calling Coyote DX to run that exchange (e.g. `cdx newcontacts.json`). No coding necessary; all the components are either contained in Coyote DX or a library dropped into its path.

So far, it has been useful in integrating applictions, connecting field devices to the cloud, performing load, performance, and integration testing, modeling new data exchanges, testing service APIs and connecting hardware prototypes to test systems. This tool is helpful in more cases than integration; is can be used to exchange data between any network connect actor.

New components are being added regularly and its design supports third-party contributions without recompilation. Just add your library to the path and your "exchange" file can reference the new components in that library.

## Documentation

There is a [project wiki](https://github.com/sdcote/coyotedx/wiki) with is constantly updated with the latest information about the toolkit, how to use it, examples and best practices. It is the primary source for infromation on the toolkit.

## Development Status

This library is currently past prototyping in initial development and well into testing. Documentation is being generated and the code is in use by integration teams uncovering issues for resolution and new use cases for toolkit expansion.

No broken builds are checked-in, so what is in this repository should build and provide value. This project is indirectly supporting integration efforts and is being tested using real world scenarios. As new use cases are discovered the toolkit is updated to support them. 

Feel free to copy whatever you find useful and please consider contributing so others may benefit as you may have. 

## Project Goals

This project has a simple goal: make executing data exchange jobs quick and simple.

 * Configuration file based, for easy operation of many different tasks (i.e "exchange" file),
 * Support command-line operations with no coding (just an "exchange" file),
 * Do not require complicated frameworks or facilities (e.g. containers)
 * Enable integrations prototyping, and development operations
 * Provide utilities to assist in the reading, writing and transformation of data,
 * Simple Configuration; if we need a GUI to configure the tools, we are not simple enough.
