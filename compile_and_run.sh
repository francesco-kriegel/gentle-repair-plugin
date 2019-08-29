#/bin/bash

mvn clean package
cp target/gentle-repair-plugin-0.0.3-SNAPSHOT.jar ../Protege-5.5.0/plugins
../Protege-5.5.0/run.sh