#!/bin/bash
#get the actual path of this script
SAAF_BIN="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
VM_ARGS="-Xms500M -Xmx500M"
cd $SAAF_BIN/../
java $VM_ARGS \
-Dfile.encoding=UTF-8 \
-jar SAAF.jar "$@"
