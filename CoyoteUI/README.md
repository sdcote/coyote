# Overview

_**Under Development**_

This is a library of HTTP handlers for the [Coyote DX toolkit](https://github.com/sdcote/coyote) which allows user interaction with a running service. This allows for additional commands to be registered with the toolkit which are invokable via secured web pages. 

# Design

The Coyote DX service looks for a static fixture in the class path which acts as a factory for the management server. If it is found, it is created and started.

If a fixture is not found, the default manager is used which gives only basic operational monitoring of the service.
