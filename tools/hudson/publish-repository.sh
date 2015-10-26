#! /bin/bash -e

if [ -z "$1" ]; then
    echo "No release JDK (e.g., 'jdk-1.8.0-latest') supplied."
    exit 1
else
    JDK=$1
fi

if [ -z "$2" ]; then
    echo "No simultaneous release name (e.g., 'kepler', 'luna', 'mars') supplied."
    exit 2
else
    SIMREL=$2
fi

if [ -z "$3" ]; then
    echo "No repository name (e.g., 'main') supplied."
    exit 3
else
    REPOSITORY=$3
fi

if [ -z "$4" ]; then
    echo "No download path (e.g., 'downloads.eclipse.org/recommenders/updates/stable') supplied."
    exit 4
else
    DOWNLOAD_PATH=$4
fi

echo "Publishing '${REPOSITORY}' repository of '${JDK}'/'${SIMREL}' configuration at http://${DOWNLOAD_PATH}"
echo

SOURCE=${HUDSON_HOME}/jobs/${PROMOTED_JOB_NAME}/configurations/axis-jdk/${JDK}/axis-simrel/${SIMREL}/builds/${PROMOTED_ID}/archive/repositories/${REPOSITORY}/target/repository
TARGET=/home/data/httpd/${DOWNLOAD_PATH}

mkdir -p ${TARGET}
rm -f -R ${TARGET}/*
cp -v -R ${SOURCE}/* ${TARGET}

echo
