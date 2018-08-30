FROM frolvlad/alpine-oraclejdk8
ARG JAR_FILE
ADD ../../../../build/libs/sjb-v1-0.0.1.jar /
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/sjb-v1-0.0.1.jar"]