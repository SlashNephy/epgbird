package blue.starry.epgbird

import io.ktor.client.request.*

object SayaApi {
    suspend fun getCommentInfo(channel: ChannelItem): CommentInfo = EpgbirdHttpClient.use { client ->
        client.get("http://${Env.SAYA_HOST}:${Env.SAYA_PORT}${Env.SAYA_BASE_URI}comments/${channel.channelType}_${channel.serviceId}/info")
    }
}
