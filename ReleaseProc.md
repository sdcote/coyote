## Version Numbers

Versioning is currently managed by modules. Each module has its own versioning.

When Coyote DX is released, it will contains a group of modules of with their own separate versions.

Change the versions in the following files as appropriate:

* installer/install.xml
* CoyoteDX/src/main/java/coyote/dx/CDX 
* CoyoteMQ/src/main/java/coyote/mq/CMQ
* CoyoteFT/src/main/java/coyote/ft/CFT
* CoyoteWS/src/main/java/coyote/ws/CWS
* Loader/src/main/java/coyote/loader/Loader

# Installer

The project uses [IZPack](http://izpack.org/) for cross-platform installations. The installer configuration is located in the `<projectRoot>/installer` directory.

To build a new installer for Coyote run the following:

    <pathTo>\IzPack\bin\compile.bat installer\install.xml -b . -o CoyoteInstaller.jar -k standard

The self-executing JAR (`CoyoteInstaller.jar`) Will be in the root of the project.

BTW, Use the `compile` if you are running on a *nix machine.