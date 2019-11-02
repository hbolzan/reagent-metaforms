# reagent metaforms public frontend files
FROM timbru31/java-node
RUN yarn --version

RUN mkdir -p /reagent-metaforms/src
RUN mkdir -p /reagent-metaforms/public
COPY ./package.json /reagent-metaforms/package.json
COPY ./shadow-cljs.edn /reagent-metaforms/shadow-cljs.edn
COPY ./src/. /reagent-metaforms/src/.
COPY ./public/. /reagent-metaforms/public/.
WORKDIR /reagent-metaforms
RUN yarn install
RUN yarn release app

FROM busybox
RUN mkdir -p /var/www/html
COPY --from=0 /reagent-metaforms/public/. /var/www/html/.
# COPY public/. /var/www/html/.
ADD VERSION .
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT  ["/docker-entrypoint.sh"]
