#!/bin/sh
set -eu

if [ "$#" -gt 1 ]; then
    echo "Usage: sh $0 [version]" >&2
    echo "Omit version to update configuration using the currently deployed image." >&2
    exit 1
fi

if [ "$#" -eq 1 ]; then
    VERSION="$1"
    case "$VERSION" in
        ''|[.-]*|*[!a-zA-Z0-9_.-]*) echo "Invalid image tag: $VERSION" >&2; exit 1 ;;
    esac
    [ "${#VERSION}" -le 128 ] || { echo "Image tag is too long" >&2; exit 1; }
    IMAGE="manager:$VERSION"
else
    IMAGE=$(kubectl -n imp-manager get deployment manager \
        -o 'jsonpath={.spec.template.spec.containers[?(@.name=="manager")].image}') || {
        echo "Cannot read the deployed manager image. For a first installation, pass its version." >&2
        exit 1
    }
    [ -n "$IMAGE" ] || { echo "No manager container image found. Pass its version." >&2; exit 1; }
fi

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MANIFEST_DIR="$SCRIPT_DIR/../k8s"
CLUSTER_CONFIG="$SCRIPT_DIR/../../config/clusterInfo.yaml"
SETTINGS_CONFIG="$MANIFEST_DIR/configmap.yaml"

[ -r "$CLUSTER_CONFIG" ] || { echo "Cannot read $CLUSTER_CONFIG" >&2; exit 1; }
[ -r "$SETTINGS_CONFIG" ] || { echo "Cannot read $SETTINGS_CONFIG" >&2; exit 1; }

RENDERED=$(mktemp)
CONFIG_MANIFEST="$RENDERED.configmap"
DEPLOYMENT_MANIFEST="$RENDERED.deployment"
trap 'rm -f "$RENDERED" "$CONFIG_MANIFEST" "$DEPLOYMENT_MANIFEST"' EXIT

# Generate the file ConfigMap from the single source configuration file.
kubectl -n imp-manager create configmap manager-configmap \
    --from-file="clusterInfo.yaml=$CLUSTER_CONFIG" \
    --dry-run=client -o yaml > "$CONFIG_MANIFEST"

# Changing either configuration changes the Pod template and triggers a rollout.
CLUSTER_HASH=$(sha256sum "$CLUSTER_CONFIG")
CLUSTER_HASH=${CLUSTER_HASH%% *}
SETTINGS_HASH=$(sha256sum "$SETTINGS_CONFIG")
SETTINGS_HASH=${SETTINGS_HASH%% *}

kubectl set image --local -f "$MANIFEST_DIR/deployment.yaml" \
    "manager=$IMAGE" -o yaml > "$RENDERED"

kubectl patch --local -f "$RENDERED" --type=merge \
    -p "{\"spec\":{\"template\":{\"metadata\":{\"annotations\":{\"checksum/cluster-config\":\"$CLUSTER_HASH\",\"checksum/settings\":\"$SETTINGS_HASH\"}}}}}" \
    -o yaml > "$DEPLOYMENT_MANIFEST"

kubectl apply -f "$MANIFEST_DIR/namespace.yaml"
kubectl apply -f "$SETTINGS_CONFIG"
kubectl apply -f "$CONFIG_MANIFEST"
kubectl apply -f "$DEPLOYMENT_MANIFEST"
kubectl apply -f "$MANIFEST_DIR/service.yaml"
kubectl -n imp-manager rollout status deployment/manager --timeout=300s
kubectl -n imp-manager get pods -o wide
