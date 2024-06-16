#!/bin/bash
set -e

# Server

keytool -genkeypair -alias server -keyalg RSA -keysize 4096 -keystore src/main/resources/demo-server-keystore.p12 -storetype PKCS12 -validity 365 -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=US" -storepass notsecure -keypass notsecure

keytool -export -alias server -keystore src/main/resources/demo-server-keystore.p12 -storepass notsecure -file src/main/resources/demo-server-cert.cer

keytool -import -alias server -file src/main/resources/demo-server-cert.cer -keystore src/test/resources/demo-client-truststore.p12 -storetype PKCS12 -storepass notsecure -noprompt

# Client

keytool -genkeypair -alias client -keyalg RSA -keysize 4096 -keystore src/test/resources/demo-client-keystore.p12 -storetype PKCS12 -validity 365 -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=US" -storepass notsecure -keypass notsecure

keytool -export -alias client -keystore src/test/resources/demo-client-keystore.p12 -storepass notsecure -file src/test/resources/demo-client-cert.cer

keytool -import -alias client -file src/test/resources/demo-client-cert.cer -keystore src/main/resources/demo-server-truststore.p12 -storetype PKCS12 -storepass notsecure -noprompt
