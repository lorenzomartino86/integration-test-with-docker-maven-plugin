# Integration tests with Fabric8 Docker Maven Plugin

### Requirements for Linux users
You just need to install [docker for linux](https://www.docker.com/docker-ubuntu) (I will provide soon the instructions for windows users). 

If you need to add insecure registries host on your Docker daemon to pull images from your private repository you can follow [this link](https://stackoverflow.com/a/40925645/8372319g).

### How run integration tests with Fabric8
You can run local integration tests by activating *docker* profile during Maven build. 
This profile will activate Fabric8 plugin that will pull and run in containers following services from dockerhub public repository:
- mysql
- mongo
- redis

Then build will trigger integration tests against these running containers.

Finally at the end of integration-phase running containers are automatically stopped. For more details please see [this post](https://javanger.blogspot.ie/2018/04/speed-up-your-spring-boot-integration.html)

