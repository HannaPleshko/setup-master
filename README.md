# General

- Follow instructions: DeveloperSetup.doc (skip VSCode IDE plugins: all LiveShare plugins, GitLens), skip installing Android Studio

- Add your personal public SSH key to your GitLab profile: https://code.xby.icdc.io/-/profile/keys

- Clone projects: https://code.xby.icdc.io/dashboard/projects

# Links

## Onboarding materials:

```
https://disk.xby.icdc.io/index.php/s/g4SRi2cADFBQBCs/authenticate/showShare
Password: CMtbEZnj
```

## Notes Id's:

```
admin-cloud.id: ibahcl_admin-qZhB4
cert.id: ibahcl_org-G5mWp
server.id: <no_password>
pavel.lihtaro@mail.hcl.a.xby.icdc.io: IBA#2021Minsk
Sergey.Petushkov@mail.hcl.a.xby.icdc.io: IBA#2021Minsk
```

## GitLab

```
https://code.xby.icdc.io/
```

## Jira

```
https://jira.dc-iba.by/secure/Dashboard.jspa
```

## Domino Keep API

```
http://small-river.hcl.a.xby.icdc.io:8880/openapi/index.html?url=/api/v1/schema/openapi.core.json
```

## EWS API

```
https://docs.microsoft.com/en-us/exchange/client-developer/web-service-reference/ews-operations-in-exchange
```

## IMAP API

```
https://datatracker.ietf.org/doc/html/rfc3501
```

# OpenShift

## Prerequisites

Add to OS `hosts` file the following entries:

```
178.172.233.136 api.quattro.hcl.lab.xby.icdc.io
178.172.233.136 console.quattro.hcl.lab.xby.icdc.io
178.172.233.136 oauth-openshift.apps.quattro.hcl.lab.xby.icdc.io
```

> Temporarily solution due to network issue

## WEB Access

OpenShift Console:

https://console.quattro.hcl.lab.xby.icdc.io

Application Access:

| Access  | Route                               |
| ------- | ----------------------------------- |
| Private | \*.apps.quattro.hcl.lab.xby.icdc.io |
| Public  | \*.svc.quattro.hcl.lab.xby.icdc.io  |

## CLI Access

OpenShift Client:

