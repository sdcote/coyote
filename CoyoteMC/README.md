# CoyoteMC

This is a metric collection toolkit for Coyote DX which allows the creation an processing of metrics. Use cases include the following:

* Monitor web sites (and services) for client-side performance metrics,
* Checking if infrastructure is operational, such as databases are available,
* Determining the response times for critical API and service calls.

This toolkit support making metrics available to Prometheus Time Series Data Base instances for alerting and reporting in a larger Application Performance Monioring (APM) solution.

It is expected that this will also feed many other APM solutions by providing a way to perform customized monitoring of infrastructures and systems.

## Selenium
This toolkit uses Selenium to interact with Websites. The choice of Selenium is motivated by the desire to collect [W3C Navigation Timing](https://www.w3.org/TR/navigation-timing/) metrics from pages, not simply server file transfer times. Web page metrics should reflect the user experience which is more than simply retrieving the root page from server. Metrics should reflect the processing of scripts included in the page and all the resources required to render the page as implemented by the designer. While this places more libraries in the installation, the amount of useful information gathered from the browser makes the extra dependencies acceptable.

By using creating a browser instance and instructing it to load a page, it is possible to collect all the metrics and timings involved in loading the page including, DNS lookups, all the redirects, SSL/TLS negotiation, page load and unload times and DOM processing.  

The use of Selenium also opens the door to performing a few more checks and extracting data from the page through the use of well-documented selectors. This means a sophisticated page which relies on scripting to manipulate the DOM can be successfully processed and data extracted from the resulting DOM. For example, many pages include server processing time embedded in the final page. These metrics can be "scraped" from the rendered page to retrieve back office processing data to the metric collection process. Metric data can be tagged and correlated at a later time for more accurate reporting. For example; each page may contain a server name which generated it. This server name can be extracted from the page and placed in the metric stream allowing the response time of servers to be tracked.    

The goal is to enable the collection of the metrics and data which represent actual operation in a browser, not just page download times which can be misleading particularly when many resources are required to be downloaded and processed along with any AJAX-style calls a page may require to be completely rendered. This allows for the collection of metrics which most accurately represents user experience.

## OpenMetrics
This project supports the [OpenMetrics](https://openmetrics.io/) text format. Metrics can be exposed via web services, written to a file or pushed to an external web service. The initial goal is to support the popular [Prometheus](https://prometheus.io/) time series database along with its internal [AlertManager](https://prometheus.io/docs/alerting/alertmanager/) and the well-known [Grafana](https://grafana.com/) open-source dashboard system. There are many examples of installing a fully operational, production-ready monitoring system in a few minutes using both products. Combined with CoyoteMC, it is possible to begin the detailed monitoring of your systems in literally under an hour.

## Metric Format
CoyoteDX is a record-based processing system. Each record contains a set to key-value pairs which are processed by multiple components in a pipeline. Each record is expected to have a unique name so it is unambiguously addressable and each value is expected to have a single primitive type, including a character string.

### Metric Name
All metrics have a name. The value in the `name` field is the name of the metric.

### Metric Value
All metrics have a value. The value in the `value` field is the value of the metric.

### Metric Job
The Coyote platform is a batch processing system. A job runs at some point in time and terminates when all the records are processed. Coyote jobs don't normally last long enough for a monitoring system to poll it for metrics. The `Writer` "pushes" data to the monitoring system representing the results of the job.

If a metric contains a `job` field, the value in that field will be used as the name of the job. If not job field is specified, the `Writer` will assume the name of current job.

### Metric Tags
A metric may have one or more additional name-value pairs. These fields represent metadata about the metric. For example, a field name of `env` may represent the environment of the resource being monitored. It value may be "development", "test" or "production". Another field may be named `server` and represent the server on which resource is being monitored is located.

Consider the following JSON representation of a metric record in the CoyoteDX transformation context:
```json
{
  "name":"http_requests_total",
  "value":8264,
  "instance":"pod22",
  "env":"production"
}
```
The above metric can be read as there are currently 8,264 total HTTP requests on production server instance "pod22". filed other than `name` and `value` are considered tags and used to generate a more complete description of the metric value. The following is an OpenMetric format of the above example:
```
http_requests_total{instance="pod22",env="production"} 8264
```

## Metric Readers
A metric `Reader` component performs an operation and generates a series of metrics based on that operation. Each metric is a separate record passed through the transformation pipeline. When the `Reader` sends the last of the metrics, it sets its "End of File" flag (`isEof()`) to true and the transformation engine sets the `isLastRecord` flag to true indicating the metric samles are complete.

When components notice the last of the metrics have been sent through the pipeline, components then perform processing on the set of metrics as a collection. A `Writer`, for example, may collect all the metric records and send the entire batch to a Time Series Data Base for storage.

## PushGatewayWriter
This writer operates like the `WebServiceWriter` but for calculating the service path based on contents of the record and not using a protocol section since the protocol is dictated by the Push Gateway service. 

The `target` configuration parameter points to the host and port of the Prometheus Push Gateway. It is assumed that metrics will be sent to the gateway via HTTP POST operation and the payload will be OpenMetric text as described above. Each record received by the writer will potentially result in an HTTP POST to the `target` web service.

A `fields` section describe how the individual fields of records are converted into individual metrics. The name of the field is the matcher for the name of the record to be written. Once matched, the `help` and `type` fields are used to craft the OpenMetric.

At present, only `counter` and `gauge` are supported. The `histogram` and `summary` metric types are currently under design and development.

All other fields in the record are treated as labels with the name of the field being treated as the name of the label and the value of the field being treated as the label value. These additional fields are also used to calculate the service endpoint called. Be sure to create the appropriately formatted record in you writer or "transform" the record in the pipeline to meet your needs.

Labels should include the `job` field as this is required by the push gateway. If omitted `coyote` will be used.  See the Push Gateway [README](https://github.com/prometheus/pushgateway/blob/master/README.md#url) for more details on the required URL for pushing metrics.