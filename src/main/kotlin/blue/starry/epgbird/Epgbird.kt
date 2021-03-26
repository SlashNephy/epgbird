package blue.starry.epgbird

import blue.starry.penicillin.endpoints.media.MediaCategory
import blue.starry.penicillin.endpoints.media.MediaType
import blue.starry.penicillin.endpoints.statuses
import blue.starry.penicillin.endpoints.statuses.create
import blue.starry.penicillin.extensions.MediaComponent
import blue.starry.penicillin.extensions.endpoints.createWithMedia
import kotlinx.coroutines.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.io.path.name
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.milliseconds
import kotlin.time.seconds

@Suppress("BlockingMethodInNonBlockingContext")
object Epgbird {
    private var lastRecordedId = 0L
    private var lastReserveId = 0L

    suspend fun checkRecording(): Unit = coroutineScope {
        EPGStationApi.getRecording()
            .records
            // 無視するチャンネルの番組をスキップ
            .filter { it.channelId !in Env.IGNORE_CHANNEL_IDS }
            .map {
                launch {
                    postRecordedTweet(it)
                }
            }.joinAll()
    }

    suspend fun checkRecorded(): Unit = coroutineScope {
        val response = EPGStationApi.getRecorded()
        val maxId = response.records.maxOfOrNull { it.id } ?: return@coroutineScope
        if (lastRecordedId == 0L) {
            lastRecordedId = maxId
        }

        response.records
            .asSequence()
            // 録画中の番組をスキップ
            .filterNot { it.isRecording }
            // 無視するチャンネルの番組をスキップ
            .filter { it.channelId !in Env.IGNORE_CHANNEL_IDS }
            // ID チェック
            .filter { lastRecordedId < it.id }
            .toList()
            .map {
                launch {
                    postRecordedTweet(it)
                }
            }.joinAll()

        lastRecordedId = maxId
    }

    suspend fun checkReserves(includeRuleReserves: Boolean): Unit = coroutineScope {
        val response = EPGStationApi.getReserves()
        val maxId = response.reserves.maxOfOrNull { it.id } ?: return@coroutineScope
        if (lastReserveId == 0L) {
            lastReserveId = maxId
        }

        response.reserves
            .asSequence()
            // ルール予約を含めるかどうか
            .filter { includeRuleReserves || it.ruleId == null }
            // 無視するチャンネルの番組をスキップ
            .filter { it.channelId !in Env.IGNORE_CHANNEL_IDS }
            // ID チェック
            .filter { lastReserveId < it.id }
            .toList()
            .map {
                launch {
                    postReserveTweet(it)
                }
            }.joinAll()

        lastReserveId = maxId
    }

