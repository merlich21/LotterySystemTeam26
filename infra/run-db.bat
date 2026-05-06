@echo off
setlocal enabledelayedexpansion

for /f "usebackq delims=" %%i in (`powershell -Command "$securePassword = Read-Host 'Write DB password' -AsSecureString; $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword); $plainTextPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR); [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR); Write-Output $plainTextPassword"`) do set "password=%%i"

set "script_path=%~dp0"
if "!script_path:~-1!"=="\" set "script_path=!script_path:~0,-1!"

if /i "!script_path!"=="%cd%" (
    docker stop hackathon_postgres
    docker rm hackathon_postgres
    docker rmi hackathon_postgres:latest
    @REM echo Write ur DockerHub credentials to pull originall PostgreSQL image
    @REM echo .
    @REM docker login dhi.io
    @REM docker pull dhi.io/postgres:18.3-alpine3.22-dev
    docker pull postgres:18.3-alpine
    docker build -t hackathon_postgres -f db-dockerfile .
    docker network create hackathon_network
    echo .
    echo The 'hackathon_postgres' image should be built
    echo .
    docker run --name hackathon_postgres --network hackathon_network -p 7432:5432 -e POSTGRES_PASSWORD=!password! -e POSTGRES_DB=lottery_db -v postgres_data:/var/lib/postgresql/18/docker -v postgres_main:/var/lib/postgresql -d hackathon_postgres:latest
    echo .
    echo _____INFORMATION:_____
    echo .
    echo The 'hackathon_postgres' container should be running. Use 127.0.0.1:7432 to connect to PostgreSQL. Login: postgres, DB: lottery_db.
    echo Use 'docker start hackathon_postgres' command to start container, if it stops
) else (
    echo !password!
    echo You must be in the script dir!
)
pause
endlocal