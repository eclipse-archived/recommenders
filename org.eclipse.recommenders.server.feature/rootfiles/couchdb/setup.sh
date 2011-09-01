#!/bin/sh

##
## Run this script once to create databases in your local CouchDB and put all necessary views in place.
## Note, this script can be run once only. If a database or view already exists, it remains unchanged
## and an error message is printed.
##

##
## Configure URLs and names here.
##
COUCH_BASE_URL=http://127.0.0.1:5984
EXTDOC_DB_NAME=extdoc
UDC_DB_NAME=udc


########## Below this point no manual editing should be required. ###########

##
## create 'udc' database and put views in place
##

UDC_DB_URL=$COUCH_BASE_URL/$UDC_DB_NAME/
# create db:
curl -X PUT $UDC_DB_URL
# put views:
curl -X PUT $UDC_DB_URL/_design/objectUsages -T udc/objectUsages.json
curl -X PUT $UDC_DB_URL/_design/metaData -T udc/metaData.json
curl -X PUT $UDC_DB_URL/_design/compilationunits -T udc/compilationunits.json


##
## create 'extdoc' database and insert views
##

EXTDOC_DB_URL=$COUCH_BASE_URL/$EXTDOC_DB_NAME/
# create db:
curl -X PUT $EXTDOC_DB_URL
# put views:
curl -X PUT $EXTDOC_DB_URL/_design/providers -T extdoc/providers.json
