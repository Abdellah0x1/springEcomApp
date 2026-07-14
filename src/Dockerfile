FROM ubuntu:latest
LABEL authors="Abdellah"
WORKDIR /src
COPY . .
RUN mvn install


ENTRYPOINT ["top", "-b"]