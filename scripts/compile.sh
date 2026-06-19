#!/usr/bin/env sh
set -eu

rm -rf out
mvn -q compile
