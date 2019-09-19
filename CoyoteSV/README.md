# CoyoteSV
A simple, small, portable secrets manager with strong encryption.

## Overview
Coyote Secrets Vault allows jobs to be free of any passwords by specifying an encrypted password file as the source of your secrets. Jobs simply reference the identifier of the secret by using the built-in templating system.

It has a simple GUI interface allowing you to manage multiple secrets vaults and can even serve as a stand alone application to store user names, passwords, URLs and free-form notes in an encrypted file protected by one master password.

Features:
* Strong encryption - AES-256-CBC algorithm (SHA-256 is used as password hash).
* Portable - single jar file which can be carried on a USB stick.
* Built-in random password generator.
* Organize all your user name, password, URL and notes information in one file.
* Data import/export in JSON format.
* Accessible from within any Coyote data transfer job.
* Usable as a stand-alone password manager.

This package contains only the wrapper for the CoyoteVault project which is available from the Jcenter repository.
