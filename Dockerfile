# reagent metaforms public frontend files
FROM alpine:latest
RUN mkdir /html
COPY ./public/. /html
VOLUME ["metaforms_static"]
