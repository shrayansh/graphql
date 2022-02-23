FROM adoptopenjdk/openjdk8
SHELL ["/bin/bash", "-c"]
RUN apt-get update && apt-get -y install curl zip git
