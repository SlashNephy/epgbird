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
    # 省略
  
  epgbird:
    container_name: epgbird
    image: slashnephy/epgbird:latest
    restart: always
    volumes:
      - /mnt:/mnt:ro
    environment:
      # Twitter 資格情報 (必須)
      # https://torinosuke.netlify.app あたりをおすすめします
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      
      # 予約追加を通知するかどうか 
      INCLUDE_RESERVES: 1
      # ルール予約も通知するかどうか
      INCLUDE_RULE_RESERVES: 0
      # 録画開始を通知するかどうか
      INCLUDE_RECORD_START: 1
      # 録画中を通知するかどうか
      INCLUDE_RECORDING: 1
      # 録画終了を通知するかどうか
      INCLUDE_RECORD_END: 1
      
      # 予約追加・録画開始・録画中・録画終了の通知本文のフォーマット
      # 以下の変数が使えます
      #   %RESERVE_TYPE%: 予約のタイプ (ルール or 手動)
      #   %BR%: 改行文字
      #   %NAME%: 番組名 (半角・全角かは USE_HALF_WIDTH 環境変数によりコントロール可能, 以下同様)
      #   %CHANNEL%: チャンネル名, 同上
      #   %DESCRIPTION%: 番組説明, 同上 (デフォルトは 50 文字まで出力するが DESCRIPTION_LENGTH 環境変数によりコントロール可能)
      #   %START_TIME%: 番組開始時刻
      #   %DURATION%: 経過時間 (分) (RESERVES_FORMAT で使用することは想定されていない)
      #   %START_TIME%: 番組の開始時刻 (時刻のフォーマットは TIME_FORMAT 環境変数によりコントロール可能)
      #   %END_TIME%: 番組の終了時刻, 同上
      #   %DURATION%: 番組の長さ (xx時間xx分 形式)
      #   %SIZE_GB%: 番組の容量 (GB 単位, RECORD_END_FORMAT 以外で使用することは想定されていない)
      #   %COMMENT_DIGEST%: コメントのダイジェスト (saya が必要)
      #   %COMMENT_FORCE%: コメントの勢い (/min), 同上
      #   %DROP_COUNT%: ドロップ数
      #   %ERROR_COUNT%: エラー数
      #   %SCRAMBLE_COUNT%: スクランブル数
      #   %VIDEO_CODEC%: 映像のコーデック, MPEG2 or H.264 or H.265
      #   %VIDEO_RESOLUTION%: 映像の解像度, e.g. 1080p, 720p
      #   %AUDIO_SAMPLING_RATE_KHZ%: 音声のサンプリング周波数 (kHz), e.g. 4.8, 4.41
      RESERVES_FORMAT: 【%RESERVE_TYPE%予約追加】%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORD_START_FORMAT: 【%RESERVE_TYPE%録画開始】%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORDING_FORMAT: 【%RESERVE_TYPE%録画中 / %ELAPSED_MINUTES%分経過】%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%)%BR%%BR%%DESCRIPTION%
      RECORD_END_FORMAT: 【%RESERVE_TYPE%録画終了】%BR%%NAME% [%CHANNEL%]%BR%%START_TIME% ~ %END_TIME% (%DURATION%, %SIZE_GB% GB)%BR%%BR%%DESCRIPTION%
      
      # 録画中を通知する間隔 (分)
      RECORDING_POST_FREQUENCY_MINUTES: 10
      # 番組名・チャンネル名・番組説明で全角英数字を半角化するかどうか
      # 半角化すると全角の「１」 → 「1」のようになります, カタカナは変化しません
      USE_HALF_WIDTH: 1
      # 時刻表記のフォーマット
      # 指定子は https://docs.oracle.com/javase/jp/8/docs/api/java/time/format/DateTimeFormatter.html を参照
      TIME_FORMAT: HH:mm
      # 番組説明の最大長さ (文字)
      DESCRIPTION_LENGTH: 50
      # 無視するチャンネル ID
      # カンマ区切り
      IGNORE_CHANNEL_IDS: 400101
      
      # ツイートに png 画像を含めるかどうか
      WITH_PNG: 0
      # png 生成 ffmpeg コマンド
      # 以下の変数が使えます
      #   %POSITION%: 再生位置
      #   %INPUT%: 入力ファイル
      #   %OUTPUT%: 出力ファイル
      FFMPEG_PNG_COMMAND: ffmpeg %POSITION% -i %INPUT% -vframes 1 -f image2 -s 1920x1080 -loglevel error -y %OUTPUT%
      # png 生成のタイムアウト (秒)
      FFMPEG_PNG_TIMEOUT_SECONDS: 5
      
      # ツイートに mp4 動画を含めるかどうか
      WITH_MP4: 0
      # mp4 生成 ffmpeg コマンド
      # 以下の変数が使えます
      #   %POSITION%: 再生位置
      #   %INPUT%: 入力ファイル
      #   %OUTPUT%: 出力ファイル
      FFMPEG_MP4_COMMAND: ffmpeg %POSITION% -t 120 -i %INPUT% -f mp4 -c:a aac -ab 128k -ar 48000 -ac 2 -c:v libx264 -pix_fmt yuv420p -vf scale=1280:-1 -vb 2048k -r 30 -minrate 1024k -maxrate 2048k -strict experimental -threads 1 -loglevel error -y %OUTPUT%
      # mp4 生成のタイムアウト (秒)
      FFMPEG_MP4_TIMEOUT_SECONDS: 30
      
      # 録画先の親ディレクトリ
      MOUNT_POINT: /mnt
      
      # EPGStation 接続情報
      EPGSTATION_HOST: epgstation
      EPGSTATION_PORT: 8888
      # タイムゾーン
      TZ: Asia/Tokyo
      # ログレベル
      LOG_LEVEL: INFO
```
