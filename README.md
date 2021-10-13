## ngDesk


ngDesk is an open source CRM, Expense, Ticket Management, Workflow and Chat platform. It's designed to be highly customizable and scalable and easily run on premise or in the cloud.


* Site: https://www.ngdesk.com

* Latest documentation: https://support.ngdesk.com/

* Help: support@ngdesk.com or post an issue on GitHub

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Upgrading](#upgrading)
- [Release Notes](#release-notes)


### Requirements


1. Linux server which supports Docker 20.10.8 or greater (Ubuntu 20.04 LTS recommended)
2. 8GB of ram or more
3. 24GB of disk space or more
4. Python 3


### Installation


1. Install docker following the steps at https://docs.docker.com/engine/install
2. Check if Python 3 is installed and if not install it from here https://www.python.org/downloads/ 
3. $ `apt install -y python3-pip `
4. $ `pip install docker`
5. $ `wget https://raw.githubusercontent.com/SubscribeIT/ngDesk/main/ngdesk`
6. $ `chmod +x ngdesk`
7. $ `./ngdesk --action build` and follow the promts


### Upgrading

`./ngdesk --action update`


### Release Notes

