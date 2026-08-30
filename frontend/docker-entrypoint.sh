#!/bin/sh
set -eu

: "${VITE_QR_API_BASE_URL:=http://localhost:8080}"
export VITE_QR_API_BASE_URL

envsubst \
  < /opt/runtime-config.template.js \
  > /usr/share/nginx/html/runtime-config.js

exec nginx -g "daemon off;"
