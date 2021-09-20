FROM openjdk:17-bullseye

RUN sed -i -e 's/http/https/g' /etc/apt/sources.list

RUN apt-get update && apt-get install -y graphviz jq gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
