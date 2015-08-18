# Opendaylight SNMP Plugin
https://wiki.opendaylight.org/view/SNMP_Plugin:Main


### Build:
Run `mvn clean install` from this directory

### Install:
Install a bundle by either copying it to the plugins directory, or running `install <url>` from osgi.

The following bundles are required:

Bundle | Location | Alternate Location
------------ | ------------- | ------------
SNMP4J | ~/.m2/repository/org/apache/servicemix/bundles/org.apache.servicemix.bundles.snmp4j/2.3.1_1/org.apache.servicemix.bundles.snmp4j-2.3.1_1.jar| http://central.maven.org/maven2/org/apache/servicemix/bundles/org.apache.servicemix.bundles.snmp4j/2.3.1_1/org.apache.servicemix.bundles.snmp4j-2.3.1_1.jar
mibs-model | mibs-model/target/mibs-model-XXXX.jar  | 
snmp | snmp/target/snmp-XXXX-SNAPSHOT.jar

