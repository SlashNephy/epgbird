package blue.starry.epgbird

import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

val logger = KotlinLogging.createFeedchimeLogger("epgbird")

suspend fun main(): Unit = coroutineScope {
    logger.info { "Application started!" }

    watchRecording()
    watchRecordedOrReserves()
}

@OptIn(ExperimentalTime::class)
private fun CoroutineScope.watchRecording() = launch {
    while (isActive) {
        if (Env.INCLUDE_RECORDING || Env.INCLUDE_RECORD_START) {
            Epgbird.checkRecording()
        }

        delay(1.minutes)
    }
}

@OptIn(ExperimentalTime::class)
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

        delay(Env.CHECK_INTERVAL_SECONDS.seconds)
    }
}
