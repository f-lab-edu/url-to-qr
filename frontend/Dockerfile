FROM node:22.23.2-alpine3.23 AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build


FROM nginxinc/nginx-unprivileged:1.30.4-alpine3.24

USER root

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY runtime-config.template.js /etc/runtime-config.template.js
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN touch /usr/share/nginx/html/runtime-config.js \
    && chown nginx:nginx /usr/share/nginx/html/runtime-config.js \
    && chmod 664 /usr/share/nginx/html/runtime-config.js \
    && chmod 755 /docker-entrypoint.sh

USER nginx

EXPOSE 8080

ENTRYPOINT ["/docker-entrypoint.sh"]