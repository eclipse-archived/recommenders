#! /bin/bash -e

if [ -z "$1" ]; then
    echo "No simultaneous release name (e.g., 'luna', 'mars') supplied."
    exit 1
else
    SIMREL=$1
fi

if [ -z "$2" ]; then
    echo "No release name ('head', 'milestones', 'stable', 'simrel') supplied."
    exit 2
else
    RELEASE=$2
fi

if [ -z "$3" ]; then
   echo "No download path (e.g., 'downloads.eclipse.org/recommenders/updates/head') supplied."
   exit 3
else
   DOWNLOAD_PATH=$3
fi

echo "Publishing '${RELEASE}' repository of '${SIMREL}' configuration at http://${DOWNLOAD_PATH}"
echo

SOURCE=${HUDSON_HOME}/jobs/${PROMOTED_JOB_NAME}/configurations/axis-simrel/${SIMREL}/builds/${PROMOTED_ID}/archive/repositories/${RELEASE}/target/repository
TARGET=/home/data/httpd/${DOWNLOAD_PATH}

mkdir -p ${TARGET}
rm -f -R ${TARGET}/*
cp -v -R ${SOURCE}/* ${TARGET}
