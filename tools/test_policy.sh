#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
mkdir -p app/build/policy-tests
java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d app/build/policy-tests \
  app/src/main/java/com/grabthesevehicles/app/IntervalPolicy.java \
  app/src/main/java/com/grabthesevehicles/app/VehicleMessages.java \
  tests/PolicyTest.java
java -cp app/build/policy-tests com.grabthesevehicles.app.PolicyTest
