#!/bin/sh
set -eu

envsubst '${VITE_QR_API_BASE_URL}' \
  < /etc/runtime-config.template.js \
  > /usr/share/nginx/html/runtime-config.js

exec nginx -g 'daemon off;'
