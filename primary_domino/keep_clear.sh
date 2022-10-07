#!/bin/bash
cmd=podman
for i in {1..50}; do
  $cmd stop keep-$i
  $cmd stop domino_init-$i
  $cmd rm keep-$i
  $cmd rm domino_init-$i
  $cmd volume rm domino_keep-$i
done
