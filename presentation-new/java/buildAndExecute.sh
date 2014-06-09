#!/bin/bash

export CONFIG_CLASS=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}

mvn compile exec:java -q -Dexec.mainClass=${CONFIG_CLASS} | grep -v '^\['

if [ $? -ne 0 ]
then
  exit 1
fi