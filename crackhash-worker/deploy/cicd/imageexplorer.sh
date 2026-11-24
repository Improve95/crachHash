#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Usage: sh $0 <version>" >&2
    exit 1
fi

VERSION="$1"
case "$VERSION" in
    ''|[.-]*|*[!a-zA-Z0-9_.-]*) echo "Invalid image tag: $VERSION" >&2; exit 1 ;;
esac
[ "${#VERSION}" -le 128 ] || { echo "Image tag is too long" >&2; exit 1; }

CONTAINER=$(docker create "worker:$VERSION")
trap 'docker rm "$CONTAINER" >/dev/null' EXIT
docker export -o "worker-rootfs-$VERSION.tar" "$CONTAINER"
tar -tf "worker-rootfs-$VERSION.tar"
