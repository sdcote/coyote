
# Coyote Batch

This is a lightweight toolkit for performing basic ETL tasks.

The framework is single-threaded for simplicity and ease of debugging. If a more high performance, multi-threaded platform is desired, there are many from which to choose. This is designed for basic file processing in a small footprint package.

## Documentation

There is a [project wiki](https://github.com/sdcote/batch/wiki) with is constantly updated with the latest information about the toolkit, how to use it, examples and best practices. It is the primary source for infromation on the toolkit.

## Development Status

This library is currently past prototyping in initial development and well into testing. Documentation is being generated and the code is in use by integration teams uncovering issues for resolution and new use cases for toolkit expansion.

No broken builds are checked-in, so what is in this repository should build and provide value. This project is indirectly supporting integration efforts and is being tested using real world scenarios. As new use cases are discovered the toolkit is updated to support them. 

This code is not currently in production, but supporting development and implementation activities. **Do Not Trust This Code Without First Checking It Yourself.** 

Feel free to copy whatever you find useful and please consider contributing so others may benefit as you may have. 

## Project Goals

This project has a simple goal: make executing basic batch processing quick and simple.

 * Configuration file based, for easy operation of many different tasks,
 * Support command-line operations with a minimum of coding,
 * Do not require complicated frameworks or facilities (e.g. containers)
 * Enable integrations prototyping, and development operations
 * Provide utilities to assist in the loading and extraction of data,
 * Simple Configuration; if we need a GUI to configure the tools, we are not simple,
 * Provide value first, optimize later.

## Prerequisites:

  * JDK 1.7 or later installed
  * Ability to run bash (*nix) or batch (Windows) scripts
  * Network connection to get the dependencies (there are ways around that)
  * Assumes you do not have gradle installed (if you do, you can replace `gradlew` with just `gradle`)
