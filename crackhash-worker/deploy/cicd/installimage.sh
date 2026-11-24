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

ARCHIVE="/tmp/worker-images/worker-$VERSION.tar"
IMAGE="docker.io/library/worker:$VERSION"
CONTAINERD_SOCKET="${CONTAINERD_SOCKET:-/run/containerd/containerd.sock}"

[ -r "$ARCHIVE" ] || { echo "Archive not found or unreadable: $ARCHIVE" >&2; exit 1; }

sudo ctr --address "$CONTAINERD_SOCKET" --namespace k8s.io images import "$ARCHIVE"
sudo ctr --address "$CONTAINERD_SOCKET" --namespace k8s.io images list -q | grep -Fx -- "$IMAGE"
echo "Imported $IMAGE into Kubernetes containerd on this node."
