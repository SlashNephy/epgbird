package blue.starry.epgbird

import kotlinx.serialization.Serializable

@Serializable
data class CommentInfo(
    val force: Int,
    val last: String
)
