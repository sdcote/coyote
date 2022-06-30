# Docker Images

These are the starting points for different container images you may find useful in employing Read-Transform-Write (RTW)
components in your systems.

## Base
This is a simple base container that includes CDX and an empty `daemon.json` that runs an empty service configuration.

## BaseCert
Often, an organization will use self-signed certificates to protect their network connections. This is an example of how to install a root certificate so CDX can make secure connections to an organizations protected network services.

## EmptyService
This is more of an example of how to create your own custom CDX images than a working example. All this images does is starts an empty CDX service running. You can update the configuration with anything you need.

## WebhookProxy
This example shows how to convert AlertManager webhook calls into other formats. In this example, it supports Microsoft Teams and Webex Teams.

