# ZeroProxy is a multi purpose http proxy.
* Support http(https) proxy
* Support websocket proxy (Not yet)
* Support multiple targets

[![Build Status](https://travis-ci.org/code13k/zeroproxy.svg?branch=master)](https://travis-ci.org/code13k/zeroproxy)


# Supported protocol
* http / https
* ws / wss (Not yet)


# Configuration

## app_config.yml
It's application configuration file.
```yaml
# Server port
port:
  proxy_http: 55550
  proxy_ws: 55551
  api_http: 55552
```

## proxy_config.yml
It's proxy configuration file.
```yaml
# Example-1
- location: /zeroproxy_example
  type: round-robin
  connect_timeout: 3000
  idle_timeout: 3000
  targets:
    - http://1.1.1.1:3000

# Example-2
- location: /zeroproxy/example
  type: random
  connect_timeout: 3000
  idle_timeout: 3000
  targets:
    - http://1.1.1.1:3000
    - http://1.1.1.1:3001

# Example-3
- location: /zeroproxy/example/1
  type: all
  connect_timeout: 3000
  idle_timeout: 3000
  targets:
    - http://1.1.1.1:3000
    - http://1.1.1.1:3001
    - http://1.1.1.1:3002

# Example-4
- location: /
  type: round-robin
  connect_timeout: 3000
  idle_timeout: 3000
  targets:
    - http://1.1.1.1:3000
```

## logback.xml
It's Logback configuration file that is famous logging library.
* You can send error log to Telegram.
  1. Uncomment *Telegram* configuration.
  2. Set value of `<botToken>` and `<chatId>`.
       ```xml
       <appender name="TELEGRAM" class="com.github.paolodenti.telegram.logback.TelegramAppender">
           <botToken></botToken>
           <chatId></chatId>
           ...
       </appender>
       ```
  3. Insert `<appender-ref ref="TELEGRAM"/>` into `<root>`
     ```xml
     <root level="WARN">
         <appender-ref ref="FILE"/>
         <appender-ref ref="TELEGRAM"/>
     </root>
     ```
* You can send error log to Slack.
  1. Uncomment *Slack* configuration.
  2. Set value of `<webhookUri>`.
       ```xml
       <appender name="SLACK_SYNC" class="com.github.maricn.logback.SlackAppender">
           <webhookUri></webhookUri>
           ...
       </appender>
       ```
  3. Insert `<appender-ref ref="SLACK"/>` into `<root>`
     ```xml
     <root level="WARN">
         <appender-ref ref="FILE"/>
         <appender-ref ref="SLACK"/>
     </root>
     ```
* You can reload configuration but need not to restart application.


# Server
ZeroProxy has three servers. 
One is a http proxy server, another is a websocket proxy server, and the other is a restful API server.
Restful API server provide application information and additional functions.

## Main HTTP Server
### Usage
```html
http://example.com:{port}/{location}/{path}
ws://example.com:{port}/{location}/{path}
```
* port
  * Server port
  * It's *proxy_http* and *proxy_ws* in app_config.yml.
* location
  * Location
  * It's *location* in proxy_config.yml
* path
  * Target path

### Example
```html
http://example.com:55550/zeroproxy/example/test.png
```

## API HTTP Server
### Usage
```html
http://example.com:{port}/{domain}/{method}
```

### Example
```html
http://example.com:57911/app/status
http://example.com:57911/app/hello
http://example.com:57911/app/ping
```

### API
#### GET /app/status
* Get application status and environment.
##### Response
```json
{
  "data":{
    "applicationVersion":"0.1.0-alpha.1",
    "cpuUsage":2.56,
    "threadInfo":{...},
    "vmMemoryFree":"190M",
    "javaVersion":"1.8.0_25",
    "vmMemoryMax":"3,641M",
    "currentDate":"2018-09-18T18:48:58.795+09:00",
    "threadCount":15,
    "startedDate":"2018-09-18T18:48:40.901+09:00",
    "javaVendor":"",
    "runningTimeHour":0,
    "osName":"Mac OS X",
    "cpuProcessorCount":4,
    "vmMemoryTotalFree":"3,585M",
    "hostname":"",
    "osVersion":"10.11.6",
    "jarFile":"code13k-zeroproxy-0.1.0-alpha.1.jar",
    "vmMemoryAllocated":"245M",
  }
}
```
#### GET /app/hello
* Hello, World
##### Response
```json
{"data":"world"}
```

#### GET /app/ping
* Ping-Pong
##### Response
```json
{"data":"pong"}