kubectl apply -f sprint1/deployment.yaml
kubectl -n imp-manager rollout status deployment/manager --timeout=300s
# curl -i --connect-timeout 5 http://192.168.56.11:30080/sprint1/health