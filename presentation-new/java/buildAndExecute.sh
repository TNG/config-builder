#!/bin/bash

export CONFIG_CLASS=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}

mvn compile exec:java -e -q -Dexec.mainClass=${CONFIG_CLASS} | tee build.log  | grep -v '^\['

if [ $? -ne 0 ]
then
  exit 1
fi