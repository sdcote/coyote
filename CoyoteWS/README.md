# Overview

This is a library of tools for the [Coyote DX toolkit](https://github.com/sdcote/coyote) which contributes HTTP reading and writing capabilities to your integration projects.

This project enables ReST-ful web service access for reading and writing data through web service APIs. The current use cases include reading files from an external business partner and updating records via ReSTful web service calls, retrieving data from one system via web services and generating fixed length field files for mainframe input and mass loading of CSV files into various systems via web services using both SOAP and ReST.

This library also has the ability to host web services which other systems can send data through. WebServerReaders then push the request through the pipeline to a WebServerWriter which takes the results and places them in the future object created by and blocked on by the reader (request thread). This allows the system to operate as a configurable web service implementation.

## Uses
The classes are designed to be used both in the Coyote DX Toolkit and as a generic toolkit for SOAP and ReST interactions. One team used CoyoteWS to make calling web services easier and did not use it for integrations processing. Another team used it to send web requests during development and testing of web service endpoints, setting up large batches of test data at an endpoint while it was being re-factored. This saved developers from having to compile and run CI jobs every time a new use case was conceived. They then used the results to create the final integration and system tests. 

# Design

One of the design goals of this library of tools is to provide a generic set of tools which will work with synchronous and asynchronous interactions which may use a variety of transports. It is technically conceivable to use this toolkit to send and receive messages not just over HTTP, but over any transport protocol. A worker can be developed for messaging transports where the topic or queue is considered a resource and the message is an event or a request.

It is possible to stand up a secure web server to receive HTTP requests which will result in the request and any data in the body to be passed through the pipeline for processing. The writer can then send a response back. This is performed with a special handler registered with the server at a particular endpoint.

## Hierarchical Data

Web services often return complex data types and a strategy is needed to deal with these types in the context of record based components. For example, A record may contain complex type such as an "address" which is comprised of one or more "Street" attributes, a "City" and "Postal Code". A CSV writer does not have the ability to record the complex type of address, only the parent record. The same is true for relational data base writers. Additionally, filters, validators and transformers will become far more complex to handle hierarchical and will make configuration of those components more complex as well.

The current approach is to "normalize" hierarchical data into a flat record name using dotted notation. Each tier of the hierarchy will contribute its own name to the parent name. In the above example, a person with an address may have the following fields:
* FirstName
* LastName
* Address.Street1  
* Address.Street2  
* Address.City
* Address.PostalCode
* MobileNumber
* OfficeNumber

The above has the complex type of address represented in the record using a single name for the attribute, but the notion of the hierarchy is preserved.

Components which understand and can deal with complex data types can easily marshal data back into the hierarchy for processing and those that cannot, can still handle the flat record. Components can remain relatively simple to configure, operate and code.    
  
