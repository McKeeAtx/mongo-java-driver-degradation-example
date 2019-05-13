## Overview

n/a

## Setup

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
