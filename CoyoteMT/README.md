# CoyoteMT

This is a collection of message tools for the Coyote DX Toolkit which allow interacting with email servers for event-based processing such as:

* Reading data from email and processing it through the data exchange process
* Writing processed data to email and sending it to external actors
* Sending an email when a transformation event occurs (e.g. transform failure)

More traditional systems can be written:

* Batch email systems sending updates on a regular basis
* Auto responding systems listening for new emails, sending responses, and routing to internal address
* Automated email gateways, receiving mails and making API calls based on evaluation rules

Much of the heavy lifting is handled by the Java Mail API and these tools represent Readers, Writers, Listeners and such to make email exchanges a simple matter of adding a section to your configuration file.

### SMS Tools

Just like the email abstraction, SMS tasks allow easy text messaging from jobs. This can be accomplished with sending email to an SMS gateway or using specialized message tasks which connect to SMS providers using native or web service protocols.
