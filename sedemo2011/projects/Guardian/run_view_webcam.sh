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
if [ "x$1" = "x" ]; then
  echo Usage:
  echo    $0 \<deployedMCRroot\> args
else
  echo Setting up environment variables
  MCRROOT="$1"
  echo ---
  LD_LIBRARY_PATH=.:${MCRROOT}/runtime/glnxa64;
  LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRROOT}/bin/glnxa64;
  LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRROOT}/sys/os/glnxa64;
	MCRJRE=${MCRROOT}/sys/java/jre/glnxa64/jre/lib/amd64;
	LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/native_threads;
	LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/server;
	LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE}/client;
	LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${MCRJRE};
	LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:$LIBOPENTLD_ROOT;
  XAPPLRESDIR=${MCRROOT}/X11/app-defaults ;
  export LD_LIBRARY_PATH;
  export XAPPLRESDIR;
  echo LD_LIBRARY_PATH is ${LD_LIBRARY_PATH};
  shift 1
  args=
  while [ $# -gt 0 ]; do
      token=`echo "$1" | sed 's/ /\\\\ /g'`   # Add blackslash before each blank
      args="${args} ${token}" 
      shift
  done
  java -Xmx2048m -Xms1900m -Xss128m -Djava.library.path=/usr/local/lib:$HYPERMEDIA_ROOT:$LIBOPENTLD4J_ROOT:$LIBOPENTLD_ROOT -cp $JASON_ROOT/lib/jason.jar:$HYPERMEDIA_ROOT:$PROCESSING_ROOT:$LIBOPENTLD4J_ROOT:. jason.infra.centralised.RunCentralisedMAS Guardian.mas2j
fi
exit

