#! /bin/bash -e

if [ -z "$1" ]; then
    echo "No simultaneous release name (e.g., 'kepler', 'luna', 'mars') supplied."
    exit 1
else
    SIMREL=$1
fi

if [ -z "$2" ]; then
    echo "No repository name (e.g., 'main') supplied."
    exit 2
else
    REPOSITORY=$2
fi

if [ -z "$3" ]; then
   echo "No download path (e.g., 'downloads.eclipse.org/recommenders/updates/stable') supplied."
   exit 3
else
   DOWNLOAD_PATH=$3
fi

if [[ $(shopt -s nullglob; set -- ${HUDSON_HOME}/jobs/${PROMOTED_JOB_NAME}/configurations/axis-jdk/*; echo $#) -ne 1 ]]; then
    echo "Multiple axis values exist; expecting exactly one."
    exit 4
fi

echo "Publishing '${REPOSITORY}' repository of '${SIMREL}' configuration at http://${DOWNLOAD_PATH}"
echo

SOURCE=${HUDSON_HOME}/jobs/${PROMOTED_JOB_NAME}/configurations/axis-jdk/*/axis-simrel/${SIMREL}/builds/${PROMOTED_ID}/archive/repositories/${REPOSITORY}/target/repository
TARGET=/home/data/httpd/${DOWNLOAD_PATH}

mkdir -p ${TARGET}
rm -f -R ${TARGET}/*
cp -v -R ${SOURCE}/* ${TARGET}

echo
