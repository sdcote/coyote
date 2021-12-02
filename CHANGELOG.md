# Change Log
All notable changes to this project will be documented in this file.

The basic format includes a section for each release named after the version number and the date (DD/MM/YYYY) of the release. Each section contains a brief description  and the following subsections:
* **Added** for new features. 
* **Changed** for changes in existing functionality. 
* **Deprecated** for soon-to-be removed features. 
* **Removed** for now removed features. 
* **Fixed** for any bug fixes. 
* **Security** in case of vulnerabilities. 

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.8.7] - Unreleased
### Added
* **Job-level loggers** - These loggers are added to and removed from the logging sub-system for the duration of the job only. Relative targets default to the job directory and not the `[app.home]/log` directory.
* **Prometheus Monitoring** - Added an [OpenMetrics](https://openmetrics.io/) endpoint (`/metrics`) that Prometheus can use to collect `Service` operational and health metrics.
* **RunJob Listener** - This listener will kick off jobs after during the `onMap()` event passing target frame fields in the symbol table of the called jobs. This allows a reader to read records from a source, place the fields of the record in the transform context, and execute a set of jobs for each record, each using data in the context to configure its operation.
* **CheckSymbolNotNull** - This task halts the job if the configured symbol in the transform context symbol table is null, empty ("") or blank.
* **LogFileReader** - Reader that reads text log files and creates data frames. Currently, Apache log formats are supported but the ability to specify a `freeform` format is under development.
* **EventProfiler** - A listener that tracks events (anything with a timestamp field) and plots metrics. Can be used as a simple log analyzer.
* **Secure Copy (SCP) Support** - The `Retrieve` task now supports SCP file transfer.

### Fixed
* **JdbcWriter NPE** - Fixed the issue with the prepared statement not being (re)created when the `Job` is closed and later re-opened as is the case with `Job` instances running in a `Service`.
* **JdbcWriter ALTER** - Updated the Database dialect to support ALTER statements.
* **EventProfiler NPE** - Fixed the NPE encountered for some records.
* **Retrieve Task Relative Files** - All relative local files now go into the job directory as is standards for components.

## [0.0.1] - 15/08/2019
This release contained initial functionality to enable development of external providers.

### Added
- Basic project structure
- Unit Testing
- Static Code Analysis
- Design Decisions document