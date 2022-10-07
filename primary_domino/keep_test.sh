#!/bin/bash

cmd=podman

function download_image() {
  images=$($cmd images --format "{{.Repository}}:{{.Tag}}")
  image_exists=`echo "$images" | grep "registry.xby.icdc.io/hcl-team/$1"`
  if [ -z "$image_exists" ]; then
    $cmd pull registry.xby.icdc.io/hcl-team/$1
  fi
}

echo "Download images"
$cmd login registry.xby.icdc.io
download_image domino-opendockercompatible:latest
download_image projectkeep-r12:0.9.25-EARLYACCESS-1

echo "HTTP server for ConfigFile"
ip=$(hostname -I | awk '{print $1}')
pushd ./www
python -m http.server &
http_pid=$!
popd

echo "Search free index for container"
volume_name="domino_keep-"
id=0
existing_volumes=$($cmd volume list --format "{{.Name}}" | grep $volume_name | sort -n)
for i in {1..100}; do
    volume_exist=$(echo "$existing_volumes" | grep "$volume_name$i")
    [ -z "$volume_exist" ] && id=$i && break
done
[ "$id" == "0" ] && echo "You consume all available volumes. Clear first" && exit 1
volume_name="$volume_name$id"

echo "Initialize domino volume: $volume_name"
$cmd volume create $volume_name

servername="keep-$id"
hostname="$servername.hcl.a.xby.icdc.io"
echo "Store domino hostname '$hostname' in /etc/hosts"
echo "127.0.0.1 $servername $hostname" >> /etc/hosts

admin_pass="ibahcl_admin-qZhB4"
org_pass="ibahcl_org-G5mWp"

echo ""
echo "####################################################"
echo "# Start domino to init volume and generate IDs.    #"
echo "#                                                  #"
echo "# Stop with Ctrl+C when get line 'done PID is XXX' #"
echo "####################################################"
# Note: it should shutdown with success in couple of minutes
$cmd run \
 -e "ServerName=$servername" \
 -e "isFirstServer=true" \
 -e "AdminFirstName=Domino" \
 -e "AdminLastName=Admin" \
 -e "AdminIDFile=/local/notesdata/domino/html/admin.id" \
 -e "AdminPassword=$admin_pass" \
 -e "DominoDomainName=IBAHCL" \
 -e "OrganizationName=IBAHCL" \
 -e "OrganizationPassword=$org_pass" \
 -e "ConfigFile=http://$ip:8000/primary_mta.json" \
 -h $hostname \
 -p 25:25 \
 -p 80:80 \
 -p 1352:1352 \
 -v $volume_name:/local/notesdata \
 --stop-timeout=60 \
 --name domino_init-$id \
 registry.xby.icdc.io/hcl-team/domino-opendockercompatible:latest
# Additional variables what can be used:
# -e "HostName=$hostname" \ #I believe we take it already from container hostname
# -e "DominoConfigRestartWaitTime=40" \
# -e "DominoConfigRestartWaitString=NO Server started on physical node" \

# names.nsf is not created in before 
#while [ -z "$is_ready" ]; do
#  is_ready=$($cmd logs domino_init-$id | grep "done PID is")
#  sleep 5
#done

kill -9 $http_pid

echo ""
echo "###########################"
echo "# Start keep container    #"
echo "###########################"
$cmd run -it \
 -h $hostname \
 -p 25:25 \
 -p 80:80 \
 -p 1352:1352 \
 -p 8880:8880 \
 -p 8889:8889 \
 -p 8890:8890 \
 -v $volume_name:/local/notesdata \
 --stop-timeout=60 \
 --name keep-$id \
 registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1

