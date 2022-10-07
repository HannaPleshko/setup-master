#!/bin/bash

if ! grep -q "127.0.0.1 secondary.hcl.a.xby.icdc.io" /etc/hosts; then
    echo "127.0.0.1 secondary.hcl.a.xby.icdc.io" >> /etc/hosts;
fi


curl -u "Full Admin:ibahcl_admin_cPem9" https://prime.hcl.a.xby.icdc.io/secondary$1.id -o server.id

cmd=docker

images=$($cmd images --format "{{.Repository}}:{{.Tag}}")
image_exists=`echo "$images" | grep "registry.xby.icdc.io/hcl-team/projectkeep-r12:1.0.43-SNAPSHOT"`

if [ -z "$image_exists" ]; then
  echo "Download images"
  $cmd login registry.xby.icdc.io
  $cmd pull registry.xby.icdc.io/hcl-team/projectkeep-r12:1.0.43-SNAPSHOT
fi

docker-compose up -d
