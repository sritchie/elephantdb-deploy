#!/bin/sh

CLASSPATH_FILE="$( dirname "$0" )/.classpath"

if [ ! -s $CLASSPATH_FILE ]
then
  export LEIN_ROOT=1;
  lein classpath
  # Trigger another time for clean output
  lein classpath > $CLASSPATH_FILE;
fi

cat $CLASSPATH_FILE
