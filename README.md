# Integration Test with Docker Maven Plugin

In this post I describe all operations needed to set up integration tests running against external services (databases, caching systems, etc.) served by Docker container.

### Requirements for Windows users
You should follow these steps before you can proceed:

##### Docker Set up on Windows
###### STEP 0 - Install Requirements
Always use Admin provileges shell to do the following operations:
- Install Chocolatey following [this](https://chocolatey.org/install)
- Install Docker
```
    choco install -y docker
```
- Install Docker Machine
```
    choco install -y docker-machine
```
**N.B:** To find out more please check the confluence [page](https://confluence-nam.lmera.ericsson.se/display/TPX/Using+Docker-Machine+with+Windows)

###### STEP 1 - Create Docker Machine
Create a local instance of docker-machine to run container with following command:
```
    docker-machine --native-ssh create --engine-insecure-registry ieatbldcom25.athtem.eei.ericsson.se:5000 default
```
This command will:
- Create a VM with Boot2Docker up & running.
- Enable automatic pull docker images from internal registry *ieatbldcom25.athtem.eei.ericsson.se:5000* adding the option *--engine-insecure-registry*.



### Requirements for Linux users
You need just to install docker for linux.
- Add insecure registries host on your Docker daemon. Follow [this](https://stackoverflow.com/questions/40924931/trouble-running-docker-registry-in-insecure-mode-on-ubuntu-16-04).

### Instructions

###### STEP 1 - Local integration test against mariadb running on docker container
Maven profile will run a new docker container from *mariadbtest* image:
```
   mvn clean verify -Pdocker
```

###### STEP 2 - Run Manual Test with Spring Boot application connected to mariadb running on docker container
To run application with the required Maven profile you need to execute following command:
```
    mvn spring-boot:run -Pdocker
```
