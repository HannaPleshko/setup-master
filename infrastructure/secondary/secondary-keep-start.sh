#!/bin/bash

if ! grep -q "10.209.0.4 prime" /etc/hosts; then
    echo "10.209.0.4 prime" >> /etc/hosts;
fi

cmd=docker

images=$($cmd images --format "{{.Repository}}:{{.Tag}}")
image_exists=`echo "$images" | grep "registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1"`

if [ -z "$image_exists" ]; then
  echo "Download images"
  $cmd login registry.xby.icdc.io
  $cmd pull registry.xby.icdc.io/hcl-team/projectkeep-r12:1.0.38-SNAPSHOT
fi

docker-compose up -d
