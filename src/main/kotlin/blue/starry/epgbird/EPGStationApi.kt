package blue.starry.epgbird

import io.ktor.client.request.*

object EPGStationApi {
    private val BaseUrl = "http://${Env.EPGSTATION_HOST}:${Env.EPGSTATION_PORT}/api"

    suspend fun getChannels(): List<ChannelItem> = EpgbirdHttpClient.get("$BaseUrl/channels")

    suspend fun getRecording(): RecordingResponse = EpgbirdHttpClient.get("$BaseUrl/recording?isHalfWidth=${Env.USE_HALF_WIDTH}")

    suspend fun getRecorded(): RecordedResponse = EpgbirdHttpClient.get("$BaseUrl/recorded?isHalfWidth=${Env.USE_HALF_WIDTH}&hasOriginalFile=false")
    
    suspend fun getReserves(): ReservesResponse = EpgbirdHttpClient.get("$BaseUrl/reserves?isHalfWidth=${Env.USE_HALF_WIDTH}")
}
