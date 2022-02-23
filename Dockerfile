FROM adoptopenjdk/openjdk8
SHELL ["/bin/bash", "-c"]
RUN apt-get update && apt-get -y install curl zip git && \
    curl -s "https://get.sdkman.io" | bash && \
    source "$HOME/.sdkman/bin/sdkman-init.sh" && \
    sdk install sbt

ENV PATH="/root/.sdkman/candidates/sbt/current/bin:${PATH}"
