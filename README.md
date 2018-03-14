# SpringBoot-HystrixDashboard-Example
Sample application showing off Hystrix's circuit-breaking capabilities, and dashboard.

Requirements
============
- Java 8

Running
=======
To start the application:

```gradlew bootRun```

Then the following URLs become available:

- Request a random string:
```http://localhost:8080/randomString```

- Request several random strings, with the specified delay between requests:
```http://localhost:8080/randomStringStream/{delay}```

- Dashboard:
```http://localhost:8080/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A8080%2Factuator%2Fhystrix.stream```

Further Reading
===============
https://github.com/Netflix/Hystrix/wiki/How-To-Use