# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jre

WORKDIR /root/

RUN apt-get update && apt-get install -y netcat
COPY .docker/entrypoint.sh ./
COPY hotwarmcold-platform/build/libs/*.jar ./
RUN chmod +x entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]
