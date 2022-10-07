#!/bin/bash
# if (Test-Path -Path 'tmp') {} else {mkdir 'tmp'}
# if ($S='') {
#   New-Item -Name tmp/keep.txt -ItemType File
# } else {
#   echo non-empty
# }

# list of all volumes: docker volume ls
# delete volume: docker volume rm domino_keep-1
# url to see -e env vars: https://github.com/IBM/domino-docker/blob/master/docs/run-variables.md
# hcl docker help url: 
#  -e "OrgUnitName=ibahcl" ^
docker volume create domino_keep-1
docker run -it -h domino-1.hcl.xby.icdc.io ^
 -e "ServerName=domino-1.hcl.xby.icdc.io" ^
 -e "isFirstServer=true" ^
 -e "DominoDomainName=ibahcl" ^
 -e "AdminFirstName=Pavel" ^
 -e "AdminMiddleName=V" ^
 -e "AdminLastName=Lihtarovich" ^
 -e "AdminPassword=Pavel8Lih" ^
 -p 81:80 ^
 -p 1352:1352 ^
 -p 8880:8880 ^
 -p 8889:8889 ^
 -p 8890:8890 ^
 -v domino_keep-1:/local/notesdata ^
 --stop-timeout=60 ^
 --name domino-1 ^
 registry.xby.icdc.io/hcl-team/domino-opendockercompatible:latest
 
 # after that need to run image with domino and keep

echo "Start keep"
docker run -it ^
 -h keep-1.hcl.xby.icdc.io ^
 -p 81:80 ^
 -p 1352:1352 ^
 -p 8880:8880 ^
 -p 8889:8889 ^
 -p 8890:8890 ^
 -v domino_keep-1:/local/notesdata ^
 --stop-timeout=60 ^
 --name keep-1 ^
 registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1
