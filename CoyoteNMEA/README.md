# Coyote NMEA 0183

This project contains tools for parsing NMEA Sentences which come from a variety of sensors such as GPS, Depth Sounders and other transducers.

### Completely Optional
This set of libraries is not required to run the Coyote DX tools or framework. Install this library when you want your components to have the ability to interact with NMEA devices.

## Design
There are Sentence interfaces and implementations which are registered with a `SentenceParser`. It is possible to add new NMEA sentences to the parser at runtime so you can create your own library of proprietary sentences.

The `SentenceParser` uses a listener pattern allowing the the registering of one or more `SentenceListener` components. When the `SentenceParser` reads a valid NMEA sentence, it is passed to one or more registered listeners depending on the type (i.e. `SentenceId`) to which the listener is registered to receive. It is possible to register listeners to receive all parsed sentences or only particular types.

It is expected that NMEA listeners will update the operational context of the runtime which is shared with other components. This allows NMEA listeners to publish data asynchronously to other components in a decoupled manner.  

There is a `SentenceReader` which accepts an input stream which uses a buffered reader to read line and pass them to the parser. Because buffered readers tend to block until a full line is read and that may not be desirable in some situations, there is also a `SentenceMonitor` which runs a `SentenceReader` in a separate thread.

The `SentenceReader` uses an abstract `DataReader` which can be used to read from an `InputStream` (default), or message frameworks any of a number of sources. The goal is to allow a Monitor to receive sentences from any transport capable of sending string data.

## Use Cases
The primary use case is to create a managed component which connects to a serial port, and runs a `SentenceMonitor` in the background which then receives NMEA sentences from the serial port and passes them to one or more listeners. The `ManagedComponent` then manages the life cycle of the listeners and monitor to keep everything running smoothly. The listeners update the operational context with the current reading so other components can access data retrieved by the listeners. When the Coyote Framework shuts down, the managed component cleanly terminates the monitor and listeners.

Of course, this library can be used outside of the Coyote DX framework and the `SentenceReader` and `SentenceMonitor` can be used in many other ways.

## Roadmap
Currently the NMEA library is under development and does not support all NMEA 0183v3 sentences yet.

This is currently being used to read NMEA sentences from our GPS components and a couple of old devices we are using for PoC and prototyping. New sentences will be added as we need them.

Future support for a `SentenceWriter` will make it simple to `write(Sentence)` to any transport medium without having to change any code. The `DataWriter` can be chosen at runtime allowing a component to be configured to write sentences to any transport without recompilation.
