set NEWLINE=^& echo.
find /C /I "127.0.0.1 local_prime.hcl.a.xby.icdc.io" %WINDIR%\system32\drivers\etc\hosts
if %ERRORLEVEL% NEQ 0 echo %NEWLINE%^127.0.0.1 local_prime.hcl.a.xby.icdc.io>>%WINDIR%\System32\drivers\etc\hosts

set cmd=docker

set images=`%cmd% images --format "{{.Repository}}:{{.Tag}}"`
set image_exists=`echo %images% | findstr "registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1"`

if "$image_exists"=="" (
  echo "Downloading image:"
  %cmd% login registry.xby.icdc.io
  %cmd% pull registry.xby.icdc.io/hcl-team/projectkeep-r12:0.9.25-EARLYACCESS-1
)

docker-compose up -d
