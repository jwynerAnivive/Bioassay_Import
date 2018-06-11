#! /bin/sh

echo $GCLOUD_SERVICE_KEY | base64 -d > ${HOME}/gcloud-service-key.json
/root/google-cloud-sdk/bin/gcloud auth activate-service-account --key-file ${HOME}/gcloud-service-key.json
/root/google-cloud-sdk/bin/gcloud config set project aniselect-176223