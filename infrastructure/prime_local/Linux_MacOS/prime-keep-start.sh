#!/bin/bash

if ! grep -q "127.0.0.1 local_prime.hcl.a.xby.icdc.io" /etc/hosts; then
    echo "127.0.0.1 local_prime.hcl.a.xby.icdc.io" >> /etc/hosts;
fi

cmd=docker

images=$($cmd images --format "{{.Repository}}:{{.Tag}}")
image_exists=`echo "$images" | grep "registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1"`

if [ -z "$image_exists" ]; then
  echo "Download images"
  $cmd login registry.xby.icdc.io
  $cmd pull registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1
fi

docker-compose up -d
