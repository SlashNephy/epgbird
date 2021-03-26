package blue.starry.epgbird

import io.ktor.client.request.*

object EPGStationApi {
    private val BaseUrl = "http://${Env.EPGSTATION_HOST}:${Env.EPGSTATION_PORT}/api"

    suspend fun getChannels(): List<ChannelItem> = EpgbirdHttpClient.get("$BaseUrl/channels")

    suspend fun getRecording(): RecordingResponse = EpgbirdHttpClient.get("$BaseUrl/recording?isHalfWidth=true")

    suspend fun getRecorded(): RecordedResponse = EpgbirdHttpClient.get("$BaseUrl/recorded?isHalfWidth=true&hasOriginalFile=false")
    
    suspend fun getReserves(): ReservesResponse = EpgbirdHttpClient.get("$BaseUrl/reserves?isHalfWidth=true")
}
