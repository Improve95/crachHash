docker create --name manager-inspect manager:0.2.4
docker export -o manager-rootfs.tar manager-inspect
tar -tf manager-rootfs.tar | less