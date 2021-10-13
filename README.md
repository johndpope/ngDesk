## ngDesk


ngDesk is an open source CRM, Expense, Ticket Management, Workflow and Chat platform. It's designed to be highly customizable and scalable and easily run on premise or in the cloud.


* Site: https://www.ngdesk.com

* Latest documentation: https://support.ngdesk.com/

* Get Help: support at ngdesk.com or post an issue on GitHub

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Update](#update)
- [Release Notes](#release-notes)


### Requirements


1. Linux Server which supports Docker 20.10.8 or greater (Ubuntu 20.04 LTS reccommended)
2. 8gb of ram or more
3. Approx 20GB of disk space


### Installation


1. Install docker following the steps at https://docs.docker.com/engine/install
2. Check if Python 3 is installed and if not install it from here https://www.python.org/downloads/ 
3. $ `apt install -y python3-pip `
4. $ `pip install docker`
5. $ `wget https://raw.githubusercontent.com/SubscribeIT/ngDesk/main/ngdesk`
6. $ `chmod +x ngdesk`
7. $ `./ngdesk --action build` and follow the promts


### Update

`./ngdesk --action update`


### Release Notes

