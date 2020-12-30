# Welcome to Infolayer AIDA Project

AIDA project is designed to be an abstraction layer for tools and frameworks responsible for IT services provisioning, monitoring and orchestration. The goal is to provide a common gateway for frameworks and repeatable activities and response to events.

**Motivation**

| Use case | Solutions | Our goal |
|-|-|-|
|Service provisioning and configuration|Ansible, Terraform, ...|Abstract and reuse well formated templates on each known framework|
|Server monitoring|Zabbix, Site24x7, NewRelic, ...|Complement and easly maintaing such monitoring tools|

**Current supported frameworks and resources**

- Generic Shell (Bash/Perl/Python) Linux Script. The script can output data or just an exit code.
- Generic PowerShell Script. The script can output data or just an exit code.
- Ansible Playbook. The ansible playbook will be called passing Inventory and variables.
- Monitoring queries (SNMP 1/2/3) customized and using the provided Java's SNMP plugin. SNMP Trapps is not yet supported.
- SQL queries (JDBC driver) customized and using the provided Java's JDBC plugin.
- Custom Java code.

**Future plans**

- Support for events!
- Support for events in SNMP Traps format.
- Support for events in E-mail reading.
- Support for events in GELF (Graylog Extend Log format).

## Concept

### Archtecture

![Archtecture diagram](doc/images/arch.png)

### Instance

An instance is a group of resources: Service BUS, API Gateway, Repositories, Scheduler, Remote and Executor Services.

## Components

### Service BUS

Central heart of the system. It is an Apache Kafka deployment.

### The API Gateway Service

Responsible for Input/Output. Users reach frameworks and repositories thrugh this component. The protocol is HTTPS and the API is Rest format. Check documentation for examples.

### Scheduler

Reads from the service bus scheduled tasks, holds the scheduler trigger and fire. Scheduler has no user interaction and all the configuration is done by the API Gateway Services. You may or may not have an Scheduler in our setup. Stopping the scheduler services simple stop triggering scheduled tasks.

### Remote

Responsible for dealing with distributed configuration and data exchange. Each instance can be connected to a upper instance creating an hierachical management structure. Remote operations are always assincronous.

### Repositories

| Use case | Technology|
|-|-|
|System configuration|MongoDB 4.2+|
|Inventory|MongoDB 4.2+|
|Events|Elasticserach 6.8+|
|Statistics|InfluxDB|

### Executor Service

Executor Services are responsible for running code (talk to the supported framework). Code is ```code``` found under a repository (Git for exeample). Each Executor Service is connected to a service bus (version 1.0 is Apache Kafka dependant) building executors instances or executors groups controlled by a central instance.

You can have as many as necessary Executors Services and some can be specialized for some tasks. We think having fewer is better, for example, having just one which runs on Linux and another for Windows specific tasks. Also, in case your need relies on massive SQL queries (maybe you are monitoring a huge RDBMS site) you should consider deploying a group of Executors to address tasks in a load balancing strategy.

## Code

- Code is JAVA followed by Spring Boot framework.

## Build

- Build is done by maven build.

## Deploy

### Setup Apache Kafka cluster

- Deploy on a single machine (development or production) [Recommend steps](https://www.digitalocean.com/community/tutorials/how-to-install-apache-kafka-on-centos-7)

### Deploy the API Gateway

- Setup: Linux (systemv) and Windows (winsw) services. Kubernets is comming...

- Running:

```shell
java -jar gateway.jar --server.address=kafka.fqdn:5099 --bind=0.0.0.0:8080
```

### Deploy Scheduler

- Setup: Linux (systemv) and Windows (winsw) services. Kubernets is comming...

- Running:

```shell
java -jar scheduler.jar --server.address=kafka.fqdn:5099
```

### Deploy Remote

- Setup: Linux (systemv) and Windows (winsw) services. Kubernets is comming...

- Running:

```shell
java -jar remote.jar --server.address=kafka.fqdn:5099
```

### Deploy Executor Services

- Setup: Linux (systemv) and Windows (winsw) services. Kubernets is comming...

- Running:

```shell
java -jar executor.jar --server.address=kafka.fqdn:5099
```
