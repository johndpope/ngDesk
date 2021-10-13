import docker
import requests
import os, stat
from os import path
import sys
import time
import socket
import getpass
from OpenSSL import crypto, SSL



client = docker.from_env()
print('''
              _____            _    
             |  __ \          | |   
  _ __   __ _| |  | | ___  ___| | __
 | '_ \ / _` | |  | |/ _ \/ __| |/ /
 | | | | (_| | |__| |  __/\__ \   < 
 |_| |_|\__, |_____/ \___||___/_|\_\\
         __/ |                      
        |___/                       
''')
ngdesk_images = [
    {'name': 'ngdesk-consul', 'path': 'ngdesk/consul:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8500/ui/'}}, 
    {'name': 'ngdesk-zipkin', 'path': 'ngdesk/zipkin:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:9411/health'}}, 
    {'name': 'ngdesk-rabbit', 'path': 'rabbitmq:3.8', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 5672}}, 
    {'name': 'ngdesk-redis', 'path': 'bitnami/redis:6.0.8', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 6379}},
    {'name': 'ngdesk-config-server', 'path': 'ngdesk/config-server:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8888/actuator/health'}}, 

    {'name': 'ngdesk-mongodb', 'path': 'ngdesk/mongodb:latest', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 27017}}, 
    {'name': 'ngdesk-elasticsearch', 'path': 'elasticsearch:7.14.1', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:9200/_cluster/health'}}, 

    {'name': 'ngdesk-rest', 'path': 'ngdesk/rest:latest', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 9080}}, 
    {'name': 'ngdesk-manager', 'path': 'ngdesk/manager:latest', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 9081}}, 
    
    {'name': 'ngdesk-nginx', 'path': 'ngdesk/nginx:latest', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 443}}, 
    {'name': 'ngdesk-gateway', 'path': 'ngdesk/gateway:latest', 'healthcheck': {'type': 'socket', 'attempts': 24, 'interval': 5, 'port': 8443}}, 

    {'name': 'ngdesk-auth', 'path': 'ngdesk/auth:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8070/actuator/health'}}, 
    {'name': 'ngdesk-escalation', 'path': 'ngdesk/escalation:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8081/actuator/health'}}, 
    {'name': 'ngdesk-sam', 'path': 'ngdesk/sam:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8083/actuator/health'}}, 
    {'name': 'ngdesk-workflow', 'path': 'ngdesk/workflow:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8084/actuator/health'}}, 
    {'name': 'ngdesk-sidebar', 'path': 'ngdesk/sidebar:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8085/actuator/health'}}, 
    {'name': 'ngdesk-data', 'path': 'ngdesk/data:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8087/actuator/health'}}, 
    {'name': 'ngdesk-websocket', 'path': 'ngdesk/websocket:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8088/actuator/health'}}, 
    {'name': 'ngdesk-module', 'path': 'ngdesk/module:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8090/actuator/health'}}, 
    {'name': 'ngdesk-plugin', 'path':'ngdesk/plugin:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8091/actuator/health'}}, 
    {'name': 'ngdesk-company', 'path': 'ngdesk/company:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8092/actuator/health'}}, 
    {'name': 'ngdesk-role', 'path': 'ngdesk/role:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8093/actuator/health'}}, 
    {'name': 'ngdesk-graphql', 'path': 'ngdesk/graphql:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8094/actuator/health'}},
    {'name': 'ngdesk-report', 'path': 'ngdesk/report:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8099/actuator/health'}}, 
    {'name': 'ngdesk-web', 'path': 'ngdesk/web:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8200/actuator/health'}}, 
    {'name': 'ngdesk-notification', 'path': 'ngdesk/notification:latest', 'healthcheck': {'type': 'curl', 'attempts': 24, 'interval': 5, 'url': 'http://localhost:8096/actuator/health'}},
    {'name': 'ngdesk-email-server', 'path': 'ngdesk/email-server:latest'},
    {'name': 'ngdesk-email-sender', 'path': 'ngdesk/email-sender:latest'}
]


def build_ngdesk():
    print('build_ngdesk')


    print('Before the installation starts, we need to gather some infomation from you. This infomation will be used to setup the admin user and default behavior and will not be transmitted out of this system.')
    first_name = input("Enter your first name: ")
    last_name = input("Enter your last name: ")
    email = input("Enter your email: ")
    company_name = input("Enter your company name: ")
    domain = input("Enter the domain you will use to access the website: ")
    password = getpass.getpass('Enter your password (minimum 8 characters, with atleast one upper case, and atleast one special character): ')

    cert_gen(domain)

    update_ngdesk()

    create_company(company_name, email, first_name, last_name, password)


def update_ngdesk():

    print('update_ngdesk')
 
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
            else:
                curr_container.start()
        except docker.errors.NotFound:
            start_containers(image_path, image_name)


        check_container_started(image)

        
        print(image_name + ' done')




