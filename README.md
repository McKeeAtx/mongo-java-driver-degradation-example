## Overview

Sample project that demonstrates how a single non-responsive replica set member renders a client application unresponsive for multiple minutes.

The following local setup was used during testing:
* OpenJDK 1.8.0_121
* macOS High Sierra 10.13.6
* mongod version v3.4.20
* mongo-java-driver 3.10.2

## Initial Setup

Create three empty directories `rs0-0`, `rs0-1` and `rs0-2`.

Start three mongod instances:

```
mongod --replSet rs0 --port 27017 --bind_ip 127.0.0.1 --dbpath rs0-0 --smallfiles --oplogSize 128
mongod --replSet rs0 --port 27018 --bind_ip 127.0.0.1 --dbpath rs0-1 --smallfiles --oplogSize 128
mongod --replSet rs0 --port 27019 --bind_ip 127.0.0.1 --dbpath rs0-2 --smallfiles --oplogSize 128
```

Log into the mongo shell and configure the replica set
```
mongo
> rsconf = { _id: "rs0", members: [ { _id: 0, host: "127.0.0.1:27017" }, { _id: 1, host: "127.0.0.1:27018" }, { _id: 2,      host: "127.0.0.1:27019" } ] }
> rs.initiate(rsconf)
{ "ok" : 1 }
```

Create the test database:
```
rs0:PRIMARY> use test
mongo
switched to db test
```

Run `DataFixture` to insert 10000 documents into the `test` collection within the `test` database. 

## Running the example

Start three mongod instances:

```
mongod --replSet rs0 --port 27017 --bind_ip 127.0.0.1 --dbpath rs0-0 --smallfiles --oplogSize 128
mongod --replSet rs0 --port 27018 --bind_ip 127.0.0.1 --dbpath rs0-1 --smallfiles --oplogSize 128
mongod --replSet rs0 --port 27019 --bind_ip 127.0.0.1 --dbpath rs0-2 --smallfiles --oplogSize 128
```

Run the `Consumer` application.

The `ConnectionHistogramListener` logs a simple histogram every time a `ConnectionPoolWaitQueueEnteredEvent` event is published:
* `open`: no of threads that have acquired a conncection
* `waiting`: no of threads that are waiting for a conncetion

Example (the output is somewhat redundant):
```
08:01:38 [pool-2-thread-1] - Connection attempt to server '127.0.0.1:27019'.
08:01:38 [pool-2-thread-1] - Connection attempt to server '127.0.0.1:27019'.
08:01:38 [pool-2-thread-1] - 	127.0.0.1:27019[open:  0 waiting:  1] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:01:38 [pool-2-thread-1] - 	127.0.0.1:27019[open:  0 waiting:  1] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:01:39 [pool-2-thread-2] - Connection attempt to server '127.0.0.1:27018'.
08:01:39 [pool-2-thread-2] - Connection attempt to server '127.0.0.1:27018'.
08:01:39 [pool-2-thread-2] - 	127.0.0.1:27019[open:  0 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:01:39 [pool-2-thread-2] - 	127.0.0.1:27019[open:  0 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
```

## Degrade a single replica set member

Determine the `PID` of a replica set member:
```
ps -ef | grep mongo | grep "replSet"
  501 24989 11889   0  7:42AM ttys001    0:30.81 mongod --replSet rs0 --port 27017 --bind_ip 127.0.0.1 --dbpath rs0-0 --smallfiles --oplogSize 128
  501 24990 11905   0  7:42AM ttys002    0:30.02 mongod --replSet rs0 --port 27018 --bind_ip 127.0.0.1 --dbpath rs0-1 --smallfiles --oplogSize 128
  501 24991 11922   0  7:42AM ttys005    0:30.03 mongod --replSet rs0 --port 27019 --bind_ip 127.0.0.1 --dbpath rs0-2 --smallfiles --oplogSize 128
```

In this example, we pick `24991` (the member that listens on port 27019).

Use `cpulimit` to reduce the percentage of CPU that will be assign to this process:
```
cpulimit -p 24991 -l 0
Process 24991 found
```
The server selector algorithm now continues to round-robin connection requests between the three replica set members. All members are considered
* eligible
* non-stale
* and within the latency window:
```
08:09:28 [pool-2-thread-1] - 	127.0.0.1:27019[open:  0 waiting:  1] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:29 [pool-2-thread-2] - 	127.0.0.1:27019[open:  1 waiting:  1] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:30 [pool-2-thread-3] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:30 [pool-2-thread-3] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:31 [pool-2-thread-4] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:31 [pool-2-thread-4] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:32 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:32 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:33 [pool-2-thread-6] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:33 [pool-2-thread-6] - 	127.0.0.1:27019[open:  2 waiting:  0] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:34 [pool-2-thread-3] - 	127.0.0.1:27019[open:  2 waiting:  1] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:35 [pool-2-thread-4] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:36 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:36 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:37 [pool-2-thread-6] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:37 [pool-2-thread-6] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  1] / 
08:09:38 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:38 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  2] / 127.0.0.1:27018[open:  0 waiting:  1] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:39 [pool-2-thread-6] - 	127.0.0.1:27019[open:  2 waiting:  3] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
08:09:40 [pool-2-thread-5] - 	127.0.0.1:27019[open:  2 waiting:  4] / 127.0.0.1:27018[open:  0 waiting:  0] / 127.0.0.1:27017[open:  0 waiting:  0] / 
```

The node `127.0.0.1:27019` is not responding to requests in a timely manner, which causes threads to pile up waiting for a response or a connection. Eventually, the threads waithing for `127.0.0.1:27019` fully utilize the application's thread pool, rendering the whole application unresponsive although the replica set members `127.0.0.1:27017` and `127.0.0.1:27018` could still serve requests.

After a couple of minutes, the threads waiting for `127.0.0.1:27019` either return or time out and the driver stops to route further requests to `127.0.0.1:27019`. The application then recovers, distributing the traffic between the healthy replica set members `127.0.0.1:27017` and `127.0.0.1:27018`.

From the users' point of view, this behavior causes a complete outage of the client application that lasts multiple minutes.
