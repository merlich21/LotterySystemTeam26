#!/bin/bash

read -s -p "Write DB password: " password

if [ $(ls -la | grep run-db.sh | wc -l) -gt 0 ]; then
    docker stop hackathon_postgres
    docker rm hackathon_postgres
    docker rmi hackathon_postgres:latest
    # echo -e "Write ur Docker Hub credentials to pull originall PostgreSQL image\n"
    # docker login dhi.io
    # docker pull dhi.io/postgres:18.3-alpine3.22-dev
    docker pull postgres:18.3-alpine
    docker build -t hackathon_postgres -f db-dockerfile .
    echo -e "\nThe 'hackathon_postgres' should be built\n"
    docker run --name hackathon_postgres -p 5432:5432 -e POSTGRES_PASSWORD=$password -v postgres_data:/var/lib/postgresql/18/docker -v postgres_main:/var/lib/postgresql -d hackathon_postgres:latest
    echo -e "\n_____INFORMATION:_____"
    echo -e "\nThe 'hackathon_postgres' container should be running. Use 127.0.0.1:5432 to connect to PostgreSQL. Login: postgres, DB: postgres"
    echo "Use 'docker start hackathon_postgres' command to start container, if it stops"
else
    echo "You must be in script dir"
fi