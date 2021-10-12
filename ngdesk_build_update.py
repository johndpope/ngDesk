import docker
import urllib.request
import os, pwd, stat
from os import path
import getpass
import shutil
from itertools import chain

client = docker.from_env()

def build_ngdesk():

    if path.isdir('/ngdesk') == False:
        os.mkdir('/ngdesk')
    if path.isdir('/ngdesk/mongodb') == False:
        os.mkdir('/ngdesk/mongodb')
        os.chmod('/ngdesk/mongodb', stat.S_IRWXG)
        os.chmod('/ngdesk/mongodb', stat.S_IRWXU)
        os.chown('/ngdesk/mongodb', 999, 999)
    if path.isdir('/ngdesk/elasticsearch') == False:
        os.mkdir('/ngdesk/elasticsearch')
        os.chmod('/ngdesk/elasticsearch', stat.S_IRWXG)
    if path.isdir('/ngdesk/nginx') == False:
        os.mkdir('/ngdesk/nginx')
        os.chmod('/ngdesk/nginx', stat.S_IRWXG)


    ngdesk_images = [
        {'name': 'ngdesk-consul', 'path': 'ngdesk/consul:latest'}, 
        {'name': 'ngdesk-zipkin', 'path': 'ngdesk/zipkin:latest'}, 
        {'name': 'ngdesk-rabbit', 'path': 'rabbitmq:3.8'}, 
        {'name': 'ngdesk-redis', 'path': 'bitnami/redis:6.0.8'},
        {'name': 'ngdesk-config-server', 'path': 'ngdesk/config-server:latest'}, 

        {'name': 'ngdesk-mongodb', 'path': 'ngdesk/mongodb:latest'}, 
        {'name': 'ngdesk-elasticsearch', 'path': 'ngdesk/elasticsearch:latest'}, 

        {'name': 'ngdesk-rest', 'path': 'ngdesk/rest:latest'}, 
        {'name': 'ngdesk-manager', 'path': 'ngdesk/manager:latest'}, 
        
        #{'name': 'ngdesk-nginx', 'path': docker_registry_ngdesk_group + 'ngdesk-nginx:latest'}, 
        {'name': 'ngdesk-gateway', 'path': 'ngdesk/gateway:latest'}, 

        {'name': 'ngdesk-auth', 'path': 'ngdesk/auth:latest'}, 
        {'name': 'ngdesk-escalation', 'path': 'ngdesk/escalation:latest'}, 
        {'name': 'ngdesk-integration', 'path': 'ngdesk/integration:latest'}, 
        {'name': 'ngdesk-sam', 'path': 'ngdesk/sam:latest'}, 
        {'name': 'ngdesk-workflow', 'path': 'ngdesk/workflow:latest'}, 
        {'name': 'ngdesk-sidebar', 'path': 'ngdesk/sidebar:latest'}, 
        {'name': 'ngdesk-data', 'path': 'ngdesk/data:latest'}, 
        {'name': 'ngdesk-websocket', 'path': 'ngdesk/websocket:latest'}, 
        {'name': 'ngdesk-module', 'path': 'ngdesk/module:latest'}, 
        {'name': 'ngdesk-plugin', 'path':'ngdesk/plugin:latest'}, 
        {'name': 'ngdesk-company', 'path': 'ngdesk/company:latest'}, 
        {'name': 'ngdesk-role', 'path': 'ngdesk/role:latest'}, 
        {'name': 'ngdesk-graphql', 'path': 'ngdesk/graphql:latest'},
        {'name': 'ngdesk-report', 'path': 'ngdesk/report:latest'}, 
        {'name': 'ngdesk-web', 'path': 'ngdesk/web:latest'}, 
        {'name': 'ngdesk-email-server', 'path': 'ngdesk/email-server:latest'},
        {'name': 'ngdesk-email-server', 'path': 'ngdesk/email-sender:latest'},
        {'name': 'ngdesk-notification', 'path': 'ngdesk/notification:latest'}
    ]

    print(ngdesk_images)

    for image in ngdesk_images:
        image_name = image['name']
        image_path = image['path']

        print('starting ' + image_name + ' with path ' + image_path + '...')
        try:
            local_image = client.images.get(image_path)
        except docker.errors.ImageNotFound:
            local_image = client.images.pull(image_path)
            
        local_image_id = local_image.short_id

        pulled_image = client.images.pull(image_path)
        pulled_image_id = pulled_image.short_id


        try:
            curr_container = client.containers.get(image_name)
            if local_image_id != pulled_image_id:
                curr_container.stop()
                curr_container.remove()

                start_containers(image_path, image_name)
        except docker.errors.NotFound:
            start_containers(image_path, image_name)
        
        print(image_name + ' done')


def start_containers(image_path, image_name):
    if image_name == 'ngdesk-mongodb':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/mongodb':{'bind':'/data/db', 'mode': 'rw'}}, healthcheck={"Test": ["CMD-SHELL", "mongo --eval \"rs.initiate({_id: 'rs0', version: 1, members: [{ _id: 0, host : 'localhost:27017' } ]})\""],"Interval": 1000000 * 500, "Timeout": 1000000 * 5 * 1000, "Retries": 3, "StartPeriod": 1000000 * 5 * 1000})
    elif image_name == 'ngdesk-elasticsearch':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/elasticsearch':{'bind':'/usr/share/elasticsearch/data', 'mode': 'rw'}}, environment=['discovery.type=single-node', 'ES_JAVA_OPTS=-Xms1g -Xmx1g'])
    # elif image_name == 'ngdesk-kibana':
    #     client.containers.run(image_path, name=image_name, detach=True, network_mode='host', environment=['ELASTICSEARCH_HOSTS=http://localhost:9200'])
    elif image_name == 'ngdesk-redis':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', environment=['REDIS_PASSWORD=Qk4CSfb4hU7f'])
    elif image_name == 'ngdesk-nginx':
        # urllib.request.urlretrieve('http://10.2.15.60/nginx/nginx.conf', '/ngdesk/nginx/nginx.conf')
        # urllib.request.urlretrieve('http://10.2.15.60/nginx/ngdesk.crt', '/ngdesk/nginx/ngdesk.crt')
        # urllib.request.urlretrieve('http://10.2.15.60/nginx/ngdesk.key', '/ngdesk/nginx/ngdesk.key')
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/nginx/nginx.conf':{'bind':'/etc/nginx/nginx.conf', 'mode': 'ro'}, '/ngdesk/nginx/ngdesk.crt':{'bind':'/etc/nginx/keys/ngdesk.crt'}, '/ngdesk/nginx/ngdesk.key':{'bind':'/etc/nginx/keys/ngdesk.key'}})
    elif image_name == 'ngdesk-email-server':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', environment=['MANAGER_HOST=localhost'])
    else:
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host')