    private suspend fun postRecordedTweet(item: RecordedItem) {
        // RECORDING_POST_FREQUENCY_MINS 分ごとに投稿
        val elapsedMinute = (Instant.now().toEpochMilli() - item.startAt).milliseconds.inMinutes.roundToInt()
        if (item.isRecording && elapsedMinute % Env.RECORDING_POST_FREQUENCY_MINUTES != 0) {
            return
        }

        logger.trace { item }

        val text = buildString {
            val recordType = if (item.ruleId == null) "手動" else "自動"
            if (!item.isRecording) {
                appendLine("【${recordType}録画完了】")
            } else if (elapsedMinute == 0) {
                appendLine("【${recordType}録画開始】")
            } else {
                appendLine("【${recordType}録画中 / ${elapsedMinute}分経過】")
            }

            append("${item.name} ")

            val channels = EPGStationApi.getChannels()
            val channel = channels.find { it.id == item.channelId }
            appendLine("[${channel?.halfWidthName}]")

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val startAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.startAt), ZoneOffset.systemDefault())
            val endAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.endAt), ZoneOffset.systemDefault())
            append("${timeFormatter.format(startAt)} ~ ${timeFormatter.format(endAt)} ")

            append("(")

            val duration = (item.endAt - item.startAt).milliseconds
            val hours = floor(duration.inHours).toInt()
            val minutes =  duration.inMinutes.roundToInt() - hours * 60
            if (hours > 0) {
                append("${hours}時間")
            }
            append("${minutes}分")

            // 録画済の場合
            if (!item.isRecording) {
                val gb = item.videoFiles.firstOrNull()?.size?.div(2.0.pow(30))?.let { String.format("%.1f", it) }
                append(", ${gb ?: "??"} GB")
            }

            appendLine(")\n")

            append("${item.description.orEmpty()}\n${item.extended.orEmpty()}".trim().omit(50))
        }.trimEnd()

        // EPGStation は録画ファイルの絶対パスを持たないためマウントポイントから検索する
        val mnt = Paths.get(Env.MOUNT_POINT)
        val filename = item.videoFiles.firstOrNull()?.filename
        val input = filename?.let { epgsFilename ->
            withContext(Dispatchers.IO) {
                Files.find(mnt, 2, { path, _ ->
                    path.name == epgsFilename
                }).findFirst().orElse(null)
            }
        }

        // チューナーの準備が整うまで待機
        if (input != null && elapsedMinute == 0) {
            delay(3.seconds)
        }

        postTweet(
            text = text,
            input = input,
            // 録画開始直後は mp4 を生成できない
            withMp4 = elapsedMinute > 0 && Env.WITH_MP4,
            withPng = Env.WITH_PNG,
            isRecording = item.isRecording
        )
    }

    private suspend fun postReserveTweet(item: ReservelItem) {
        logger.trace { item }

        val text = buildString {
            val recordType = if (item.ruleId == null) "新規" else "ルール"
            appendLine("【${recordType}予約追加】")

            append("${item.name} ")

            val channels = EPGStationApi.getChannels()
            val channel = channels.find { it.id == item.channelId }
            appendLine("[${channel?.halfWidthName}]")

            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val startAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.startAt), ZoneOffset.systemDefault())
            val endAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.endAt), ZoneOffset.systemDefault())
            append("${timeFormatter.format(startAt)} ~ ${timeFormatter.format(endAt)} ")

            append("(")
            val duration = (item.endAt - item.startAt).milliseconds
            val hours = floor(duration.inHours).toInt()
            val minutes =  duration.inMinutes.roundToInt() - hours * 60
            if (hours > 0) {
                append("${hours}時間")
            }
            appendLine("${minutes}分)\n")

            append("${item.description.orEmpty()}\n${item.extended.orEmpty()}".trim().omit(50))
        }.trimEnd()

        postTweet(text, input = null, withMp4 = false, withPng = false, isRecording = false)
    }

    /**
     * ツイートを投稿する
     *
     * @param text ツイート本文
     * @param input M2TS ファイルへのパス
     * @param withMp4 ツイートに mp4 を添付するかどうか
     * @param withPng ツイートに png を添付するかどうか (mp4 が優先される)
     * @param isRecording 現在録画中であるかどうか (録画中である場合 mp4 および png の位置は末尾からになる)
     */
    private suspend fun postTweet(
        text: String,
        input: Path? = null,
        withMp4: Boolean = false,
        withPng: Boolean = false,
        isRecording: Boolean = false
    ) {
        if (withMp4 && input != null) {
            val mp4 = Files.createTempFile(UUID.randomUUID().toString(), ".mp4")

            try {
                FFMPEGUtil.createMP4(input, mp4, isRecording)

                if (Env.DRYRUN) {
                    return
                }

                EpgbirdTwitterClient.statuses.createWithMedia(
                    status = text,
                    media = listOf(
                        MediaComponent(mp4, MediaType.MP4, MediaCategory.TweetVideo)
                    )
                ).execute()

                return
            } catch (t: Throwable) {
                logger.error(t) { "Failed to create and post mp4. Fallback." }
            } finally {
                withContext(Dispatchers.IO) {
                    Files.deleteIfExists(mp4)
                }
            }
        }

        if (withPng && input != null) {
            val png = Files.createTempFile(UUID.randomUUID().toString(), ".png")

            try {
                FFMPEGUtil.createPNG(input, png, isRecording)

                if (Env.DRYRUN) {
                    return
                }

                EpgbirdTwitterClient.statuses.createWithMedia(text, media = listOf(MediaComponent(png, MediaType.PNG, MediaCategory.TweetImage))).execute()

                return
            } catch (t: Throwable) {
                logger.error(t) { "Failed to create and post png. Fallback." }
            } finally {
                withContext(Dispatchers.IO) {
                    Files.deleteIfExists(png)
                }
            }
        }

        try {
            if (Env.DRYRUN) {
                return
            }

            EpgbirdTwitterClient.statuses.create(text).execute()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to create status." }
        }
    }

    private fun String.omit(limit: Int): String {
        return if (length > limit) {
            take(limit) + "…"
        } else {
            this
        }
    }
}