def create_company(company_name, email, first_name, last_name, password):
    print('create company')


    payload = {
        "COMPANY_NAME": company_name,
        "COMPANY_SUBDOMAIN": "onprem_" + company_name,
        "EMAIL_ADDRESS": email,
        "FIRST_NAME": first_name,
        "HIDDEN_FIELD": "",
        "LANGUAGE": "en",
        "LAST_NAME": last_name,
        "PASSWORD": password,
        "PHONE": {
            "COUNTRY_CODE": "US",
            "DIAL_CODE": "+1",
            "PHONE_NUMBER": "4692009202",
            "COUNTRY_FLAG": "us.svg"
        },
        "TIMEZONE": "America/Chicago",
        "PRICING_TIER": "professional",
        "PLUGINS": [
            "Ticketing",
            "CRM",
            "Change Requests"
        ]
    }

    resp = requests.post('http://localhost:8443/api/ngdesk-company-service-v1/company', payload)
    print(resp)


def check_container_started(image):

    if 'healthcheck' in image:
        print('check container started')
        image_healthcheck = image['healthcheck']
        healthcheck_attempts = image_healthcheck['attempts']
        healthcheck_interval = image_healthcheck['interval']
        container_started = False

        print(healthcheck_attempts)

        for x in range(healthcheck_attempts):
            if image_healthcheck['type'] == 'curl':
                print(image_healthcheck['url'])
                try:
                    resp = requests.get(image_healthcheck['url'])
                    print(resp)
                    if resp.ok:
                        container_started = True
                        break
                    else:
                        time.sleep(healthcheck_interval)
                except requests.exceptions.ConnectionError:
                    time.sleep(healthcheck_interval)
            elif image_healthcheck['type'] == 'socket':
                sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                result = sock.connect_ex(('127.0.0.1', image_healthcheck['port']))
                if result == 0:
                    container_started = True
                    break
                else:
                    time.sleep(healthcheck_interval)
                sock.close()

                    

        if container_started == False:
            sys.exit(image['name'] + ' failed to start in alloted time')






def start_containers(image_path, image_name):
    if image_name == 'ngdesk-mongodb':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/mongodb':{'bind':'/data/db', 'mode': 'rw'}}, healthcheck={"Test": ["CMD-SHELL", "mongo --eval \"rs.initiate({_id: 'rs0', version: 1, members: [{ _id: 0, host : 'localhost:27017' } ]})\""],"Interval": 1000000 * 500, "Timeout": 1000000 * 5 * 1000, "Retries": 3, "StartPeriod": 1000000 * 5 * 1000})
    elif image_name == 'ngdesk-elasticsearch':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/elasticsearch':{'bind':'/usr/share/elasticsearch/data', 'mode': 'rw'}}, environment=['discovery.type=single-node', 'ES_JAVA_OPTS=-Xms1g -Xmx1g'])
    elif image_name == 'ngdesk-redis':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', environment=['REDIS_PASSWORD=Qk4CSfb4hU7f'])
    elif image_name == 'ngdesk-nginx':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', volumes={'/ngdesk/nginx':{'bind':'/etc/nginx/keys', 'mode': 'ro'}})
    elif image_name == 'ngdesk-email-server':
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host', environment=['MANAGER_HOST=localhost'])
    else:
        client.containers.run(image_path, name=image_name, detach=True, network_mode='host')


def cert_gen(common_name):
    email_address="support@ngdesk.com"
    country_name="US"
    locality_name="Dallas"
    state_or_province_name="TX"
    organization_name="ngDesk"
    organization_unit_name="ngDesk"
    serial_number=0
    validity_end_in_seconds=10*365*24*60*60
    KEY_FILE = "private.key"
    CERT_FILE="selfsigned.crt"
    #can look at generated file using openssl:
    #openssl x509 -inform pem -in selfsigned.crt -noout -text
    # create a key pair
    k = crypto.PKey()
    k.generate_key(crypto.TYPE_RSA, 4096)
    # create a self-signed cert
    cert = crypto.X509()
    cert.get_subject().C = country_name
    cert.get_subject().ST = state_or_province_name
    cert.get_subject().L = locality_name
    cert.get_subject().O = organization_name
    cert.get_subject().OU = organization_unit_name
    cert.get_subject().CN = common_name
    cert.get_subject().emailAddress = email_address
    cert.set_serial_number(serial_number)
    cert.gmtime_adj_notBefore(0)
    cert.gmtime_adj_notAfter(validity_end_in_seconds)
    cert.set_issuer(cert.get_subject())
    cert.set_pubkey(k)
    cert.sign(k, 'sha512')
    with open(CERT_FILE, "wt") as f:
        f.write(crypto.dump_certificate(crypto.FILETYPE_PEM, cert).decode("utf-8"))
    with open(KEY_FILE, "wt") as f:
        f.write(crypto.dump_privatekey(crypto.FILETYPE_PEM, k).decode("utf-8"))




if __name__ == '__main__':
    print('Manually running script')
    build_ngdesk()


