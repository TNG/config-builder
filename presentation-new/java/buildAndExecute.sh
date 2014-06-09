#!/bin/bash

export CONFIG_CLASS=$1
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd ${DIR}

mvn clean install &> build.log
if [ $? -ne 0 ]
then
  echo "Build was not successful"
  exit 1
else
  java -cp target/config-builder-test-1.0.jar ${CONFIG_CLASS}
fi