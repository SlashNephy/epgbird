ARG IMAGE_TAG

FROM slashnephy/epgbird:$IMAGE_TAG AS image

# Final Stage
FROM ghcr.io/starrybluesky/ffmpeg:4.3.2-ubuntu

WORKDIR /app
COPY --from=image /app/epgbird.jar /app/epgbird.jar

ENV DEBIAN_FRONTEND=noninteractive
ENV DEPENDENCIES="curl gnupg2 software-properties-common"
RUN apt-get update | true \
    && apt-get upgrade -y \
    && apt-get install -y --no-install-recommends \
        tzdata \
        $DEPENDENCIES \
    \
    # Install Amazon Corretto 18
    && curl https://apt.corretto.aws/corretto.key | apt-key add - \
    && add-apt-repository --yes 'deb https://apt.corretto.aws stable main' | true \
    && apt-get install -y java-18-amazon-corretto-jdk \
    \
    ## Cleanup
    && apt-get purge -y $DEPENDENCIES \
    && apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists

ENTRYPOINT ["java", "-jar", "/app/epgbird.jar"]
