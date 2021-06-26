package blue.starry.epgbird

import io.ktor.client.request.*

object EPGStationApi {
    private val BaseUrl = "http://${Env.EPGSTATION_HOST}:${Env.EPGSTATION_PORT}/api"

    suspend fun getChannels(): List<ChannelItem> = EpgbirdHttpClient.use { client ->
        client.get("$BaseUrl/channels")
    }

    suspend fun getRecording(): RecordingResponse = EpgbirdHttpClient.use { client ->
        client.get("$BaseUrl/recording?isHalfWidth=${Env.USE_HALF_WIDTH}")
    }

    suspend fun getRecorded(): RecordedResponse = EpgbirdHttpClient.use { client ->
        client.get("$BaseUrl/recorded?isHalfWidth=${Env.USE_HALF_WIDTH}&hasOriginalFile=false")
    }

    suspend fun getReserves(): ReservesResponse = EpgbirdHttpClient.use { client ->
        client.get("$BaseUrl/reserves?isHalfWidth=${Env.USE_HALF_WIDTH}")
    }
}
