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
