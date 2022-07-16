FROM --platform=$BUILDPLATFORM gradle:7.5.0-jdk17 AS cache
WORKDIR /app
ENV GRADLE_USER_HOME /app/gradle
COPY *.gradle.kts gradle.properties /app/
RUN gradle shadowJar --parallel --console=verbose

FROM --platform=$BUILDPLATFORM gradle:7.5.0-jdk17 AS build
WORKDIR /app
COPY --from=cache /app/gradle /home/gradle/.gradle
COPY *.gradle.kts gradle.properties /app/
COPY src/main/ /app/src/main/
RUN gradle shadowJar --parallel --console=verbose

# Final Stage
FROM amazoncorretto:18.0.1 as runtime

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
