package blue.starry.epgbird

import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration

val logger = KotlinLogging.createFeedchimeLogger("epgbird")

suspend fun main(): Unit = coroutineScope {
    logger.info { "Application started!" }

    watchRecording()
    watchRecordedOrReserves()
}

private fun CoroutineScope.watchRecording() = launch {
    while (isActive) {
        if (Env.INCLUDE_RECORDING || Env.INCLUDE_RECORD_START) {
            Epgbird.checkRecording()
        }

        delay(Duration.minutes(1))
    }
}

private fun CoroutineScope.watchRecordedOrReserves() = launch {
    while (isActive) {
        listOf(
            launch {
                if (Env.INCLUDE_RECORD_END) {
                    Epgbird.checkRecorded()
                }
            },
            launch {
                if (Env.INCLUDE_RESERVES) {
                    Epgbird.checkReserves()
                }
            }
        ).joinAll()

        delay(Duration.seconds(Env.CHECK_INTERVAL_SECONDS))
    }
}
