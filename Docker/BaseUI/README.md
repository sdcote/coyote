# Base Image with Browser Interface

This is more of an example of how to create your own custom CDX images than a working example. All this images does is starts an empty CDX service running.

## Design

The `Service` is configured with only a `Wedge` component to keep the service open. No other jobs are configured.

# Building

Build the entire project first.

Next, build the base image.

Then, from the base of the project, run `docker build`:

    docker build -f Docker/BaseUI/Dockerfile -t cdxui .

Other images may use a `FROM cdxui` so keep the name consistent, otherwise you will need to change the `FROM`
directive in the `Dockerfile` in the other projects.

You will have an image that will not do much of anything but take up port 55290 and respond to a browser on that port.

## Usage

Just call the container:

    docker run -d -p 55290:55290 cdxui

Point your browser to http://localhost:55290, and you should be presented with the CUI.

## Configuration

This project can be used as a starting point for your own images that need a user interface.

## Design Notes

None