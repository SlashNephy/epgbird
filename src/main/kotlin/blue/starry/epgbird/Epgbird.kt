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
import java.util.*
import kotlin.io.path.name
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.milliseconds

@Suppress("BlockingMethodInNonBlockingContext")
object Epgbird {
    private var lastRecordedId = 0L
    private var lastReserveId = 0L

    suspend fun checkRecording(): Unit = coroutineScope {
        EPGStationApi.getRecording()
            .records
            // 無視するチャンネルの番組をスキップ
            .filter { it.channelId !in Env.IGNORE_CHANNEL_IDS }
            .map { item ->
                launch { // 経過時間 (分)
                    val elapsedMinutes = Duration.milliseconds((Instant.now().toEpochMilli() - item.startAt)).toDouble(DurationUnit.MINUTES).roundToInt() // 番組の長さ (分)
                    val totalMinutes = Duration.milliseconds((item.endAt - item.startAt)).toDouble(DurationUnit.MINUTES).roundToInt() // RECORDING_POST_FREQUENCY_MINS 分ごとに投稿
                    if (item.isRecording && elapsedMinutes % Env.RECORDING_POST_FREQUENCY_MINUTES != 0) {
                        return@launch
                    } // 番組の長さだけ録画した場合には無視
                    // ex) 30分番組で【30分録画中】とならないようにする
                    if (item.isRecording && elapsedMinutes == totalMinutes) {
                        return@launch
                    }

                    postTweet(item)
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
            .map { item ->
                launch {
                    postTweet(item)
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
            .map { item ->
                launch {
                    postTweet(item)
                }
            }.joinAll()

        lastReserveId = maxId
    }

    private suspend fun postTweet(item: ProgramItem) {
        logger.trace { item }

        val text = TemplateFormatter(item).format()

        // 予約ならメディアを添付できないため, そのままツイートする
        if (item.isReserve) {
            postTweet(
                text = text,
                input = null,
                withMp4 = false,
                withPng = false,
                isRecording = false
            )
        } else {
            // EPGStation は録画ファイルの絶対パスを持たないため, マウントポイントから検索する
            val mnt = Paths.get(Env.MOUNT_POINT)
            val filename = item.videoFiles.firstOrNull()?.filename
            val input = filename?.let { epgsFilename ->
                withContext(Dispatchers.IO) {
                    Files.find(mnt, Env.MOUNT_POINT_MAX_DEPTH, { path, _ ->
                        path.name == epgsFilename
                    }).findFirst().orElse(null)
                }
            }

            postTweet(
                text = text,
                input = input,
                withMp4 = Env.WITH_MP4,
                withPng = Env.WITH_PNG,
                isRecording = item.isRecording
            )
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
        input: Path?,
        withMp4: Boolean,
        withPng: Boolean,
        isRecording: Boolean
    ) {
        if (withMp4 && input != null) {
            val mp4 = Files.createTempFile(UUID.randomUUID().toString(), ".mp4")

            try {
                FFMPEGUtil.createMP4(input, mp4, isRecording)

                if (Env.DRYRUN) {
                    return
                }

                EpgbirdTwitterClient.use { client ->
                    client.statuses.createWithMedia(
                        status = text, media = listOf(
                            MediaComponent(mp4, MediaType.MP4, MediaCategory.TweetVideo)
                        )
                    ).execute()
                }

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

                EpgbirdTwitterClient.use { client ->
                    client.statuses.createWithMedia(text, media = listOf(MediaComponent(png, MediaType.PNG, MediaCategory.TweetImage))).execute()
                }

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
            EpgbirdTwitterClient.use { client ->
                client.statuses.create(text).execute()
            }
        } catch (t: Throwable) {
            logger.error(t) { "Failed to create status." }
        }
    }
}
