# на другой ноде
sudo ctr --address /run/containerd/containerd.sock --namespace k8s.io images import /tmp/manager/manager-0.2.4.tar
sudo ctr --address /run/containerd/containerd.sock --namespace k8s.io images list | grep docker.io/library/manager:0.2.2