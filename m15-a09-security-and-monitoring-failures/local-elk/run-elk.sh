#!/bin/bash
set -ev

# Build the custom ELK image
docker build -t custom-elk .

CONTAINER_NAME="elk"

if [ "$(docker ps -q -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "Container $CONTAINER_NAME is already running. Stopping it..."
    docker stop $CONTAINER_NAME
    echo "Container stopped."
fi

# Check if the container exists and is not running (stopped)
if [ "$(docker ps -aq -f status=exited -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "Container $CONTAINER_NAME exists but is not running. Removing it..."
    docker rm $CONTAINER_NAME
    echo "Container removed."
fi

# Run the custom ELK container
docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -d --name elk custom-elk

docker logs elk --follow
