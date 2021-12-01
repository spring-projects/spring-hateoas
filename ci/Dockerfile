FROM adoptopenjdk/openjdk8:latest

RUN apt-get update
RUN apt-get install -y graphviz jq gpg

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
