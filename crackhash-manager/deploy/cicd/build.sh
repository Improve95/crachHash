#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Использование: sh $0 <версия>"
    echo "Пример: sh $0 0.2.1"
    exit 1
fi

VERSION="$1"
IMAGE="manager:$VERSION"
ARCHIVE="manager-$VERSION.tar"

docker buildx build --load -t "$IMAGE" ./crackHash/crackhash-manager/

docker image ls "$IMAGE"

docker save -o "$ARCHIVE" "$IMAGE"

scp "$ARCHIVE" ubuntu@192.168.56.11:/tmp/manager

echo "Образ $IMAGE собран и передан на worker: /tmp/manager/$ARCHIVE"