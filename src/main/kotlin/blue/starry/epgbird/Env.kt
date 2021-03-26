package blue.starry.epgbird

import kotlin.properties.ReadOnlyProperty

object Env {
    val CHECK_INTERVAL_SECONDS by long { 10 }
    val LOG_LEVEL by stringOrNull
    val DRYRUN by boolean { false }
    val MOUNT_POINT by string { "/mnt" }

    val INCLUDE_RECORDED by boolean { true }
    val INCLUDE_RECORDING by boolean { true }
    val INCLUDE_RESERVES by boolean { true }
    val INCLUDE_RULE_RESERVES by boolean { false }
    val RECORDING_POST_FREQUENCY_MINUTES by int { 10 }

    val IGNORE_CHANNEL_IDS by longList

    val WITH_PNG by boolean { false }
    val FFMPEG_PNG_COMMAND by string { "ffmpeg %POSITION% -i %INPUT% -vframes 1 -f image2 -s 1920x1080 -loglevel error -y %OUTPUT%" }
    val FFMPEG_PNG_TIMEOUT_SECONDS by long { 5 }
    val WITH_MP4 by boolean { false }
    val FFMPEG_MP4_COMMAND by string { "ffmpeg %POSITION% -t 120 -i %INPUT% -f mp4 -c:a aac -ab 128k -ar 48000 -ac 2 -c:v libx264 -pix_fmt yuv420p -vf scale=1280:-1 -vb 2048k -r 30 -minrate 1024k -maxrate 2048k -strict experimental -threads 1 -loglevel error -y %OUTPUT%" }
    val FFMPEG_MP4_TIMEOUT_SECONDS by long { 30 }

    val EPGSTATION_HOST by string { "epgstation" }
    val EPGSTATION_PORT by int { 8888 }

    val TWITTER_CK by string
    val TWITTER_CS by string
    val TWITTER_AT by string
    val TWITTER_ATS by string
}

private fun boolean(default: () -> Boolean): ReadOnlyProperty<Env, Boolean> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toBooleanFuzzy() ?: default()
}

private fun String?.toBooleanFuzzy(): Boolean {
    return when (this) {
        null -> false
        "1", "yes" -> true
        else -> toLowerCase().toBoolean()
    }
}

private val string: ReadOnlyProperty<Env, String>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name) ?: error("Env: ${property.name} is not present.")
    }

private val stringOrNull: ReadOnlyProperty<Env, String?>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)
    }

private fun string(default: () -> String): ReadOnlyProperty<Env, String> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) ?: default()
}

private fun int(default: () -> Int): ReadOnlyProperty<Env, Int> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toIntOrNull() ?: default()
}

private fun long(default: () -> Long): ReadOnlyProperty<Env, Long> = ReadOnlyProperty { _, property ->
    System.getenv(property.name)?.toLongOrNull() ?: default()
}

private val longList: ReadOnlyProperty<Env, List<Long>>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.split(",")?.mapNotNull { it.trim().toLongOrNull() }.orEmpty()
    }

private val stringList: ReadOnlyProperty<Env, List<String>>
    get() = ReadOnlyProperty { _, property ->
        System.getenv(property.name)?.split(",")?.map { it.trim() }.orEmpty()
    }
