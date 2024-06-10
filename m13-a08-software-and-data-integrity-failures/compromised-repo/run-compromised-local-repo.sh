#!/bin/bash
set -e

# Define the container and image names
CONTAINER_NAME="compromised-repo"
IMAGE_NAME="compromised-repo"

# Build the Docker image
docker build -t $IMAGE_NAME .

# Check if the container is already running, and stop and remove it if it is
if docker ps -a | grep -q $CONTAINER_NAME; then
    echo "Container $CONTAINER_NAME is running. Stopping and removing..."
    docker stop $CONTAINER_NAME
    docker rm $CONTAINER_NAME
fi

# Run the Docker container
docker run -d -p 8080:80 --name $CONTAINER_NAME $IMAGE_NAME
