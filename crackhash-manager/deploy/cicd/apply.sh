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

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MANIFEST_DIR="$SCRIPT_DIR/../k8s"
RENDERED=$(mktemp)
trap 'rm -f "$RENDERED"' EXIT

# Use the requested image tag without modifying the source manifest.
kubectl set image --local -f "$MANIFEST_DIR/deployment.yaml" \
    "manager=manager:$VERSION" -o yaml > "$RENDERED"

kubectl apply -f "$MANIFEST_DIR/namespace.yaml"
kubectl apply -f "$RENDERED"
kubectl apply -f "$MANIFEST_DIR/service.yaml"
kubectl -n imp-manager rollout status deployment/manager --timeout=300s
kubectl -n imp-manager get pods -o wide
