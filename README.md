# epgbird

🐦 A tiny tool to tweet EPGStation events such as recording or new reserves

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/epgbird/Docker)](https://hub.docker.com/r/slashnephy/epgbird)
[![Docker Image Size (tag)](https://img.shields.io/docker/image-size/slashnephy/epgbird/latest)](https://hub.docker.com/r/slashnephy/epgbird)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/epgbird)](https://hub.docker.com/r/slashnephy/epgbird)
[![license](https://img.shields.io/github/license/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/epgbird)](https://github.com/SlashNephy/epgbird/pulls)

[![screenshot.png](https://raw.githubusercontent.com/SlashNephy/epgbird/master/docs/screenshot.png)](https://github.com/SlashNephy/epgbird)

Demo => [@a0b4m0c4](https://twitter.com/a0b4m0c4)

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
    # 省略
  
  epgbird:
    container_name: epgbird
    image: slashnephy/epgbird:latest
    restart: always
    volumes:
      - /mnt:/mnt:ro
    environment:
      # タイムゾーン
      TZ: Asia/Tokyo
      # ログレベル
      LOG_LEVEL: TRACE
      # EPGStation 接続情報
      EPGSTATION_HOST: epgstation
      EPGSTATION_PORT: 8888
      # Twitter 資格情報 (必須)
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      # 新規の録画済番組を通知するかどうか
      INCLUDE_RECORDED: 1
      # 新規の録画中を通知するかどうか
      INCLUDE_RECORDING: 1
      # 新規の予約を通知するかどうか
      INCLUDE_RESERVES: 1
      # 録画中を通知する間隔 (分)
      RECORDING_POST_FREQUENCY_MINUTES: 10
      # ツイートに PNG 画像を含めるかどうか
      WITH_PNG: 1
      # ツイートに MP4 動画を含めるかどうか
      WITH_MP4: 0
      # 録画先の親ディレクトリ
      MOUNT_POINT: /mnt
```
