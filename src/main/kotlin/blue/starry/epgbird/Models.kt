package blue.starry.epgbird

import kotlinx.serialization.Serializable

@Serializable
data class RecordingResponse(
    val records: List<RecordedItem>,
    val total: Int
)

@Serializable
data class RecordedResponse(
    val records: List<RecordedItem>,
    val total: Int
)

@Serializable
data class ReservesResponse(
    val reserves: List<ReservelItem>,
    val total: Int
)

@Serializable
data class RecordedItem(
    val id: Long,
    val ruleId: Long? = null,
    val programId: Long? = null,
    val channelId: Long? = null,
    val startAt: Long,
    val endAt: Long,
    val name: String,
    val description: String? = null,
    val extended: String? = null,
    val genre1: Int? = null,
    val subGenre1: Int? = null,
    val genre2: Int? = null,
    val subGenre2: Int? = null,
    val genre3: Int? = null,
    val subGenre3: Int? = null,
    val videoType: String? = null,
    val videoResolution: String? = null,
    val videoStreamContent: Int? = null,
    val videoComponentType: Int? = null,
    val audioSamplingRate: Int? = null,
    val audioComponentType: Int? = null,
    val isRecording: Boolean,
    val thumbnails: List<Long> = emptyList(),
    val videoFiles: List<VideoFile> = emptyList(),
    val dropLogFile: DropLogFile? = null,
    val tags: List<RecordedTag> = emptyList(),
    val isEncoding: Boolean,
    val isProtected: Boolean
) {
    @Serializable
    data class VideoFile(
        val id: Long,
        val name: String,
        val filename: String? = null,
        val type: String,
        val size: Long
    )

    @Serializable
    data class 	DropLogFile(
        val id: Long,
        val errorCnt: Int,
        val dropCnt: Int,
        val scramblingCnt: Int
    )

    @Serializable
    data class RecordedTag(
        val id: Long,
        val name: String,
        val color: String
    )
}

@Serializable
data class ReservelItem(
    val id: Long,
    val ruleId: Long? = null,
    val isSkip: Boolean,
    val isConflict: Boolean,
    val isOverlap: Boolean,
    val allowEndLack: Boolean,
    val isTimeSpecified: Boolean,
    val tags: List<Long> = emptyList(),
    val parentDirectoryName: String? = null,
    val directory: String? = null,
    val recordedFormat: String? = null,
    val encodeMode1: Int? = null,
    val encodeParentDirectoryName1: String? = null,
    val encodeDirectory1: String? = null,
    val encodeMode2: Int? = null,
    val encodeParentDirectoryName2: String? = null,
    val encodeDirectory2: String? = null,
    val encodeMode3: Int? = null,
    val encodeParentDirectoryName3: String? = null,
    val encodeDirectory3: String? = null,
    val isDeleteOriginalAfterEncode: Boolean,
    val programId: Long? = null,
    val channelId: Long,
    val startAt: Long,
    val endAt: Long,
    val name: String,
    val description: String? = null,
    val extended: String? = null,
    val genre1: Int? = null,
    val subGenre1: Int? = null,
    val genre2: Int? = null,
    val subGenre2: Int? = null,
    val genre3: Int? = null,
    val subGenre3: Int? = null,
    val videoType: String? = null,
    val videoResolution: String? = null,
    val videoStreamContent: Int? = null,
    val videoComponentType: Int? = null,
    val audioSamplingRate: Int? = null,
    val audioComponentType: Int? = null
)

@Serializable
data class ChannelItem(
    val id: Long,
    val serviceId: Int,
    val networkId: Int,
    val name: String,
    val halfWidthName: String,
    val hasLogoData: Boolean,
    val channelType: String,
    val channel: String,
    val remoteControlKeyId: Int? = null
)
