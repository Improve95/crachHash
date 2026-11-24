# на другой ноде
sudo ctr --address /run/containerd/containerd.sock --namespace k8s.io images import /tmp/worker/worker-0.1.0.tar
sudo ctr --address /run/containerd/containerd.sock --namespace k8s.io images list | grep docker.io/library/worker-0.1.0