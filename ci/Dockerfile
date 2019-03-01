FROM openjdk:8-jdk

RUN apt-get update && apt-get install -y graphviz

RUN apt-get clean \
 && rm -rf /var/lib/apt/lists/*