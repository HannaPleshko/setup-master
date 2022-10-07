set NEWLINE=^& echo.
find /C /I "127.0.0.1 secondary.hcl.a.xby.icdc.io" %WINDIR%\system32\drivers\etc\hosts
if %ERRORLEVEL% NEQ 0 echo %NEWLINE%^127.0.0.1 secondary.hcl.a.xby.icdc.io>>%WINDIR%\System32\drivers\etc\hosts
curl -u "Full Admin:ibahcl_admin_cPem9" https://prime.hcl.a.xby.icdc.io/secondary%1.id -o server.id
docker login registry.xby.icdc.io
docker pull registry.xby.icdc.io/hcl-team/projectkeep-r12:1.0.43-SNAPSHOT
docker-compose up -d
