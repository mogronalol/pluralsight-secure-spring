FROM sebp/elk:8.13.4

# Install necessary utilities
RUN apt-get update && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:rmescandon/yq && \
    apt-get update && \
    apt-get install -y unzip jq yq

COPY elastic-agent-8.13.4-linux-x86_64.tar.gz /tmp/elastic-agent-8.13.4-linux-x86_64.tar.gz
RUN mkdir -p /opt/elastic-agent
RUN tar -xzf /tmp/elastic-agent-8.13.4-linux-x86_64.tar.gz -C /opt/elastic-agent --strip-components=1
RUN rm /tmp/elastic-agent-8.13.4-linux-x86_64.tar.gz
ENV PATH="/opt/elastic-agent:${PATH}"

ENV ELASTICSEARCH_START=0
ENV KIBANA_START=0
ENV LOGSTASH_START=0
ENV ES_PROTOCOL=https

# Create Elasticsearch keystore and add bootstrap password
RUN if [ ! -f /opt/elasticsearch/elasticsearch.keystore ]; then \
        /opt/elasticsearch/bin/elasticsearch-keystore create; \
    fi && \
    echo "changeme" | /opt/elasticsearch/bin/elasticsearch-keystore add -xf bootstrap.password

# Generate CA and unzip it
RUN /opt/elasticsearch/bin/elasticsearch-certutil ca --pem --out /etc/elasticsearch/elastic-stack-ca.zip --pass ""
RUN unzip /etc/elasticsearch/elastic-stack-ca.zip -d /etc/elasticsearch

# Generate certificates based on configuration
RUN mkdir -p /opt/elasticsearch/config && \
    echo "instances:" > /opt/elasticsearch/config/instances.yml && \
    echo "  - name: \"instance\"" >> /opt/elasticsearch/config/instances.yml && \
    echo "    ip:" >> /opt/elasticsearch/config/instances.yml && \
    echo "      - \"127.0.0.1\"" >> /opt/elasticsearch/config/instances.yml && \
    echo "    dns:" >> /opt/elasticsearch/config/instances.yml && \
    echo "      - \"localhost\"" >> /opt/elasticsearch/config/instances.yml && \
    /opt/elasticsearch/bin/elasticsearch-certutil cert --ca-cert /etc/elasticsearch/ca/ca.crt --ca-key /etc/elasticsearch/ca/ca.key --in /opt/elasticsearch/config/instances.yml --out /etc/elasticsearch/elastic-certificates.zip --pass "changeit"
RUN unzip /etc/elasticsearch/elastic-certificates.zip -d /etc/elasticsearch && \
    chown -R elasticsearch:elasticsearch /etc/elasticsearch

RUN rm /etc/logstash/conf.d/*
COPY logstash.conf /etc/logstash/conf.d/logstash.conf

# Configure SSL/TLS for Elasticsearch
RUN echo "\
xpack.security.enabled: true\n\
xpack.security.transport.ssl.enabled: true\n\
xpack.security.transport.ssl.verification_mode: certificate\n\
xpack.security.transport.ssl.keystore.path: /etc/elasticsearch/instance/instance.p12\n\
xpack.security.transport.ssl.keystore.password: changeit\n\
xpack.security.transport.ssl.truststore.path: /etc/elasticsearch/instance/instance.p12\n\
xpack.security.transport.ssl.truststore.password: changeit\n\
xpack.security.http.ssl.enabled: true\n\
xpack.security.http.ssl.keystore.path: /etc/elasticsearch/instance/instance.p12\n\
xpack.security.http.ssl.keystore.password: changeit\n\
xpack.security.http.ssl.truststore.path: /etc/elasticsearch/instance/instance.p12\n\
xpack.security.http.ssl.truststore.password: changeit\n\
" > /etc/elasticsearch/elasticsearch.yml

# Configure Kibana to use the created user and enable xpack security
RUN echo "\
elasticsearch.username: \"kibana_system\"\n\
elasticsearch.password: \"kibana_password\"\n\
elasticsearch.hosts: [\"https://localhost:9200\"]\n\
elasticsearch.ssl.certificateAuthorities: [\"/etc/elasticsearch/ca/ca.crt\"]\n\
elasticsearch.ssl.verificationMode: full\n\
" >> /opt/kibana/config/kibana.yml

# Expose the usual ports
EXPOSE 5601 9200 5044

COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
