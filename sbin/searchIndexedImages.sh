#!/bin/bash
# exit on error
set -e
CWD=$(pwd)
function dofail {
    cd $CWD
    printf '%s\n' "$1" >&2  ## Send message to stderr. Exclude >&2 if you don't want it that way.
    exit "${2-1}"  ## Return a code specified by $2 or 1 by default.
}

LIRESEARCHER_NUMTHREADS=8
LIRESEARCHER_FEATURES=OpponentHistogram,ColorLayout,SimpleColorHistogram
LIRESEARCHER_MAXDIFFERENCESCORE=8
LIRESEARCHER_SHOWSIMILARHITS=1

# check parameters
if [ "$#" -lt 2 ]; then
    dofail "USAGE: $0 srcDir indexDir\nFATAL: requires 'srcDir', 'indexDir' with files to search for 'import-XXXX' and index to read from\noptional parameters:\n- FEATURES default $LIRESEARCHER_FEATURES\n- MAXDIFFERENCESCORE default $LIRESEARCHER_MAXDIFFERENCESCORE\n- SHOWSIMILARHITS default $LIRESEARCHER_SHOWSIMILARHITS\n- NUMTHREADS default $LIRESEARCHER_NUMTHREADS\n" 1
    exit 1
fi

SEARCHDIR=$1
INDEXDIR=$2
if [ ! -d "$SEARCHDIR" ]; then
  echo "FATAL: srcDir '${SEARCHDIR}' must exist"
  exit 1
fi
if [ ! -d "$INDEXDIR" ]; then
  echo "FATAL: indexDir '${INDEXDIR}' must exist"
  exit 1
fi

# check optional parameters
if [ "$#" -gt 2 ]; then
    LIRESEARCHER_FEATURES=$3
fi
if [ "$#" -gt 3 ]; then
    LIRESEARCHER_MAXDIFFERENCESCORE=$4
fi
if [ "$#" -gt 4 ]; then
    LIRESEARCHER_SHOWSIMILARHITS=$5
fi
if [ "$#" -gt 5 ]; then
    LIRESEARCHER_NUMTHREADS=$6
fi


echo "start - searching images"

echo "now: configure linux vars: run sbin/configure-environment.sh"
source ./configure-environment.bash

echo "now: running searching images from '$SEARCHDIR' in index '$INDEXDIR' features:'$LIRESEARCHER_FEATURES' maxDifferenceScore:'LIRESEARCHER_MAXDIFFERENCESCORE' showSimilarHits: '$LIRESEARCHER_SHOWSIMILARHITS' threads:'$LIRESEARCHER_NUMTHREADS'"
cd ${LIRETOOLS}
./gradlew runSearch --args="-i $SEARCHDIR -l $INDEXDIR -m $LIRESEARCHER_MAXDIFFERENCESCORE -n $LIRESEARCHER_NUMTHREADS -f $LIRESEARCHER_FEATURES -s $LIRESEARCHER_SHOWSIMILARHITS" > $SEARCHDIR/findFilesInLireIndex.tmp && sed -e '/BUILD SUCCESSFUL/,$d' $SEARCHDIR/findFilesInLireIndex.tmp | sed -e '1,/Getting all images in/d' > $SEARCHDIR/findFilesInLireIndex.json
cd ${CWD}

echo "done - searching images"
