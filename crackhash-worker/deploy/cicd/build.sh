#!/bin/sh
set -eu

if [ "$#" -ne 1 ]; then
    echo "Использование: sh $0 <версия>"
    echo "Пример: sh $0 0.2.1"
    exit 1
fi

VERSION="$1"
case "$VERSION" in
    ''|[.-]*|*[!a-zA-Z0-9_.-]*) echo "Invalid image tag: $VERSION" >&2; exit 1 ;;
esac
[ "${#VERSION}" -le 128 ] || { echo "Image tag is too long" >&2; exit 1; }

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROJECT_DIR=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)
NODE_SSH="${NODE_SSH:-ubuntu@192.168.56.11}"
REMOTE_DIR=/tmp/worker-images
IMAGE="worker:$VERSION"
ARCHIVE="$SCRIPT_DIR/worker-$VERSION.tar"

docker buildx build --load -t "$IMAGE" "$PROJECT_DIR"

docker image ls "$IMAGE"

docker save -o "$ARCHIVE" "$IMAGE"

ssh "$NODE_SSH" 'mkdir -p /tmp/worker-images'
scp "$ARCHIVE" "$NODE_SSH:$REMOTE_DIR/worker-$VERSION.tar"
scp "$SCRIPT_DIR/installimage.sh" "$NODE_SSH:$REMOTE_DIR/installimage.sh"

echo "Uploaded $IMAGE to $NODE_SSH:$REMOTE_DIR/worker-$VERSION.tar"
echo "Next, run on node1: sh $REMOTE_DIR/installimage.sh $VERSION"
