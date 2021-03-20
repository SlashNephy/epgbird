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

## Requirements

- Java 8 or later

## Get Started

`config.yml`

```yaml
# Twitter の資格情報
ck: xxx
cs: xxx
at: xxx
ats: xxx

# ツイートの取得間隔 (秒)
# 10 未満の値はエラーになります
interval: 3600
# 一度のチェックで通知する最大数
limit: 1
# ログレベル (OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL)
logLevel: 'TRACE'

# チェックするツイート定義のリスト
tweets:
    # スクリーンネーム
  - userScreenName: 'AUTOMATONJapan'
    # Discord Webhook URL
    discordWebhookUrl: 'https://discord.com/api/webhooks/xxx/xxx'
    # RT を無視するか
    ignoreRTs: true
    # 無視するテキスト (部分一致)
    ignoreTexts:
      - '【求人】'

    # ユーザ ID
  - userId: 4000001
    discordWebhookUrl: 'https://discord.com/api/webhooks/xxx/xxx'

    # リスト ID
  - listId: 10000001
    discordWebhookUrl: 'https://discord.com/api/webhooks/xxx/xxx'
```

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
  epgbird:
    container_name: epgbird
    image: slashnephy/epgbird:latest
    restart: always
    volumes:
      - ./config.yml:/app/config.yml:ro
      - data:/app/data

volumes:
  data:
    driver: local
```
