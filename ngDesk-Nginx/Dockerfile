FROM centos:centos7

EXPOSE 80
EXPOSE 443

RUN yum -y update
RUN yum clean all
RUN yum -y install epel-release
RUN yum -y install nginx
RUN yum -y install yum-utils
RUN yum-config-manager --add-repo https://openresty.org/package/centos/openresty.repo
RUN yum -y install openresty

COPY ./gzip.conf /etc/nginx/conf.d/
COPY ./http_headers.lua /usr/local/openresty/lualib/resty/
COPY ./http.lua /usr/local/openresty/lualib/resty/

RUN mkdir /etc/nginx/keys

COPY ./nginx.conf /etc/nginx/


ENTRYPOINT ["/usr/local/openresty/nginx/sbin/nginx", "-c", "/etc/nginx/nginx.conf"]


