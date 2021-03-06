# epgbird

π¦ A tiny tool to tweet EPGStation events such as recording or new reserves

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/epgbird/Docker)](https://hub.docker.com/r/slashnephy/epgbird)
[![Docker Image Size (tag)](https://img.shields.io/docker/image-size/slashnephy/epgbird/latest)](https://hub.docker.com/r/slashnephy/epgbird)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/epgbird)](https://hub.docker.com/r/slashnephy/epgbird)
[![license](https://img.shields.io/github/license/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/pulls)

[![screenshot.png](https://raw.githubusercontent.com/SlashNephy/epgbird/master/docs/screenshot.png)](https://github.com/SlashNephy/epgbird)

Demo Account => [@a0b4m0c4](https://twitter.com/a0b4m0c4)

## Requirements

- Java 8 or later
- ffmpeg (if you want to tweet with media)

## Get Started

### Docker

There are some image tags.

- `slashnephy/epgbird:latest`  
  Automatically published every push to `master` branch.
- `slashnephy/epgbird:dev`  
  Automatically published every push to `dev` branch.
- `slashnephy/epgbird:<version>`  
  Coresponding to release tags on GitHub.

`docker-compose.yml`

```yaml
version: '3.8'

services:
  epgstation:
    # ηη₯
  
  epgbird:
    container_name: epgbird
    image: slashnephy/epgbird:latest
    restart: always
    volumes:
      - /mnt:/mnt:ro
    environment:
      # Twitter θ³ζ Όζε ± (εΏι )
      # https://torinosuke.netlify.app γγγγγγγγγγΎγ
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      
      # δΊη΄θΏ½ε γιη₯γγγγ©γγ 
      INCLUDE_RESERVES: 1
      # γ«γΌγ«δΊη΄γιη₯γγγγ©γγ
      INCLUDE_RULE_RESERVES: 0
      # ι²η»ιε§γιη₯γγγγ©γγ
      INCLUDE_RECORD_START: 1
      # ι²η»δΈ­γιη₯γγγγ©γγ
      INCLUDE_RECORDING: 1
      # ι²η»η΅δΊγιη₯γγγγ©γγ
      INCLUDE_RECORD_END: 1
      
      # δΊη΄θΏ½ε γ»ι²η»ιε§γ»ι²η»δΈ­γ»ι²η»η΅δΊγ?ιη₯ζ¬ζγ?γγ©γΌγγγ
      # δ»₯δΈγ?ε€ζ°γδ½ΏγγΎγ
      #   %RESERVE_TYPE%: δΊη΄γ?γΏγ€γ (γ«γΌγ« or ζε)
      #   %BR%: ζΉθ‘ζε­
      #   %NAME%: ηͺη΅ε (εθ§γ»ε¨θ§γγ― USE_HALF_WIDTH η°ε’ε€ζ°γ«γγγ³γ³γγ­γΌγ«ε―θ½, δ»₯δΈεζ§)
      #   %CHANNEL%: γγ£γ³γγ«ε, εδΈ
      #   %DESCRIPTION%: ηͺη΅θͺ¬ζ, εδΈ (γγγ©γ«γγ― 50 ζε­γΎγ§εΊεγγγ DESCRIPTION_LENGTH η°ε’ε€ζ°γ«γγγ³γ³γγ­γΌγ«ε―θ½)
      #   %START_TIME%: ηͺη΅ιε§ζε»
      #   %DURATION%: η΅ιζι (ε) (RESERVES_FORMAT γ§δ½Ώη¨γγγγ¨γ―ζ³ε?γγγ¦γγͺγ)
      #   %START_TIME%: ηͺη΅γ?ιε§ζε» (ζε»γ?γγ©γΌγγγγ― TIME_FORMAT η°ε’ε€ζ°γ«γγγ³γ³γγ­γΌγ«ε―θ½)
      #   %END_TIME%: ηͺη΅γ?η΅δΊζε», εδΈ
      #   %DURATION%: ηͺη΅γ?ι·γ (xxζιxxε ε½’εΌ)
      #   %SIZE_GB%: ηͺη΅γ?ε?Ήι (GB εδ½, RECORD_END_FORMAT δ»₯ε€γ§δ½Ώη¨γγγγ¨γ―ζ³ε?γγγ¦γγͺγ)
      #   %COMMENT_DIGEST%: γ³γ‘γ³γγ?γγ€γΈγ§γΉγ (saya γεΏθ¦)
      #   %COMMENT_FORCE%: γ³γ‘γ³γγ?ε’γ (/min), εδΈ
      #   %DROP_COUNT%: γγ­γγζ°
      #   %ERROR_COUNT%: γ¨γ©γΌζ°
      #   %SCRAMBLE_COUNT%: γΉγ―γ©γ³γγ«ζ°
      #   %VIDEO_CODEC%: ζ εγ?γ³γΌγγγ―, MPEG2 or H.264 or H.265
      #   %VIDEO_RESOLUTION%: ζ εγ?θ§£εεΊ¦, e.g. 1080p, 720p
      #   %AUDIO_SAMPLING_RATE_KHZ%: ι³ε£°γ?γ΅γ³γγͺγ³γ°ε¨ζ³’ζ° (kHz), e.g. 4.8, 4.41
      RESERVES_FORMAT: γ%RESERVE_TYPE%δΊη΄θΏ½ε γ%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORD_START_FORMAT: γ%RESERVE_TYPE%ι²η»ιε§γ%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORDING_FORMAT: γ%RESERVE_TYPE%ι²η»δΈ­ / %ELAPSED_MINUTES%εη΅ιγ%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORD_END_FORMAT: γ%RESERVE_TYPE%ι²η»η΅δΊγ%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%, %SIZE_GB% GB)%BR%%BR%%DESCRIPTION%
      
      # ι²η»δΈ­γιη₯γγιι (ε)
      RECORDING_POST_FREQUENCY_MINUTES: 10
      # ηͺη΅εγ»γγ£γ³γγ«εγ»ηͺη΅θͺ¬ζγ§ε¨θ§θ±ζ°ε­γεθ§εγγγγ©γγ
      # εθ§εγγγ¨ε¨θ§γ?γοΌγ β γ1γγ?γγγ«γͺγγΎγ, γ«γΏγ«γγ―ε€εγγΎγγ
      USE_HALF_WIDTH: 1
      # ζε»θ‘¨θ¨γ?γγ©γΌγγγ
      # ζε?ε­γ― https://docs.oracle.com/javase/jp/8/docs/api/java/time/format/DateTimeFormatter.html γεη§
      TIME_FORMAT: HH:mm
      # ηͺη΅θͺ¬ζγ?ζε€§ι·γ (ζε­)
      DESCRIPTION_LENGTH: 50
      # η‘θ¦γγγγ£γ³γγ« ID
      # γ«γ³γεΊεγ
      IGNORE_CHANNEL_IDS: 400101
      
      # γγ€γΌγγ« png η»εγε«γγγγ©γγ
      WITH_PNG: 0
      # png ηζ ffmpeg γ³γγ³γ
      # δ»₯δΈγ?ε€ζ°γδ½ΏγγΎγ
      #   %POSITION%: εηδ½η½?
      #   %INPUT%: ε₯εγγ‘γ€γ«
      #   %OUTPUT%: εΊεγγ‘γ€γ«
      FFMPEG_PNG_COMMAND: ffmpeg %POSITION% -i %INPUT% -vframes 1 -f image2 -s 1920x1080 -loglevel error -y %OUTPUT%
      # png ηζγ?γΏγ€γ γ’γ¦γ (η§)
      FFMPEG_PNG_TIMEOUT_SECONDS: 5
      
      # γγ€γΌγγ« mp4 εη»γε«γγγγ©γγ
      WITH_MP4: 0
      # mp4 ηζ ffmpeg γ³γγ³γ
      # δ»₯δΈγ?ε€ζ°γδ½ΏγγΎγ
      #   %POSITION%: εηδ½η½?
      #   %INPUT%: ε₯εγγ‘γ€γ«
      #   %OUTPUT%: εΊεγγ‘γ€γ«
      FFMPEG_MP4_COMMAND: ffmpeg %POSITION% -t 120 -i %INPUT% -f mp4 -c:a aac -ab 128k -ar 48000 -ac 2 -c:v libx264 -pix_fmt yuv420p -vf scale=1280:-1 -vb 2048k -r 30 -minrate 1024k -maxrate 2048k -strict experimental -threads 1 -loglevel error -y %OUTPUT%
      # mp4 ηζγ?γΏγ€γ γ’γ¦γ (η§)
      FFMPEG_MP4_TIMEOUT_SECONDS: 30
      
      # ι²η»εγ?θ¦ͺγγ£γ¬γ―γγͺ
      MOUNT_POINT: /mnt
      
      # EPGStation ζ₯ηΆζε ±
      EPGSTATION_HOST: epgstation
      EPGSTATION_PORT: 8888
      # γΏγ€γ γΎγΌγ³
      TZ: Asia/Tokyo
      # γ­γ°γ¬γγ«
      LOG_LEVEL: INFO
```
