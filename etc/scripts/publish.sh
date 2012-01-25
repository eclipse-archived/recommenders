#!/bin/sh
echo "value of channel: $channel"
echo "value of version: $version"
rm -rf /home/data/httpd/download.eclipse.org/recommenders/updates/$channel/$version/*
cp -r  $WORKSPACE/dist/org.eclipse.recommenders.repository.$channel.$version/target/repository/* /home/data/httpd/download.eclipse.org/recommenders/updates/$channel/$version/
cp     $WORKSPACE/etc/scripts/index.php /home/data/httpd/download.eclipse.org/recommenders/updates/$channel/$version/