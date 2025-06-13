FROM localstack/localstack:4.0.3
USER root

RUN apt-get update && \
    apt-get install -y curl jq && \
    apt-get clean && \
    echo "done"

RUN apt-get update && apt-get install -y zip

RUN mkdir -p /var/lib/localstack && chown -R localstack:localstack /var/lib/localstack

USER localstack

EXPOSE 4566 4510-4559