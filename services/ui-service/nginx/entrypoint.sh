#!/bin/sh
set -eu

mkdir -p /tmp/client_temp /tmp/proxy_temp /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp /var/run/starterkit-ui-config

: "${UI_API_PROXY_BASE_URL:=}"
: "${UI_OTLP_PROXY_BASE_URL:=}"

if [ -n "${UI_API_PROXY_BASE_URL}" ]; then
  envsubst '${UI_API_PROXY_BASE_URL} ${UI_OTLP_PROXY_BASE_URL}' \
    < /etc/starterkit-ui/local-proxy.conf.template \
    > /var/run/starterkit-ui-config/local-proxy.conf
else
  : > /var/run/starterkit-ui-config/local-proxy.conf
fi

exec nginx -g 'daemon off;' -c /etc/nginx/nginx.conf
