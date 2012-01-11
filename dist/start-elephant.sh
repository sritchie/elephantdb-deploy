#!/bin/sh

java -server -Xms5120m -Xmx5120m -cp `sh classpath.sh` elephantdb.keyval.core $1 local-conf.clj
