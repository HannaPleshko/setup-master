#!/bin/bash
touch /tmp/keep.txt
S=$(cat /tmp/keep.txt)
if [ -z "$S" ]; then
  S=1
else
  S=$(( $S + 1 ))
fi
echo "$S" > /tmp/keep.txt
echo "SERAIL: $S"
echo "Initialize domino volume"
docker volume create domino_keep-$S
docker run -it -e "ServerName=domino-$S.hcl.xby.icdc.io" \
 -e "isFirstServer=true" \
 -h domino-$S.hcl.xby.icdc.io \
 -p 81:80 \
 -p 1352:1352 \
 -p 8880:8880 \
 -p 8889:8889 \
 -p 8890:8890 \
 -v domino_keep-$S:/local/notesdata \
 --stop-timeout=60 \
 --name domino-$S \
 registry.xby.icdc.io/hcl-team/domino-opendockercompatible:latest
echo "Start keep"
docker run -it \
 -h keep-$S.hcl.xby.icdc.io \
 -p 81:80 \
 -p 1352:1352 \
 -p 8880:8880 \
 -p 8889:8889 \
 -p 8890:8890 \
 -v domino_keep-$S:/local/notesdata \
 --stop-timeout=60 \
 --name keep-$S \
 registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1