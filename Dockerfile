FROM --platform=$BUILDPLATFORM gradle:7.5.0-jdk17@sha256:8dd07704f6d4453f3159154eec16a9551a9efc687079122f46b53ec3a0c716a7 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM --platform=$BUILDPLATFORM gradle:7.5.0-jdk17@sha256:8dd07704f6d4453f3159154eec16a9551a9efc687079122f46b53ec3a0c716a7 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

# Final Stage
FROM amazoncorretto:18.0.1@sha256:50da77dcfd039a3af6864d322ae3f11d25492fc91dbc575009a1073ed7319a47 as runtime

#RUN cd /opt \
#    && mkdir ffmpeg \
#    && cd ffmpeg \
#    && yum install -y tar xz \
#    && curl -O https://johnvansickle.com/ffmpeg/releases/ffmpeg-release-amd64-static.tar.xz \
#    && tar -xf ffmpeg-release-amd64-static.tar.xz --strip-components=1 \
#    && rm -f ffmpeg-release-amd64-static.tar.xz \
#    && yum remove -y tar xz \
#    && yum clean all \
#    && rm -rf /var/cache/yum
#ENV PATH="/opt/ffmpeg:$PATH"
COPY ./.github/workflows/ffmpeg /usr/bin/
RUN chmod +x /usr/bin/ffmpeg

COPY --from=build /app/build/libs/epgbird-all.jar /app/epgbird.jar

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/epgbird.jar"]
