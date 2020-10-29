# Base Image

This is the base container image for Coyote. Nearly all other images start from this base.

This contains all the Coyote modules so it can be used for all your jobs. If image size is an issue, you can use this project as a template for your custom images.

## Building

Make sure you have built the entire project firt. , Just go to the base of the project directory and run the `gradle` command.
Go to the base of this project and execute:

    docker build -f Docker/Base/Dockerfile -t coyote .

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


# Debugging Containers

Here are a few tricks to help with debugging your containers.

## Override the Entrypoint

Run a container and get a command shell by overriding the entry point:

    docker run -it --rm --entrypoint sh coyote


## Running Stopped Containers

Sometimes your containers stop without a clear reason. To help debug this situation, just commit the stopped container 
as an image and run that image by overriding the entrypoint:
```
# Commit the stopped image
docker commit 0dfd54557799 debug/base

# create a new container from the "broken" image
docker run -it --rm --entrypoint sh debug/base

# inside of the container we can inspect the file system
/ # ls -las /opt/coyotedx
total 20
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 .
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 ..
     4 drwxr-xr-x    2 root     root          4096 Oct 29 14:19 bin
     4 drwxr-xr-x    2 root     root          4096 Oct 28 15:26 cfg
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 lib
/ #
```
Keep in mind that any file changes in this container will be lost.
