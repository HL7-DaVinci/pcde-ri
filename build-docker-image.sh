#!/bin/sh

mvn package && \
  docker build -t pcde .
