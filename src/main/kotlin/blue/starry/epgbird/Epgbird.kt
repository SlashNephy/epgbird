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
                    postTweet(it)
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
                    postTweet(it)
                }
            }.joinAll()

        lastRecordedId = maxId
    }

    suspend fun checkReserves(): Unit = coroutineScope {
        val response = EPGStationApi.getReserves()
        val maxId = response.reserves.maxOfOrNull { it.id } ?: return@coroutineScope
        if (lastReserveId == 0L) {
            lastReserveId = maxId
        }

        response.reserves
            .asSequence()
            // ルール予約を含めるかどうか
            .filter { Env.INCLUDE_RULE_RESERVES || it.ruleId == null }
            // 無視するチャンネルの番組をスキップ
            .filter { it.channelId !in Env.IGNORE_CHANNEL_IDS }
            // ID チェック
            .filter { lastReserveId < it.id }
            .toList()
            .map {
                launch {
                    postTweet(it)
                }
            }.joinAll()

        lastReserveId = maxId
    }

    private suspend fun postTweet(item: ProgramItem) {
        // 経過時間 (分)
        val elapsedMinutes = (Instant.now().toEpochMilli() - item.startAt).milliseconds.inMinutes.roundToInt()
        // 番組の長さ (分)
        val totalMinutes = (item.endAt - item.startAt).milliseconds.inMinutes.roundToInt()

        // RECORDING_POST_FREQUENCY_MINS 分ごとに投稿
        if (item.isRecording && elapsedMinutes % Env.RECORDING_POST_FREQUENCY_MINUTES != 0) {
            return
        }

        // 番組の長さだけ録画した場合には無視
        // ex) 30分番組で【30分録画中】とならないようにする
        if (item.isRecording && elapsedMinutes == totalMinutes) {
            return
        }

        logger.trace { item }

        val text = when {
                // 予約追加
                item.isReserve -> Env.RESERVES_FORMAT
                // 録画開始
                item.isRecording && elapsedMinutes == 0 -> Env.RECORD_START_FORMAT
                // 録画中
                item.isRecording -> Env.RECORDING_FORMAT
                // 録画完了
                else -> Env.RECORD_END_FORMAT
            // 改行文字
            }.replace("%BR%") {
                "\n"
            // 予約のタイプ (ルール or 手動)
            }.replace("%RESERVE_TYPE%") {
                if (item.ruleId != null) {
                    "ルール"
                } else {
                    "手動"
                }
            // 番組名
            // 半角・全角かは USE_HALF_WIDTH 環境変数によりコントロール可能, 以下同様
            }.replace("%NAME%") {
                item.name
            // チャンネル名, 同上
            }.replace("%CHANNEL%") {
                val channels = EPGStationApi.getChannels()
                val channel = channels.find { it.id == item.channelId }

                if (Env.USE_HALF_WIDTH) {
                    channel?.halfWidthName
                } else {
                    channel?.name
                }.orEmpty()
            // 番組説明, 同上
            // デフォルトは 50 文字まで出力するが DESCRIPTION_LENGTH 環境変数によりコントロール可能
            }.replace("%DESCRIPTION%") {
                "${item.description.orEmpty()}\n${item.extended.orEmpty()}".trim().omit(Env.DESCRIPTION_LENGTH)
            // 経過時間 (分)
            // RESERVES_FORMAT で使用することは想定されていない
            }.replace("%ELAPSED_MINUTES%") {
                elapsedMinutes.toString()
            // 番組の開始時刻
            // 時刻のフォーマットは TIME_FORMAT 環境変数によりコントロール可能
            }.replace("%START_TIME%") {
                val formatter = DateTimeFormatter.ofPattern(Env.TIME_FORMAT)
                val startAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.startAt), ZoneOffset.systemDefault())
                formatter.format(startAt)
            // 番組の終了時刻, 同上
            }.replace("%END_TIME%") {
                val formatter = DateTimeFormatter.ofPattern(Env.TIME_FORMAT)
                val endAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.endAt), ZoneOffset.systemDefault())
                formatter.format(endAt)
            // 番組の長さ (xx時間xx分 形式)
            }.replace("%DURATION%") {
                val duration = (item.endAt - item.startAt).milliseconds
                val hours = floor(duration.inHours).toInt()
                val minutes =  duration.inMinutes.roundToInt() - hours * 60

                buildString {
                    if (hours > 0) {
                        append("${hours}時間")
                    }
                    append("${minutes}分")
                }
            // 番組の容量 (GB 単位)
            // 録画済でなければ空文字を出力
            }.replace("%SIZE_GB%") {
                val giga = 2.0.pow(30)
                item.videoFiles.firstOrNull()?.size?.div(giga)?.let { String.format("%.1f", it) }.orEmpty()
            }

        // 予約ならメディアを添付できないため, そのままツイートする
        if (item.isReserve) {
            postTweet(text, input = null, withMp4 = false, withPng = false, isRecording = false)
            return
        }

        // EPGStation は録画ファイルの絶対パスを持たないため, マウントポイントから検索する
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
        if (input != null && elapsedMinutes == 0) {
            delay(3.seconds)
        }

        postTweet(
            text = text,
            input = input,
            // 録画開始直後は mp4 を生成できない
            withMp4 = elapsedMinutes > 0 && Env.WITH_MP4,
            withPng = Env.WITH_PNG,
            isRecording = item.isRecording
        )
    }

    /**
     * 出現した `pattern` を評価した block で置換する
     *
     * @param pattern 出現文字列
     * @param block 遅延評価される置換文字列
     */
    private inline fun String.replace(pattern: String, block: () -> String): String {
        if (!contains(pattern)) {
            return this
        }

        val value = block()
        return replace(pattern, value)
    }

    /**
     * 文字列の長さを `limit` に制限し, 超えた場合には三点リーダ (…) を付与する
     *
     * @param limit 文字列の最大長
     */
    private fun String.omit(limit: Int): String {
        return if (length > limit) {
            take(limit) + "…"
        } else {
            this
        }
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

        if (Env.DRYRUN) {
            return
        }

        try {
            EpgbirdTwitterClient.statuses.create(text).execute()
        } catch (t: Throwable) {
            logger.error(t) { "Failed to create status." }
        }
    }
}
