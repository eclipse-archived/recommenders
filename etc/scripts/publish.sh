#!/bin/sh
rm -rf /home/data/httpd/download.eclipse.org/recommenders/updates/$target/*
cp -r $WORKSPACE/dist/$repo_name/target/repository/* /home/data/httpd/download.eclipse.org/recommenders/updates/$target/
