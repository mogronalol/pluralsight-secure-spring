#!/bin/bash
set -e

echo "Starting elasticsearch..."

service elasticsearch start

# Wait for Elasticsearch to be available
until curl -u elastic:changeme -s -k https://localhost:9200/_cat/health; do
    echo "Waiting for Elasticsearch..."
    sleep 5
done

echo "Elasticsearch started, setting up passwords..."

curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/kibana_system/_password" -H "Content-Type: application/json" -d '{"password":"kibana_password"}' && \
curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/logstash_system/_password" -H "Content-Type: application/json" -d '{"password":"logstash_password"}' && \
curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/beats_system/_password" -H "Content-Type: application/json" -d '{"password":"beats_password"}' && \
curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/apm_system/_password" -H "Content-Type: application/json" -d '{"password":"apm_password"}' && \
curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/remote_monitoring_user/_password" -H "Content-Type: application/json" -d '{"password":"monitoring_password"}'
curl -f -k -u "elastic:changeme" "https://localhost:9200/_security/user/elastic/_password" -H "Content-Type: application/json" -d '{"password":"elastic_password"}'


echo starting logstash

service logstash start

/opt/kibana/bin/kibana --allow-root &

echo "Waiting for Kibana to be fully operational..."
until curl --output /dev/null --silent --head --fail http://localhost:5601/api/status; do
    printf 'Waiting for Kibana to be full operational'
    sleep 5
done
echo "Kibana is up."

#!/bin/bash
set -e

echo  "Creating Kibana data view..."
curl -f -u "elastic:elastic_password" -v "http://localhost:5601/api/saved_objects/index-pattern" \
     -H 'kbn-xsrf: true' -H 'Content-Type: application/json' \
     -d'{"attributes":{"title":"logstash-*","timeFieldName":"@timestamp"}}'

echo ""
echo "Data view created."
echo "Creating policy..."
echo ""

auth="elastic:elastic_password"
content_type="application/json"
kbn_xsrf_header="kbn-xsrf: true"

# API Endpoint
api_endpoint="http://localhost:5601/api/fleet/agent_policies"

# Policy Details
policy_name="Agent policy 1"

# Step 1: Check if the policy exists
response=$(curl -s -u $auth -H "$kbn_xsrf_header" -H "Content-Type: $content_type" "$api_endpoint")
policy_id=$(echo "$response" | jq -r ".items[] | select(.name==\"$policy_name\") | .id")

# Step 2: Create the policy if it does not exist
if [ -z "$policy_id" ]; then
  echo "Policy does not exist, creating..."
  create_response=$(curl -s -o /dev/null -w '%{http_code}' -X POST -u $auth \
    -H "$kbn_xsrf_header" \
    -H "Content-Type: $content_type" \
    -d '{
      "name": "'"$policy_name"'",
      "description": "",
      "namespace": "default",
      "monitoring_enabled": ["logs", "metrics"],
      "inactivity_timeout": 1209600,
      "is_protected": false
    }' \
    "$api_endpoint")

  # Check if creation was successful
  if [ "$create_response" -eq 200 ]; then
    echo "Policy created successfully."
    # Fetch the policy ID again
    policy_id=$(curl -s -u $auth -H "$kbn_xsrf_header" -H "Content-Type: $content_type" "$api_endpoint" | jq -r ".items[] | select(.name==\"$policy_name\") | .id")
  else
    echo "Failed to create policy, status code: $create_response"
    exit 1
  fi
else
  echo "Policy already exists with ID: $policy_id"
fi

# Step 3: Retrieve the policy details
if [ ! -z "$policy_id" ]; then
  policy_details=$(curl -f -s -u $auth -H "$kbn_xsrf_header" -H "Content-Type: $content_type" "$api_endpoint/$policy_id")
  echo "Policy Details: $policy_details"
else
  echo "Error: Failed to retrieve policy details."
fi

echo "Found policy ID ${policy_id}"

echo "Integrating.. with spring boot"

curl -f -s -X POST "http://localhost:5601/api/fleet/package_policies" \
  -H 'Content-Type: application/json' \
  -H 'kbn-xsrf: true' \
  -u $auth \
  -d '{
       "policy_id": "'"$policy_id"'",
       "package": {
         "name": "spring_boot",
         "version": "1.4.0"
       },
       "name": "spring_boot-1",
       "description": "",
       "namespace": "",
       "inputs": {
         "spring_boot-httpjson": {
           "enabled": true,
           "vars": {
             "hostname": "http://host.docker.internal:8080",
             "ssl": false
           },
           "streams": {
             "spring_boot.audit_events": {
               "enabled": true,
               "vars": {
                 "period": "60s",
                 "tags": ["spring_boot.audit_events.metrics"],
                 "preserve_original_event": false
               }
             },
             "spring_boot.http_trace": {
               "enabled": true,
               "vars": {
                 "period": "60s",
                 "tags": ["spring_boot.http_trace.metrics"],
                 "preserve_original_event": false
               }
             }
           }
         },
         "spring_boot-jolokia/metrics": {
           "enabled": true,
           "vars": {
             "path": "/actuator/jolokia/",
             "hosts": "http://host.docker.internal:8080",
             "ssl": false
           },
           "streams": {
             "spring_boot.gc": {
               "enabled": true,
               "vars": {
                 "period": "60s",
                 "tags": ["spring_boot.gc.metrics"]
               }
             },
             "spring_boot.memory": {
               "enabled": true,
               "vars": {
                 "period": "60s",
                 "tags": ["spring_boot.memory.metrics"]
               }
             },
             "spring_boot.threading": {
               "enabled": true,
               "vars": {
                 "period": "60s",
                 "tags": ["spring_boot.threading.metrics"]
               }
             }
           }
         }
       }
     }'

echo "Set up spring integration, fetching policy data..."

# Fetch the policy data using curl
response=$(curl -s -u elastic:elastic_password "http://localhost:5601/api/fleet/agent_policies/${policy_id}/full?standalone=true&kubernetes=false")

echo "Fetched policy data..."
echo "$response"

# Modify the response with jq to update username and password
modified_response=$(echo "$response" | jq '.item' | jq '
  .outputs.default.username = "elastic" |
  .outputs.default.password = "elastic_password" |
  .outputs.default.hosts[0] |= sub("http"; "https") |
  .outputs.default.ssl.verification_mode = "none" |
  (.. | strings) |= gsub("/actuator/httptrace"; "/actuator/httpexchanges") |
  (.. | strings) |= gsub("body.traces"; "body.exchanges") |
  .inputs[].streams[] |= (
        if .data_stream.dataset == "spring_boot.http_trace" then
          .processors += [{
            "rename": {
              "fields": [
                { "from": "*", "to": "http.*" }
              ],
              "ignore_missing": true,
              "fail_on eye_or": false
            }
          }]
        else
          .
        end
)')

echo "$modified_response"

echo "Writing modified response to /opt/elastic-agent/elastic-agent.yml"

# Print the modified response, convert to YAML using yq
echo "$modified_response" | yq eval -P > /opt/elastic-agent/elastic-agent.yml

elastic-agent run -e -v
tail -f /dev/null

