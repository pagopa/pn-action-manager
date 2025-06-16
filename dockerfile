FROM localstack/localstack:latest

USER root

RUN mkdir -p /var/lib/localstack && \
    chown -R localstack:localstack /var/lib/localstack

RUN apt-get update && \
    apt-get install -y curl jq zip docker.io && \
    usermod -aG docker localstack && \
    rm -rf /var/lib/apt/lists/*

USER localstack