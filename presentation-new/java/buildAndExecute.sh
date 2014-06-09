#!/bin/bash

export CONFIG_CLASS=$1

mvn clean install &> build.log
if [ $? -ne 0 ]
then
  echo "Build was not successful"
else
  java -cp target/config-builder-test-1.0.jar ${CONFIG_CLASS}
fi