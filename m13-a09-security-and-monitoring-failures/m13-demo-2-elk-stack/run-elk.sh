set -e

docker pull sebp/elk

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

docker run -p 5601:5601 -p 9200:9200 -p 5044:5044 -d --name elk \
  -v ./logstash:/etc/logstash/conf.d/ \
  sebp/elk

while ! docker ps | grep -q 'elk'; do
    sleep 1
    echo "Waiting for the container to start..."
done

# Wait for Kibana to be fully ready
echo "Waiting for Kibana to be fully operational..."
until curl --output /dev/null --silent --head --fail http://localhost:5601/api/status; do
    printf '.'
    sleep 5
done
echo "Kibana is up."

echo "Creating Kibana data view..."
curl -v -X POST "http://localhost:5601/api/saved_objects/index-pattern" \
     -H 'kbn-xsrf: true' -H 'Content-Type: application/json' \
     -d'{"attributes":{"title":"logstash-*","timeFieldName":"@timestamp"}}'

echo "Data view created."

docker logs elk --follow