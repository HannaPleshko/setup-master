# @DEPRECATED Docker setup

- Login to https://artifactory.xby.icdc.io/ with iba creds (email, intranet psw), go to https://artifactory.xby.icdc.io/#browse/browse:docker-hosted and check if you see 2 docker images (domino-opendockercompatible:latest, projectkeep-r12:0.9.25-EARLYACCESS-1).

- Run a Docker client, wait until it fully loads.

- Login and pull docker images:

```
docker login registry.xby.icdc.io
docker pull registry.xby.icdc.io/hcl-team/domino-opendockercompatible:latest
docker pull registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1
```

- Run the "hcl-keep-setup.sh" file:

```
chmod +x hcl-keep.sh (one-time operation)
./hcl-keep.sh

Notes:
1) for Windows the "\" symbol should be replaced with the "^" symbol, example:

Mac / Linux:
-p 8890:8890 \
-v domino_keep:/local/notesdata \
--stop-timeout=60 \

Windows:
-p 8890:8890 ^
-v domino_keep:/local/notesdata ^
--stop-timeout=60 ^


2) you should see the following line in your setup logs:
./java -ss512k -Xmso5M -cp jhall.jar:cfgdomserver.jar:./ndext/ibmdirectoryservices.jar lotus.domino.setup.WizardManagerDomino -data /local/notesdata -silent /domino-docker/SetupProfile.pds
Please refer the "domino-1.log" for a full log of successfull setup.

```

- Add to "hosts" file:

```
127.0.0.1 domino-1.hcl.xby.icdc.io

Notes:
1) if you setup additional servers, then do not forget to include them in the file:
domino-2.hcl.xby.icdc.io, ..., domino-N.hcl.xby.icdc.io

```

- Verify setup:
  https://code.xby.icdc.io/hcl/quattro/domino-keep/-/blob/develop/docs/060-docker-image.md#validation
