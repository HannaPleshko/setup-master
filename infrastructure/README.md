- [Domino server set-up](#domino-server-set-up)
  - [Domino local prime server set-up](#domino-local-prime-server-set-up)
  - [Domino local secondary server set-up](#domino-secondary-server-set-up)
  - [Downloading user ids](#downloading-user-ids)
  - [Domino prime server set-up](#domino-prime-server-set-up)
  - [Process overview](#process-overview)
  - [User ids auto-register](#user-ids-auto-register)
- [Domino administrator](#Issues)
  - [Domino Administrator and Designer on IBA Cloud](#domino-administrator-and-designer-on-iba-cloud)
  - [Running Domino Administrator and Designer on MacOS and Linux under Wine](#running-domino-administrator-and-designer-on-macos-and-linux-under-wine)
  - [User ids register](#user-ids-register)
- [Domino Console](#domino-console)
- [Issues](#Issues)
  - [Logs stop collecting](#logs-stop-collecting)
  - [Open Relay](#open-relay)
  - [Domino Server does not up after down when using an existing volume](#domino-server-does-not-up-after-down-when-using-an-existing-volume)
  - [Databases corrupted](#databases-corrupted)
  - [SMTP configure](#smtp-configure)
- [Server tasks descriptions](#server-tasks-descriptions)
- [JMAP](#jmap)
  - [JMAP build docker image](#jmap-build-docker-image)
  - [JMAP deploy](#jmap-deploy)
- [Configuring Apache HTTP Server to access containers logs](#configuring-apache-http-server-to-access-containers-logs)
- [Links](#links)
## Domino server set-up
### Domino local prime server set-up
[Scripts and description](infrastructure/prime_local)
### Domino local secondary server set-up

#### Manual setup
- Сlone this repository
```
git clone git@code.xby.icdc.io:hcl/quattro/hcl-keep-setup.git
cd hcl-keep-setup/infrastructure/secondary_local (MacOS, Linux)
cd hcl-keep-setup\infrastructure\secondary_local (Windows)
```
- Download the required `server.id`
```
curl -u "Full Admin:ibahcl_admin_cPem9" https://prime.hcl.a.xby.icdc.io/secondaryID_NUM.id -o server.id
```
Replace `ID_NUM` with the required `server.id` number. 
`ID_NUM` ranges from 1 to 19.
- Login to docker-registry
```
docker login registry.xby.icdc.io
```
USERNAME: hcl

PASSWORD: Pe7bvQkXyepPs22C
- Pull the latest keep image
```
docker pull registry.xby.icdc.io/hcl-team/projectkeep-r12:1.0.43-SNAPSHOT
```
- Add to `/etc/hosts` (MacOS, Linux) or `C:\Windows\System32\drivers\etc\hosts` (Windows):
```
127.0.0.1 secondary.hcl.a.xby.icdc.io
```
- Up the container
```
docker-compose up -d
```

In the future, you may use

To stop the container:
```
docker stop keep
```
To start the container:
```
docker start keep
```
#### Auto setup

[Auto setup script and description for Windows](infrastructure/secondary_local_auto/Windows)

[Auto setup script and description for Linux and MacOS](infrastructure/secondary_local_auto/Linux_MacOS)
### Downloading user ids
``` bash
curl -u "Full Admin:ibahcl_admin_cPem9" -vvv http://prime.hcl.a.xby.icdc.io/ID_NAME.id -o ID_NAME.id
```
### Domino prime server set-up
[Сonfig](infrastructure/prime_new)
### Process overview
- /domino_docker_entrypoint.sh - container entrypoint
- /domino-docker/scripts/docker_prestart.sh - configure server based on environment variables
- /opt/hcl/domino/bin/java -jar /local/notesdata/DominoUpdateConfig.jar CONFIG.json - configure domino via Domino API

https://code.xby.icdc.io/hcl/quattro/domino-keep/-/blob/develop/docs/060-docker-image.md

https://help.hcltechsw.com/domino/12.0.0/admin/inst_onetouch_preparing_json.html
### User ids auto-register
/opt/hcl/domino/bin/java -jar /local/notesdata/[DominoUpdateConfigMod.jar](infrastructure/domino_config/DominoUpdateConfigMod.jar) [p_mta.json](infrastructure/domino_config/p_mta.json)

https://help.hcltechsw.com/dom_designer/10.0.1/basic/H_REGISTERNEWUSER_METHOD.html

https://help.hcltechsw.com/dom_designer/9.0.1/appdev/H_MAILINTERNETADDRESS_PROPERTY_REG_JAVA.html

https://github.com/IBM/domino-docker/blob/master/docs/configjson.md

https://github.com/Col-E/Recaf
## Domino administrator
Download link: `ftp:\\main2\install\notes\HCL Notes 11\Notes_Designer_Admin_1101_Win_English.exe`
### Domino administrator and designer on IBA Cloud
Windows based:
```
Address: small-river.hcl.a.xby.icdc.io:3390
Username: Administrator
Password: ERj1JeW7yGkQS4A!
```


### Running Domino Administrator and Designer on MacOS or Linux under Wine

Wine:
```
Download and setup instructions page: https://wiki.winehq.org/Download
```

Prepared archive for Wine:
```
Download link: https://disk.xby.icdc.io/index.php/s/g4SRi2cADFBQBCs/download?path=%2F&files=HCL.zip
Password: CMtbEZnj
```

HCL directory location: `"~/.wine/drive_c/Program Files (x86)/HCL"`

Start commands:
```
Notes client: wine start "C:\Program Files (x86)\HCL\Notes\notes.exe"
Domino Administrator: wine start "C:\Program Files (x86)\HCL\Notes\admin.exe"
Domino Designer: wine start "C:\Program Files (x86)\HCL\Notes\designer.exe"
```

### User ids register
People & Groups -> People -> Register -> login with cert.id -> Advanced -> Basics: First name, Last name, Short name, Password; Mail: Mail file template mail12.ntf; Address: Separator Dot, Internet Domain SERVER_URL -> v -> Register All

## Domino Console
The Domino Character Console provides a way to access the server console from the command line.

To access cconsole:
```
[...]# docker exec -ti keep bash
[notes@prime /]$ cd /local/notesdata/
[notes@prime notesdata]$ /opt/hcl/domino/bin/cconsole -f domino/html/admin.id
[...] Domino Character Console v0.0
[...] Warning:  Another console program is running on this machine.
[...] Your usage of certain commands, such as secure console
[...] commands, should be coordinated with other console users to
[...]  prevent conflict.
[...] Do you want to end this cconsole session? [y/n] n
[...] Warning:  You are remotely connected to host HOSTNAME
[...] If you have not taken precautions to secure this connection,
[...] passwords will be exposed over the network.
[...] Do you want to end this cconsole session? [y/n] n
[...] Enter your password:  
[...] TIMESTAMP  Initiating cconsole on HOSTNAME
>
```
To exit cconsole, type:
```
done
[...] TIMESTAMP Ending cconsole on HOSTNAME
```
Useful ```cconsole``` command:

`Restart Task taskname` - Shuts down and then restarts a specified server task.

`Show Tasks` - Displays the server name, the Domino program directory path, and the status of the active server tasks.

`Load taskname` - Loads and starts a specified server task or program on the Domino server.

`Stop taskname` - Stop a specified server task or program on the Domino server.

more: https://help.hcltechsw.com/domino/9.0.1/admin/admin/admn_servercommandsyntax_c.html
## Issues
### Logs stop collecting
`truncate -s 0 /var/lib/containers/storage/volumes/domino_keep/_data/IBM_TECHNICAL_SUPPORT/console.log`

If logs still not collecting
`cconsole`:
```
Stop Consolelog
Start Consolelog
```
### Open Relay
An open mail relay is a SMTP server configured in such a way that it allows anyone on the Internet to send e-mail through it, not just mail destined to or originating from known users.

Open -> Application -> Open an Application -> Look in: SERVER_NAME -> File name: names.nsf -> Configuration -> Servers -> Configurations -> select SERVER_NAME -> Router/SMTP -> Restrictions and Controls -> SMTP Inbound Controls -> Deny messages  to be sent to the following external internet domains: (* means all): * -> Save & Close
### Domino Server does not up after down when using an existing volume
```bash
docker exec -ti keep bash
/bin/sh /opt/nashcom/startscript/rc_domino_script stop
/bin/sh /opt/nashcom/startscript/rc_domino_script start
```
### Databases corrupted:
Fix corrupted views and documents:
`/opt/hcl/domino/bin/fixup DB_NAME`

options: https://help.hcltechsw.com/domino/11.0.0/admn_fixupoptions_r.html

Compact database:
`/opt/hcl/domino/bin/compact -D -c -i -n -v -ZU DB_NAME`

-D - Discards built view indexes

-c - Uses copy-style compacting

-i - Enables compacting to continue even if it encounters errors such as document corruption. Only used for copy-style compacting

-n - Enables Compress database design

-v - Enables Compress document data

-ZU - Upgrades attachments to LZ1 compression from Huffman compression

options: https://help.hcltechsw.com/domino/11.0.0/tune_compactoptions_r.html

The Updall task manages database view indexes and full-text indexes:

`/opt/hcl/domino/bin/updall -R DB_NAME`

-R - Rebuilds full-text indexes and does not rebuild views. Use to rebuild full-text
indexes that are corrupted

`/opt/hcl/domino/bin/updall -X DB_NAME`

-X - Rebuilds all used views. Using this option is resource-intensive, so use it as a last resort to solve corruption problems with a specific database.

options: https://webcache.googleusercontent.com/search?q=cache:Dj4v7q1MvPgJ:https://www.ibm.com/docs/fi/SSKTMJ_9.0.1/admin/admn_updalloptions_r.html

DB_NAME  can be null, in this case operation are performed for all db in current folder.
## SMTP configure
Typically, SMTP is already configured after the initial configuration script and does not need additional configuration.

Domain documents creation:

Open -> Application -> Open an Application -> Look in: SERVER_NAME -> File name: names.nsf -> Configuration -> Messaging -> Domains -> Add Domain ->
- Basics: Domain type: Foreign SMTP Domain; Routing: Internet Domain: \*.\*, Domain name: Internet.
- Basics: Domain type: Global Domain, Global domain role: R5/R6/R7/R8 Internet Domains or R4.x SMTP MTA; Restrictions: Domino domains and aliases: DOMAIN_NAME; Conversions: Local primary Internet domain: HOST_NAME.
## Server tasks descriptions
- replica - Replicates databases with other servers
- router - Routes mail from mail.box
- update - Keep view indexes and full-text indexes up-to-date
- amgr - Runs agents on one or more databases
- adminp - Automates a variety of administrative tasks
- http - Enables a Domino server to act as a Web server so browser clients can access databases on the server
- certmgr - Works with a new database, Certificate Store (certstore.nsf) to automate generation of TLS certificates from the Let's Encrypt certificate authority (CA) or another third-party CA
- smtp - Listens for incoming SMTP connections, enabling Domino to receive mail from other SMTP hosts
- keep - Keep task

more: https://help.hcltechsw.com/domino/10.0.1/admn_dominoservertasks_r.html
## JMAP
### JMAP build docker image
```
git clone git@code.xby.icdc.io:hcl/quattro/openclient-keepcomponent.git
vi .npmrc
# @hcllabs:registry=ARTIFACTORY
# AUTH_STRING
rm package-lock.json
npm package
git clone git@code.xby.icdc.io:hcl/quattro/jmap-middleware.git
vi .npmrc
# @hcllabs:registry=ARTIFACTORY
# AUTH_STRING
rm package-lock.json
vi package.json
# @hcllabs/openclientkeepcomponent": "OPENCLIENT_VER"
vi deploy/dockerfile
# RUN npm install @types/uuid
# RUN --mount=type=secret,id=npmrcpat,dst=.npmrc npm install --unsafe-perm
DOCKER_BUILDKIT=1 docker build -t jmap-middleware-test:jmap-test2 -f deploy/dockerfile .
docker tag jmap-middleware-test:jmap-test2 registry.xby.icdc.io/hcl-team/jmap-middleware-test:jmap-test2
docker login registry.xby.icdc.io
docker push registry.xby.icdc.io/hcl-team/jmap-middleware-test:jmap-test2
```
### JMAP deploy
``` bash
docker login registry.xby.icdc.io
docker pull registry.xby.icdc.io/hcl-team/jmap-middleware-test:jmap-test2
docker inspect keep | grep IPAddress
docker run -e KEEP_BASE_URL=http://KEEP_SERVER_URL -e NODE_ENV:'development' -e SERVER_URL=https://JMAP_SERVER_URL -p 3001:3001 --name="jmap" -d jmap-middleware-test:jmap-test2
```
lttrs apk: https://f-droid.org/en/packages/rs.ltt.android/
## Configuring Apache HTTP Server to access containers logs
[httpd.conf](infrastructure/apache/httpd.conf)
``` bash
docker cp httpd.conf jmap-log:/usr/local/apache2/conf/httpd.conf
docker exec -ti jmap-log apachectl graceful
docker run -dit --name jmap-log -p HOST_PORT:80 httpd:2.4
docker exec -ti jmap-logs bash
htpasswd -c /usr/local/apache2/.htpasswd USERNAME
apt update
apt install openssh-client
chown -R daemon:daemon /usr/sbin
su daemon
ssh-keygen
ssh-copy-id root@DOCKER_HOST
cat cgi-bin/jmap_prime_logs
  #!/bin/sh
  ssh root@DOCKER_HOST docker logs CONTAINER_NAME
```
## Links
https://mxtoolbox.com/SuperTool.aspx

https://help.hcltechsw.com/domino/11.0.0/index.html

https://opensource.hcltechsw.com/domino-keep-docs/
