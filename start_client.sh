#!/bin/bash

java -DgigapaxosConfig=gigapaxos.properties -Djavax.net.ssl.keyStorePassword=qwerty -Djavax.net.ssl.keyStore=conf/keyStore/node100.jks -Djavax.net.ssl.trustStorePassword=qwerty -Djavax.net.ssl.trustStore=conf/keyStore/node100.jks -cp jar/fuse.jar exercise.NumNoopAppClient
