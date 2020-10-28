# Base Image

This is the base container image for Coyote. Nearly all other images start from this base.

This contains all the Coyote modules so it can be used for all your jobs. If image size is an issue, you can use this project as a template for your custom images.

## Building

Change into the `Base` directory and run a build:

    docker build -t coyote .

All the other images will use a `FROM coyote` so keep the name consistent, otherwise you will need to change the 
`Dockerfile` in the other projects.

## Extending

Other images can be built from this base image and contribute only the (configuration) files necessary.

## Configuration

This image runs an empty Coyote instance listening on port 55290. 

## Examples

Assuming the image name of "coyote" 

Generate/encode an encrypted string
    $ docker run coyote encrypt "my secret text"

Generate/encode an encrypted string using a specific key and encryption
    $ docker run -e cipher.key='5up3rS3cret' -e cipher.name='xtea' coyote encrypt "my secret text"

Run a job configuration using a specific encryption key
    $ docker run -e cipher.key='5up3rS3cret' coyote http://someplace.com/cfg/myjob.json

