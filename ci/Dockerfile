FROM adoptopenjdk/openjdk8:latest

RUN apt-get update
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get install --no-install-recommends -y graphviz jq gpg ca-certificates net-tools curl

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*
