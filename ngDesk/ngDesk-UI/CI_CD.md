# ngDesk-Angular CI/CD

## Setup

### Create docker image

`cd /root`  
`docker build -t abs-kuber-1:5000/angular ./angular-docker`  
`docker push abs-kuber-1:5000/angular`  

### Create a runner

`docker run --rm -t -i -v /srv/gitlab-runner/config:/etc/gitlab-runner --name angular-runner gitlab/gitlab-runner register --non-interactive --executor "docker" --docker-image abs-kuber-1:5000/angular:latest  --url "https://gitlab.bluemsp.com/" --registration-token "NawJBzuwTxruZR37TjvN" --description "angular-runner" --tag-list "docker,angular" --run-untagged --locked="false"`

`docker run -d --name angular-runner --restart always -v /srv/gitlab-runner/config:/etc/gitlab-runner -v /var/run/docker.sock:/var/run/docker.sock gitlab/gitlab-runner:latest`

### Create ci config files

In root of project create .gitlab-ci.yml


