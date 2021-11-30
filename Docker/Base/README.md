# Base Image

This is the base container image for CDX. Nearly all other images start from this base.

This contains all the CDX modules, so it can be used for all your jobs. If image size is an issue, you can use this project as a template for your custom images.

## Building

Make sure you have built the entire project first. Just go to the base of the project directory and run the `gradle` command.

Next, from the base of the project, run `docker build`:

    docker build -f Docker/Base/Dockerfile -t cdx .

All the other images will use a `FROM cdx` so keep the name consistent, otherwise you will need to change the `FROM` 
directive in the `Dockerfile` in the other projects.

## Pushing

Tag the newly created image with host, path, and version tags:

    docker tag cdx docker.io/coyotesys/cdx:0.8.6

Then push:

    docker push docker.io/coyotesys/cdx:0.8.6


## Extending

Other images can be built from this base image and contribute only the (configuration) files necessary. Here is an example `Dockerfile` that 

```Dockerfile
FROM cdx
COPY myjob.json /opt/cdx/cfg
CMD ["myjob"]
```
The above starts with the base CDX image and copies the job configuration file to the base configuration directory. Finally, it calls the entrypoint with the name of the job configuration file. Running this container will just call the CDX job included in the image.

### Custom Classes

Just like the full project, you can add custom classes to the toolkit by copying their JAR or class files to the `lib` directory:

```Dockerfile
FROM cdx
COPY myjob.json /opt/cdx/cfg
COPY custom.jar /opt/cdx/lib
CMD ["myjob"]
```

The above will allow all the classes and resources in the `custom.jar` file to be included in the runtime classpath. You can write your own tasks, transforms, and any other framework component and call it from within your jobs. 

## Configuration

There is no configuration included with the base image. You must extend the base image to include configurations or use volumes to mount a configuration directory as described below.

## Examples

This is the base image of the Coyote toolset. It contains no useful configurations, and is designed to be mounted to a volume:

    docker run -v c:/jobs/:/opt/cdx/cfg cdx somejob

The above mounts the `c:/jobs/` directory on your local host to the configuration directory for CDX (`/opt/cdx/cfg`) and then runs the `somejob` job configuration found in that directory.

The working directory of the container can be mounted in a similar manner. This allows for the results of processing to be accessed locally:

    docker run -v c:/work/:/opt/cdx/wrk -v c:/jobs/:/opt/cdx/cfg cdx somejob

The same is true for the log directory:

    docker run -v c:/logs/:/opt/cdx/log -v c:/work/:/opt/cdx/wrk -v c:/jobs/:/opt/cdx/cfg cdx somejob

If you are on a unix system, you can set an alias to keep everything tight:

    alias cdx='docker run -v ~/cdx/logs/:/opt/cdx/log -v ~/cdx/work/:/opt/cdx/wrk -v ~/cdx/jobs/:/opt/cdx/cfg cdx'

The above allows you to run Dockerized CDX like it was installed on your system:

    docker run -v ~/cdx/logs/:/opt/cdx/log -v ~/cdx/work/:/opt/cdx/wrk -v ~/cdx/jobs/:/opt/cdx/cfg cdx

If you are on Windows, you can simply write a script/batch file (e.g. `cdx.ps1`, `cdx.bat`, or `cdx.cmd`) and place it in your path.  Something like the following:

    CMD_LINE_ARGS=%$
    docker run -v ~/cdx/logs/:/opt/cdx/log -v ~/cdx/work/:/opt/cdx/wrk -v ~/cdx/jobs/:/opt/cdx/cfg cdx %CMD_LINE_ARGS%

The syntax will differ depending on the scripting approach used and the version of the operating system.

### Other Tasks

Generate/encode an encrypted string for use in your configurations:

    docker run cdx encrypt "my secret text"

**Note:** the above should be considered obfuscated and _not_ encrypted since the default, easily accessible key is used.    

This is an example that uses a provided secret key and named encryption algorithm:

    docker run -e cipher.key=5up3rS3cret! -e cipher.name=XTEA cdx encrypt "my secret text"

Run a job configuration retrieved from the network and using a specific encryption key:

    docker run -e cipher.key=5up3rS3cret! cdx http://someplace.com/cfg/myjob.json

# Debugging Containers

Here are a few tricks to help with debugging your containers.

## Override the Entrypoint

Run a container and get a command shell by overriding the entry point:

    docker run -it --rm --entrypoint sh cdx

## Running Stopped Containers

Sometimes your containers stop without a clear reason. To help debug this situation, just commit the stopped container 
as an image and run that image by overriding the entrypoint:
```
# Commit the stopped image
docker commit 0dfd54557799 debug/base

# create a new container from the "broken" image
docker run -it --rm --entrypoint sh debug/base

# inside of the container we can inspect the file system
/ # ls -las /opt/cdx
total 20
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 .
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 ..
     4 drwxr-xr-x    2 root     root          4096 Oct 29 14:19 bin
     4 drwxr-xr-x    2 root     root          4096 Oct 28 15:26 cfg
     4 drwxr-xr-x    1 root     root          4096 Oct 29 14:19 lib
/ #
```
Keep in mind that any file changes in this container will be lost.
