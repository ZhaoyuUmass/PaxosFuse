#!/bin/bash

HEAD=`dirname $0`
CLASSPATH=$CLASSPATH:`ls $HEAD/jar/fuse.jar`
LOG_PROPERTIES=logging.properties
GP_PROPERTIES=gigapaxos.properties
JVMFLAGS="-ea -Djava.util.logging.config.file=$LOG_PROPERTIES \
 -DgigapaxosConfig=$GP_PROPERTIES"

ACTIVE="active"
RECONFIGURATOR="reconfigurator"

SSL_OPTIONS="-Djavax.net.ssl.keyStorePassword=qwerty \
-Djavax.net.ssl.keyStore=conf/keyStore/node100.jks \
-Djavax.net.ssl.trustStorePassword=qwerty \
-Djavax.net.ssl.trustStore=conf/keyStore/node100.jks"

APP=`cat $GP_PROPERTIES|grep "^[ \t]*APPLICATION="|                \
sed s/"^[ \t]*APPLICATION="//g`

if [[ $APP == "edu.umass.cs.gigapaxos.examples.noop.NoopPaxosApp" ]];
then CLIENT=edu.umass.cs.gigapaxos.examples.noop.NoopPaxosAppClient
else if [[ $APP == \
"edu.umass.cs.reconfiguration.examples.noopsimple.NoopApp" || $APP == "" ]]; then
CLIENT=edu.umass.cs.reconfiguration.examples.NoopAppClient 
else
CLIENT=$1
fi 
fi

echo "Running $CLIENT"

java $JVMFLAGS $SSL_OPTIONS $CLIENT
