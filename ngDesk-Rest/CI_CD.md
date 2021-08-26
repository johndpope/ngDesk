# ngDesk-Rest CI/CD

## Setup

### Create docker image

`cd /root`  
`docker build -t abs-kuber-1:5000/java ./ajva-docker`  
`docker push abs-kuber-1:5000/java`  

### Stop any previous runners

##### list the containers

`docker container ls`

##### Stop the container

`docker container stop container_id`

##### Delete the container

`docker rm container_id`


### Create a runner

`docker run --rm -t -i -v /srv/gitlab-runner/config:/etc/gitlab-runner --name java-runner gitlab/gitlab-runner register --non-interactive --executor "docker" --docker-image abs-kuber-1:5000/java:latest --url "https://gitlab.bluemsp.com/" --registration-token "9ByvGxjqnBY34uf4Tps9" --description "java-runner" --tag-list "docker,java" --run-untagged --locked="false"`

`docker run -d --name java-runner --restart always -v /srv/gitlab-runner/config:/etc/gitlab-runner -v /var/run/docker.sock:/var/run/docker.sock gitlab/gitlab-runner:latest`

### Create ci config files

In root of project create .gitlab-ci.yml
