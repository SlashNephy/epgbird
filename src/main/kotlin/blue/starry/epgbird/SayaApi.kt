package blue.starry.epgbird

import io.ktor.client.request.*

object SayaApi {
    suspend fun getCommentInfo(channel: ChannelItem): CommentInfo = EpgbirdHttpClient.get("http://${Env.SAYA_HOST}:${Env.SAYA_PORT}/comments/${channel.channelType}_${channel.serviceId}/info")
}