- [openshift-client-linux](https://github.com/openshift/okd/releases/download/4.7.0-0.okd-2021-07-03-190901/openshift-client-linux-4.7.0-0.okd-2021-07-03-190901.tar.gz)
- [openshift-client-mac](https://github.com/openshift/okd/releases/download/4.7.0-0.okd-2021-07-03-190901/openshift-client-mac-4.7.0-0.okd-2021-07-03-190901.tar.gz)
- [openshift-client-windows](https://github.com/openshift/okd/releases/download/4.7.0-0.okd-2021-07-03-190901/openshift-client-windows-4.7.0-0.okd-2021-07-03-190901.zip)

OC Login:

```
oc login api.quattro.hcl.lab.xby.icdc.io:6443 --username=<email>
```

# VSCode IDE
- Make Prettier settings same as on the pic:
```
./vscode/prettier.png
```

- Make Mocha settings same as on the pic:
```
./vscode/mocha.png
```

- Place 2 prettier config files in a root of every project:
```
./vscode/.prettierignore
./vscode/.prettierrc.js
```

- Create a debug config file (launch.json):
```
./vscode/launch.json
```

# EWS

- Create a separate branch for your changes:

```
git pull && git checkout -b ci/setup
rm -rf node_modules
```

- Edit the .npmrc file:

```
@hcllabs:registry=https://artifactory.xby.icdc.io/repository/npm-hosted/

always-auth=true
```

- Authenticate with a local npm repository:

```
npm login --registry=https://artifactory.xby.icdc.io/repository/npm-hosted/ --scope=@hcllabs --always-auth

  Username: hcllabs
  Password: it142ue71LX|3W+L
  Email: admin@mail.hcl.a.xby.icdc.io

npm config set @hcllabs:registry https://artifactory.xby.icdc.io/repository/npm-hosted
```

**The changes are reflected in the ./.npmrc file.**

- Install npm packages:

```
npm cache clean --force
npm install
```

- Run ngrok:

```
signin / signup at https://.ngrok.com
https://dashboard.ngrok.com/get-started/setup -> 2. Connect your account -> copy and past to console,
example:
./ngrok authtoken 1tnAFdrMNCvIkPPK9cfskX4Nr1R_5tY9wW6NsPixkuBNN2dfg2

./ngrok http 3000
```

- Start server:

```
npm run start:dev
```

- Run setup scripts from a package.json file:

```
- What is the proper order of commands to run a EWS project?
- David Kennedy: Of the scripts in package.json, I typically run clean, build, start
```

- Setup VSCode debug config for Mocha tests

**launch.json**

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": "Mocha Tests",
      "type": "pwa-node",
      "request": "launch",
      "program": "${workspaceFolder}/node_modules/mocha/bin/_mocha",
      "env": {
        "TS_NODE_FILES": "true"
      },
      "args": [
        "-r",
        "ts-node/register",
        "--timeout",
        "999999",
        "--recursive",
        "--colors",
        "${workspaceFolder}/src/tests/**/mail.test.ts"
      ],
      "internalConsoleOptions": "openOnSessionStart",
      "skipFiles": ["<node_internals>/**"],
      "sourceMaps": true,
      "outFiles": ["${workspaceFolder}/src/**/*.ts"]
    },
    {
      "type": "pwa-node",
      "request": "launch",
      "name": "npm run start:dev",
      "skipFiles": ["<node_internals>/**"],
      "program": "${workspaceFolder}/index.js",
      "preLaunchTask": "tsc: build - tsconfig.json",
      "outFiles": ["${workspaceFolder}/dist/**/*.js"],
      "env": {
        "NODE_ENV": "development"
      }
    }
  ]
}
```

- Follow all instructions from the /docs dir.

- Create '.env' file in the project's root directory

```
KEEP_BASE_URL = http://small-river.hcl.a.xby.icdc.io:8880
```

- Provide a valid user name / password in the **'/test/ews.postman_environment.json'** file.

```
...
 {
  "key": "email",
  "value": Dev.User1@mail.hcl.a.xby.icdc.io
  "enabled": true
},
...
{
  "key": "password",
  "value": "IBA#2021Minsk",
  "enabled": true
}
```

- Run tests:

```
npm run start:dev
npm run testPR (in a separate terminal)
```

### Windows environment

**package.json**

- change `env name=value` to `set name=value`, e.g.:

  from

  ```
    "start:dev": "env NODE_ENV=development npm start",
    "test:unit": "env TS_NODE_FILES=true mocha -r ts-node/register 'src/tests/**/*.ts'"
  ```

  to

  ```
    "start:dev": "set NODE_ENV=development && npm start",
    "test:unit": "set TS_NODE_FILES=true && mocha -r ts-node/register 'src/tests/**/*.ts'",
  ```

- replace `open` to `start`, e.g.:

  from

  ```
    "viewPRTestReport": "open ./reports/ews_postman_report.html",
    "coverage": "open coverage/index.html"
  ```

  to

  ```
    "viewPRTestReport": "start ./reports/ews_postman_report.html",
    "coverage": "start coverage/index.html"
  ```

# Install yalc and a local openclient-keepcomponent

- Install yalc:

```
https://github.com/wclr/yalc
```

- Clone and open the openclient-keepcomponent project in your IDE, then run:

```
yalc publish
```

This will publish the project locally in your OS environment as a npm package, yalc will remember this link.

- Open EWS project and run:

```
npm remove @hcllabs/openclientkeepcomponent
yalc add @hcllabs/openclientkeepcomponent
```

This will refer to a local copy of the @hcllabs/openclientkeepcomponent project.

- To update a local hcllabs/openclientkeepcomponent project run:

```
yalc update @hcllabs/openclientkeepcomponent
```

- To remove a local hcllabs/openclientkeepcomponent project, run:

```
yalc remove @hcllabs/openclientkeepcomponent
```

# Useful commands (for Mac / Linux):

List running Docker containers:

```
docker ps
```

Docker container logs

```
docker logs <container_name>
```

Docker container bash console:

```
docker exec -it <container_name> bash
```

Docker volume list / create / delete:

```
docker volume list
docker volume create <volume_name>
docker volume rm <volume_name>
```

List all serving ports:

```
netstat -tnlp
```

List / kill running processes:

```
ps aux
ps aux | grep notes

kill -9 <PID>
```

Find / read / edit file:

```
cd /
find -name <file_name>
cd <file_dir>

cat ./<file_name>

vi ./<file_name>
нажать i
ctrl + c, :wq или :q!
```

Start / stop Domino server:

```
/bin/sh /opt/nashcom/startscript/rc_domino_script start
/bin/sh /opt/nashcom/startscript/rc_domino_script stop
```

Make a file executable and run it:

```
chmod +x <file_name>.sh
./<file_name>.sh
```

Example of a \*.ch file:

```
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
```
