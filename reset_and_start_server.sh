#!/bin/bash

./clear.sh

java -DgigapaxosConfig=gigapaxos.properties -ea -Djava.util.logging.config.file=logging.properties -Dlog4j.configuration=log4j.properties -Djavax.net.ssl.keyStorePassword=qwerty -Djavax.net.ssl.keyStore=conf/keyStore/node100.jks -Djavax.net.ssl.trustStorePassword=qwerty -Djavax.net.ssl.trustStore=conf/keyStore/node100.jks -cp jar/fuse.jar edu.umass.cs.reconfiguration.ReconfigurableNode 100 101 102 900 901 902
