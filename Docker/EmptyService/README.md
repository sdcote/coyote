# Empty Service

This is more of an example of how to create your own custom CDX images than a working example. All this images does is starts an empty CDX service running.

## Design

The `Service` is configured with only a `Wedge` component to keep the service open. No other jobs are configured.

# Building

Build the entire project first. 

Next, build the base image. 

Then build this image from this directory:

    docker build -t emptyservice .

You will have an image that will not do much of anything but take up a port.

## Usage

Just call the container:

    docker run -P emptyservice

 
## Configuration

This project can be used as a starting point for your own jobs. 

## Design Notes

None