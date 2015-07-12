
# Coyote Batch

This is a lightweight framework for performing basic ETL tasks.

The framework is single-threaded for simplicity and ease of debugging. If a more high performance, multi-threaded platform is desired, there are many from which to choose. This is designed for basic file processing in a small footprint package.

## Development Status

This library is currently in the prototyping stage and is undergoing constant change and redesign.

No broken builds are checked-in, so what is in this repository should build and provide value. **Do Not Trust This Code Without First Checking It Yourself.**  

Feel free to copy whatever you find useful and please consider contributing so others may benefit as you may have. 

## Project Goals

This project has a simple goal: make executing basic batch processing quick and simple.

 * Configuration file based, for easy operation of many different tasks,
 * Support command-line operations with a minimum of coding,
 * Enable integrations prototyping,
 * Provide utilities to assist in the loading and extraction of data,
 * Provide value first, optimize later.

## Prerequisites:

  * JDK 1.6 or later installed
  * Ability to run bash (*nix) or batch (Windows) scripts
  * Network connection to get the dependencies (there are ways around that)
  * Assumes you do not have gradle installed (if you do, you can replace gradlew with gradle)
