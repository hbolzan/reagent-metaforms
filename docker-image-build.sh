#!/bin/bash

VERSION=$1

# build release app before building the image
yarn release app

docker build --build-arg VERSION=$VERSION --build-arg USER=$DOCKER_USER_ID \
       -t "${DOCKER_USER_ID}/metaforms-static:latest" \
       -t "${DOCKER_USER_ID}/metaforms-static:${VERSION}" .
docker push "${DOCKER_USER_ID}/metaforms-static:${VERSION}"
docker push "${DOCKER_USER_ID}/metaforms-static:latest"
