# Efficient Logfile Analysis

## Requirements

* JDK 17 or above
* Apache Maven (to build the project)
* Glassfish6 installed (to run the project)

-----

## Building

Navigate to the project folder in a Terminal. <br>
```
mvn package
```
Output .war File is located in: ``/target/EfficientLogfileAnalysis-1.0-SNAPSHOT.war``

-----

## Starting and Stopping the server
Navigate to the ``/bin`` Folder located in your glassfish6 install 

### Starting the server
```
asadmin.bat start-domain
```
The server will by default use port ``8080``

### Stopping the server
```
asadmin.bat stop-domain
```

-----

## Deploying the application
Place the compiled ``.war`` folder in the ``/bin`` directory of your glassfish 6 install.<br>
You can then deploy the application using:
```
asadmin.bat deploy --contextroot "/" EfficientLogfileAnalysis-1.0-SNAPSHOT.war
```
Note that the server must be running. <br>
Any subsequent starts of the server will also start the application, so there is no need to redeploy it every time the server reboots. 
