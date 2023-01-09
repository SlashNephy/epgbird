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

interface ProgramItem {
    val isRecording: Boolean
    val isReserve: Boolean
    val name: String
    val channelId: Long?
    val startAt: Long
    val endAt: Long
    val description: String?
    val extended: String?
    val videoFiles: List<RecordedItem.VideoFile>
    val ruleId: Long?
    val dropLogFile: RecordedItem.DropLogFile?
    val videoType: String?
    val videoResolution: String?
    val audioSamplingRate: Int?
}

@Serializable
data class RecordedItem(
    val id: Long,
    override val ruleId: Long? = null,
    val programId: Long? = null,
    override val channelId: Long? = null,
    override val startAt: Long,
    override val endAt: Long,
    override val name: String,
    override val description: String? = null,
    override val extended: String? = null,
    val genre1: Int? = null,
    val subGenre1: Int? = null,
    val genre2: Int? = null,
    val subGenre2: Int? = null,
    val genre3: Int? = null,
    val subGenre3: Int? = null,
    override val videoType: String? = null,
    override val videoResolution: String? = null,
    val videoStreamContent: Int? = null,
    val videoComponentType: Int? = null,
    override val audioSamplingRate: Int? = null,
    val audioComponentType: Int? = null,
    override val isRecording: Boolean,
    val thumbnails: List<Long> = emptyList(),
    override val videoFiles: List<VideoFile> = emptyList(),
    override val dropLogFile: DropLogFile? = null,
    val tags: List<RecordedTag> = emptyList(),
    val isEncoding: Boolean,
    val isProtected: Boolean
): ProgramItem {
    override val isReserve = false

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
    override val ruleId: Long? = null,
    val isSkip: Boolean,
    val isConflict: Boolean,
    val isOverlap: Boolean,
    val allowEndLack: Boolean,
    val isTimeSpecified: Boolean,
    val tags: List<Long> = emptyList(),
    val parentDirectoryName: String? = null,
    val directory: String? = null,
    val recordedFormat: String? = null,
    val encodeMode1: String? = null,
    val encodeParentDirectoryName1: String? = null,
    val encodeDirectory1: String? = null,
    val encodeMode2: String? = null,
    val encodeParentDirectoryName2: String? = null,
    val encodeDirectory2: String? = null,
    val encodeMode3: String? = null,
    val encodeParentDirectoryName3: String? = null,
    val encodeDirectory3: String? = null,
    val isDeleteOriginalAfterEncode: Boolean,
    val programId: Long? = null,
    override val channelId: Long,
    override val startAt: Long,
    override val endAt: Long,
    override val name: String,
    override val description: String? = null,
    override val extended: String? = null,
    val genre1: Int? = null,
    val subGenre1: Int? = null,
    val genre2: Int? = null,
    val subGenre2: Int? = null,
    val genre3: Int? = null,
    val subGenre3: Int? = null,
    override val videoType: String? = null,
    override val videoResolution: String? = null,
    val videoStreamContent: Int? = null,
    val videoComponentType: Int? = null,
    override val audioSamplingRate: Int? = null,
    val audioComponentType: Int? = null
): ProgramItem {
    override val isRecording = false
    override val isReserve = true
    override val videoFiles = emptyList<RecordedItem.VideoFile>()
    override val dropLogFile: RecordedItem.DropLogFile? = null
}

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
