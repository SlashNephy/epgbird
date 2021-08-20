FROM slashnephy/epgbird AS image

# Final Stage
FROM ghcr.io/starrybluesky/ffmpeg:4.3.2-ubuntu

ENV DEBIAN_FRONTEND=noninteractive
ENV DEPENDENCIES="git wget gnupg2 software-properties-common"
RUN apt-get update \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends \
        tzdata \
        $DEPENDENCIES \
    \
    ## Install AdoptOpenJDK
    && wget -qO - https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public | apt-key add - \
    && add-apt-repository --yes https://adoptopenjdk.jfrog.io/adoptopenjdk/deb/ \
    && mkdir -p /usr/share/man/man1 \
    && apt-get update \
    && apt-get install -y adoptopenjdk-11-hotspot \
    \
    ## Cleanup
    && apt-get purge -y $DEPENDENCIES \
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists

WORKDIR /app
COPY --from=image /app/epgbird.jar /app/epgbird.jar

ENTRYPOINT ["java", "-jar", "/app/epgbird.jar"]
