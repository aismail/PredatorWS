#!/bin/sh
# script for execution of deployed applications
#
# Sets up the MCR environment for the current $ARCH and executes 
# the specified command.
#

JAVA_BIN=/usr/lib/jvm/java-6-sun/bin

exe_name=$0
exe_dir=`dirname "$0"`
echo "------------------------------------------"
echo Setting up environment variables

echo ---

LD_LIBRARY_PATH=.:${MCR_ROOT}/runtime/glnxa64;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCR_ROOT}/bin/glnxa64;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCR_ROOT}/sys/os/glnxa64;
MCRJRE=${MCR_ROOT}/sys/java/jre/glnxa64/jre/lib/amd64;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/native_threads;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/server;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/client;
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE};
LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$LIBOPENTLD_ROOT;
XAPPLRESDIR=${MCR_ROOT}/X11/app-defaults ;
export LD_LIBRARY_PATH;
export XAPPLRESDIR;
echo LD_LIBRARY_PATH is ${LD_LIBRARY_PATH};

MISC_JARS=
for JAR_FILE in `ls $MISC_JARS_ROOT/*.jar`; do
    echo $JAR_FILE
    MISC_JARS=$MISC_JARS:$JAR_FILE
done

java -Xmx2048m -Xms1900m -Xss128m -Djava.library.path=/usr/local/lib:$HYPERMEDIA_ROOT:$LIBOPENTLD4J_ROOT:$LIBOPENTLD_ROOT:$JSON_ROOT:$FACE4J_ROOT:$CARTAGO_ROOT -cp $JASON_ROOT/lib/jason.jar:$HYPERMEDIA_ROOT:$PROCESSING_ROOT:$LIBOPENTLD4J_ROOT:$JSON_ROOT:$FACE4J_ROOT$MISC_JARS:$CARTAGO_ROOT/cartago.jar:$CARTAGO_ROOT/c4jason.jar:./build:. jason.infra.centralised.RunCentralisedMAS Guardian.mas2j

exit

