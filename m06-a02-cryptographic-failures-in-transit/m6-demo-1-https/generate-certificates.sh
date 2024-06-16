#!/bin/bash
set -e

keytool -genkeypair -alias demo -keyalg ECC -keysize 256 -keystore src/main/resources/demo-keystore.p12 -storetype PKCS12 -validity 365 -dname "CN=localhost, OU=Dev, O=MyCompany, L=City, ST=State, C=US" -storepass notsecure -keypass notsecure

keytool -export -alias demo -keystore src/main/resources/demo-keystore.p12 -storepass notsecure -file src/main/resources/demo-cert.cer

keytool -import -alias demo -file src/main/resources/demo-cert.cer -keystore src/test/resources/demo-truststore.p12 -storetype PKCS12 -storepass notsecure -noprompt
