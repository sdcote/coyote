# Overview



## Installer

The project uses [IZPack](http://izpack.org/) for cross-platform installations. The installer configuration is located in the `<projectRoot>/installer` directory.

To build a new installer for Coyote run the following:

    <pathTo>\IzPack\bin\compile.bat installer\install.xml -b . -o CDXInstaller.jar -k standard

The self-executing JAR (`CDXInstaller.jar`) Will be in the root of the project.

BTW, Use the `compile` if you are running on a *nix machine.

## Docker 

From the base directory after everything has compiled, create the base Docker image:

```shell
docker build -f Docker/Base/Dockerfile -t cdx .
```

Then tag it:

```shell
docker tag cdx coyotesys/cdx:0.8.7

```

Then push it:

```shell
docker push coyotesys/cdx:0.8.7
```

## Version Numbers

Versioning is currently managed by modules. Each module has its own versioning.

When CDX is released, it will contain a group of modules of with their own separate versions.

1. Confirm the versions in the following files as appropriate:
   * installer/install.xml - The same version as the base DX package.
   * CoyoteFT/src/main/java/coyote/dx/CDB
   * CoyoteDX/src/main/java/coyote/dx/CDX - The same version as the installer.
   * CoyoteMQ/src/main/java/coyote/dx/CFT
   * CoyoteMQ/src/main/java/coyote/dx/CMC
   * CoyoteMQ/src/main/java/coyote/dx/CMQ
   * CoyoteFT/src/main/java/coyote/dx/CMT
   * CoyoteFT/src/main/java/coyote/dx/CUI
   * CoyoteWS/src/main/java/coyote/dx/CWS
2. Update the [Release Notes](ReleaseNotes.md).
   * Add the version number of the DX base module (and installer)
   * Follow the guidelines in [Keep A Change Log](https://keepachangelog.com/en/1.0.0/).

Be sure to manage the versions of each of the modules when changes are made. If only a fix is being performed, update 
the **patch*. If functionality is being added, update the **minor** version. If this is a breaking change, update the 
**major** version.
