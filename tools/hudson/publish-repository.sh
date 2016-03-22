#! /bin/bash -e

if [ -z "$1" ]; then
    echo "No simultaneous release name (e.g., 'kepler', 'luna', 'mars') supplied."
    exit 1
else
    SIMREL=$1
fi

if [ -z "$2" ]; then
    echo "No release toolchain (e.g., 'JavaSE-1.7') supplied."
    exit 2
else
    TOOLCHAIN=$2
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

echo "Publishing '${REPOSITORY}' repository of '${SIMREL}'/'${TOOLCHAIN}' configuration at http://${DOWNLOAD_PATH}"
echo

SOURCE=${HUDSON_HOME}/jobs/${PROMOTED_JOB_NAME}/configurations/axis-simrel/${SIMREL}/axis-toolchain/${TOOLCHAIN}/builds/${PROMOTED_ID}/archive/repositories/${REPOSITORY}/target/repository
TARGET=/home/data/httpd/${DOWNLOAD_PATH}

mkdir -p ${TARGET}
rm -f -R ${TARGET}/*
cp -v -R ${SOURCE}/* ${TARGET}

echo
