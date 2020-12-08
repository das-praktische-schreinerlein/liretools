#!/bin/bash
# exit on error
set -e
CWD=$(pwd)
SCRIPTPATH="$( cd "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
function dofail {
    cd $CWD
    printf '%s\n' "$1" >&2  ## Send message to stderr. Exclude >&2 if you don't want it that way.
    exit "${2-1}"  ## Return a code specified by $2 or 1 by default.
}

LIREINDEXER_NUMTHREADS=8
LIREINDEXER_FEATURES=CEDD,FCTH,OpponentHistogram,ColorLayout,JCD,SimpleColorHistogram

# check parameters
if [ "$#" -lt 2 ]; then
    dofail "USAGE: $0 srcDir indexDir\\n FATAL: requires 'srcDir', 'indexDir' with files to index 'import-XXXX' and index to write to\n\noptional parameters:\n- FEATURES default $LIREINDEXER_FEATURES\n- NUMTHREADS default $LIREINDEXER_NUMTHREADS\n" 1
    exit 1
fi

INDEXSRC_MEDIADIR=$1
INDEXDIR=$2
if [ ! -d "$INDEXSRC_MEDIADIR" ]; then
  echo "FATAL: srcDir '${INDEXSRC_MEDIADIR}' must exist"
  exit 1
fi

# check optional parameters
if [ "$#" -gt 2 ]; then
    LIREINDEXER_FEATURES=$3
fi
if [ "$#" -gt 3 ]; then
    LIREINDEXER_NUMTHREADS=$4
fi


echo "start - indexing images"

echo "now: configure linux vars: run sbin/configure-environment.sh"
source ${SCRIPTPATH}/configure-environment.bash

echo "now: running indexing images from '$INDEXSRC_MEDIADIR' to '$INDEXDIR' features:'$LIREINDEXER_FEATURES' threads:'$LIREINDEXER_NUMTHREADS'"
cd ${LIRETOOLS}
./gradlew runIndexing --args="-i $INDEXSRC_MEDIADIR -l $INDEXDIR -n $LIREINDEXER_NUMTHREADS -f $LIREINDEXER_FEATURES"
cd ${CWD}

echo "done - indexing images"
