FROM nginxinc/nginx-unprivileged:alpine

COPY dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

USER root

RUN touch /usr/share/nginx/html/runtime-config.js \
    && chown nginx:nginx /usr/share/nginx/html/runtime-config.js \
    && chmod 664 /usr/share/nginx/html/runtime-config.js

USER nginx

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
