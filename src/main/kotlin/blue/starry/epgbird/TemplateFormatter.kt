package blue.starry.epgbird

import com.twitter.twittertext.TwitterTextParser
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

class TemplateFormatter(private val item: ProgramItem) {
    private var _channel: ChannelItem? = null
    private var _comment: CommentInfo? = null

    private suspend fun getChannel(): ChannelItem? {
        if (_channel == null) {
            _channel = EPGStationApi.getChannels().find { it.id == item.channelId }
        }

        return _channel
    }

    private suspend fun getComment(): CommentInfo? {
        if (_comment == null) {
            _comment = SayaApi.getCommentInfo(getChannel() ?: return null)
        }

        return _comment
    }

    suspend fun format(): String { // 経過時間 (分)
        val elapsedMinutes = ((Instant.now().toEpochMilli() - item.startAt)).milliseconds.inWholeMinutes
        val template = when { // 予約追加
            item.isReserve -> Env.RESERVES_FORMAT // 録画開始
            item.isRecording && elapsedMinutes == 0L -> Env.RECORD_START_FORMAT // 録画中
            item.isRecording -> Env.RECORDING_FORMAT // 録画完了
            else -> Env.RECORD_END_FORMAT
        }
        var text = template // 改行文字
            .replace("%BR%") {
                "\n" // 予約のタイプ (ルール or 手動)
            }.replace("%RESERVE_TYPE%") {
                if (item.ruleId != null) {
                    "ルール"
                } else {
                    "手動"
                } // 番組名
                // 半角・全角かは USE_HALF_WIDTH 環境変数によりコントロール可能, 以下同様
            }.replace("%NAME%") {
                item.name // チャンネル名, 同上
            }.replace("%CHANNEL%") {
                if (Env.USE_HALF_WIDTH) {
                    getChannel()?.halfWidthName
                } else {
                    getChannel()?.name
                } // 番組説明, 同上
                // デフォルトは 50 文字まで出力するが DESCRIPTION_LENGTH 環境変数によりコントロール可能
            }.replace("%DESCRIPTION%") {
                "${item.description.orEmpty()}\n${item.extended.orEmpty()}".trim().omit(Env.DESCRIPTION_LENGTH) // 経過時間 (分)
                // RESERVES_FORMAT で使用することは想定されていない
            }.replace("%ELAPSED_MINUTES%") {
                elapsedMinutes // 番組の開始時刻
                // 時刻のフォーマットは TIME_FORMAT 環境変数によりコントロール可能
            }.replace("%START_TIME%") {
                val formatter = DateTimeFormatter.ofPattern(Env.TIME_FORMAT)
                val startAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.startAt), ZoneOffset.systemDefault())
                formatter.format(startAt) // 番組の終了時刻, 同上
            }.replace("%END_TIME%") {
                val formatter = DateTimeFormatter.ofPattern(Env.TIME_FORMAT)
                val endAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(item.endAt), ZoneOffset.systemDefault())
                formatter.format(endAt) // 番組の長さ (xx時間xx分 形式)
            }.replace("%DURATION%") {
                val duration = (item.endAt - item.startAt).milliseconds
                val hours = floor(duration.toDouble(DurationUnit.HOURS)).toInt()
                val minutes = duration.toDouble(DurationUnit.MINUTES).roundToInt() - hours * 60

                buildString {
                    if (hours > 0) {
                        append("${hours}時間")
                    }
                    append("${minutes}分")
                } // 番組の容量 (GB 単位)
                // 録画済でなければ空文字を出力
            }.replace("%SIZE_GB%") {
                val giga = 2.0.pow(30)
                item.videoFiles.firstOrNull()?.size?.div(giga)?.let { String.format("%.1f", it) } // コメントのダイジェスト (saya が必要)
            }.replace("%COMMENT_DIGEST%") {
                getComment()?.last // コメントの勢い (/min), 同上
            }.replace("%COMMENT_FORCE%") {
                getComment()?.force // ドロップ数
            }.replace("%DROP_COUNT%") {
                item.dropLogFile?.dropCnt // エラー数
            }.replace("%ERROR_COUNT%") {
                item.dropLogFile?.errorCnt // スクランブル数
            }.replace("%SCRAMBLE_COUNT%") {
                item.dropLogFile?.scramblingCnt // 映像のコーデック
            }.replace("%VIDEO_CODEC%") {
                item.videoType?.uppercase(Locale.getDefault()) // 映像の解像度
            }.replace("%VIDEO_RESOLUTION%") {
                item.videoResolution // 音声のサンプリング周波数 (kHz)
            }.replace("%AUDIO_SAMPLING_RATE_KHZ%") {
                item.audioSamplingRate?.div(1000.0F)
            }.trim()

        while (!TwitterTextParser.parseTweet(text).isValid) {
            text = text.dropLast(1)
        }

        return text
    }

    /**
     * 出現した `pattern` を評価した block で置換する
     *
     * @param pattern 出現文字列
     * @param block 遅延評価される置換文字列
     */
    private inline fun String.replace(pattern: String, block: () -> Any?): String {
        if (!contains(pattern)) {
            return this
        }

        val value = block()?.toString().orEmpty()
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
}
