# datadog-homework
An HTTP log monitor for a take-home interview project for Datadog

### Install

#### Download Binary

[Download the released binary here](https://github.com/julianvmodesto/httplogmon/releases/tag/1.0.0).

#### Build from Source

Prerequisites: Java 8 & sbt. You can [download sbt here](http://www.scala-sbt.org/download.html)
e.g. `brew install sbt`.

```
git clone git@github.com:julianvmodesto/datadog-homework.git
cd datadog-homework
sbt assembly
```

### Quickstart

To generate an active HTTP access log in
[Common Log Format](https://en.wikipedia.org/wiki/Common_Log_Format), I piped
an existing access log with a rate limit.

For the logs, I used an HTTP access log from NASA in 1995. You can [download
the logs here](http://ita.ee.lbl.gov/html/contrib/NASA-HTTP.html).

For the pipe rate-limiter, I used `pv`, or Pipe Viewer. You can [download `pv`
here](http://www.ivarch.com/programs/pv.shtml) e.g. `brew install pv`.

```
# download and unpack the access log

# then,
touch access.log
cat NASA_access_log_Jul95 | pv -L 1k >> access.log

# afterwards, in another terminal:
java -jar target/scala-2.11/httplogmon.jar -f access.log -t 1000
```

![running running running](https://github.com/julianvmodesto/datadog-homework/blob/master/example.gif)

