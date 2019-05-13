## Overview

n/a

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
