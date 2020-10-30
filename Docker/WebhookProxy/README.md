# Webhook Proxy

Webhooks often requires some transformation before they can work with a particular integration. For example, Prometheus 
alerts can be sent via webhooks but not all endpoints know how to parse a Prometheus Alert Manager request. This image 
is a starting point for providing a simple webhook proxy to convert webhook request between systems.

## Design

An `HttpListener` is configured as a reader to accept the webhook requests. A `WebServiceWriter` is used to make the 
delegated request to the configured endpoint.

# Building

Build the entire project first. Then build the base image. Then build this image from this directory:

    docker build -t webhookproxy .

You will have an image from which you can build multiple webhook proxy containers.

## Usage

Just call the container

    docker run -d -p80:80 -p55290:55290 --name whproxy webhookproxy

 This will start a webhook proxy listening on port 80.
 
## Configuration

The configuration file can support multiple `Job` definitions, each listing on a separate endpoint. This allows one 
Coyote instance to support multiple endpoints. One endpoint can be set to `/webex` and integrate WebEx Teams, another 
can be set to `/teams` and integrate Microsoft Teams while a third can be set to `/slack` and support Slack integration.
