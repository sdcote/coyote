# Webhook Proxy

Webhooks often requires some transformation before they can work with a particular integration. For example, Prometheus 
alerts can be sent via webhooks but not all endpoints know how to parse a Prometheus Alert Manager request. This image 
is a starting point for providing a simple webhook proxy to convert webhook request between systems.

## Design

An `HttpListener` is configured as a reader to accept the webhook requests. A `WebServiceWriter` is used to make the 
delegated request to the configured endpoint.

# Building

Build the entire project first. 

Next, build the base image. 

Then build this image from this directory:

    docker build -t webhookproxy .

You will have an image from which you can build multiple webhook proxy containers.

## Usage

Just call the container:

    docker run -P webhookproxy

This will start a webhook proxy listening on port 80. 

Of course, you will probably be a bit more specific with your ports and run it in the background:

    docker run -d -p80:80 -p55290:55290 --name whproxy webhookproxy


 
## Configuration

This image will include a static configuration file found in the `./opt/coyotedx/cfg` directory

The configuration file can support multiple `Job` definitions, each listing on a separate endpoint. This allows one 
Coyote instance to support multiple endpoints. One endpoint can be set to `/webex` and integrate WebEx Teams, another 
can be set to `/teams` and integrate Microsoft Teams while a third can be set to `/slack` and support Slack integration.


## Design Notes

You can test this with `curl`:

    curl -X POST -H "Content-Type: application/json" -d '{"markdown" : "This is a sample message."}' "http://P2026071.aepsc.com"

### Alert Manager
Alert Manager Webhook outgoing specification: https://prometheus.io/docs/alerting/latest/configuration/#webhook_config

```json
{
  "version": "4",
  "groupKey": <string>,              // key identifying the group of alerts (e.g. to deduplicate)
  "truncatedAlerts": <int>,          // how many alerts have been truncated due to "max_alerts"
  "status": "<resolved|firing>",
  "receiver": <string>,
  "groupLabels": <object>,
  "commonLabels": <object>,
  "commonAnnotations": <object>,
  "externalURL": <string>,           // backlink to the Alertmanager.
  "alerts": [
    {
      "status": "<resolved|firing>",
      "labels": <object>,
      "annotations": <object>,
      "startsAt": "<rfc3339>",
      "endsAt": "<rfc3339>",
      "generatorURL": <string>       // identifies the entity that caused the alert
    },
    ...
  ]
}
```

Primary Example:
```json
{
	"receiver": "webhook",
	"status": "firing",
	"alerts": [
		{
			"status": "resolved",
			"labels": {
				"alertname": "JenkinsTooSlowHealthCheck",
				"instance": "ecdp3:8088",
				"job": "jenkins",
				"monitor": "ECD",
				"quantile": "0.999",
				"severity": "notify"
			},
			"annotations": {
				"description": " ecdp3:8088 is responding too slow to the regular internal health check",
				"summary": "Jenkins responding too slow to internal health check"
			},
			"startsAt": "2020-11-11T00:09:03.509022477Z",
			"endsAt": "2020-11-11T00:16:03.509022477Z",
			"generatorURL": "http://33f48340e2bd:9090/graph?g0.expr=jenkins_health_check_duration%7Bquantile%3D%220.999%22%7D+%3E+0.01&g0.tab=1",
			"fingerprint": "42cfd1d420dda436"
		},
		{
			"status": "firing",
			"labels": {
				"alertname": "JenkinsTooSlowHealthCheck",
				"instance": "ecdt3:8088",
				"job": "jenkins",
				"monitor": "ECD",
				"quantile": "0.999",
				"severity": "notify"
			},
			"annotations": {
				"description": " ecdt3:8088 is responding too slow to the regular internal health check",
				"summary": "Jenkins responding too slow to internal health check"
			},
			"startsAt": "2020-11-11T00:18:53.509022477Z",
			"endsAt": "0001-01-01T00:00:00Z",
			"generatorURL": "http://33f48340e2bd:9090/graph?g0.expr=jenkins_health_check_duration%7Bquantile%3D%220.999%22%7D+%3E+0.01&g0.tab=1",
			"fingerprint": "4624b54e1b18a6c2"
		}
	],
	"groupLabels": [
	],
	"commonLabels": {
		"alertname": "JenkinsTooSlowHealthCheck",
		"job": "jenkins",
		"monitor": "ECD",
		"quantile": "0.999",
		"severity": "notify"
	},
	"commonAnnotations": {
		"summary": "Jenkins responding too slow to internal health check"
	},
	"externalURL": "http://1d870718c941:9093",
	"version": "4",
	"groupKey": "{}:{}"
}
```

### Cisco WebEx Teams
Cisco WebEx Teams outgoing webhook specification: https://developer.webex.com/docs/api/guides/webhooks

Incoming webhooks are very simple. It is a JSON structure with either a `text` or `markdown` key-value pair.

```json
{"text" : "This is a message from a Cisco Webex Teams incoming webhook."}
```

Markdown example:
```json
{"markdown" : "This is a `formatted message` from a [Cisco Webex Teams](https://www.ciscospark.com/) incoming webhook."}
```

An example:
https://webexapis.com/v1/webhooks/incoming/Y2lzY29zcGFyazovL3VzL1dFQkhPT0svYTBjOGQ3ZmQtYmRlOS00MjFlLWFiMTMtN2UxNWMxYjdlYmRh