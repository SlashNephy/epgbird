package blue.starry.epgbird

import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString

object FFMPEGUtil {
    fun createPNG(input: Path, output: Path, fromEnd: Boolean) {
        val position = if (fromEnd) {
            "-sseof -1"
        } else {
            "-ss 30"
        }

        val commands = Env.FFMPEG_PNG_COMMAND
            .replace("%POSITION%", position)
            .split(" ").map {
                when (it) {
                    "%INPUT%" -> input.absolutePathString()
                    "%OUTPUT%" -> output.absolutePathString()
                    else -> it
                }
            }
        logger.debug { "ffmpeg png: $commands" }

        val process = ProcessBuilder(commands).redirectError(ProcessBuilder.Redirect.INHERIT).start()
        if (!process.waitFor(Env.FFMPEG_PNG_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroy()
        }

        logger.debug { "ffmpeg png process done. ($output)" }
    }

    fun createMP4(input: Path, output: Path, fromEnd: Boolean) {
        val position = if (fromEnd) {
            "-sseof -121"
        } else {
            "-ss 1"
        }

        val commands = Env.FFMPEG_MP4_COMMAND
            .replace("%POSITION%", position)
            .split(" ").map {
                when (it) {
                    "%INPUT%" -> input.absolutePathString()
                    "%OUTPUT%" -> output.absolutePathString()
                    else -> it
                }
            }
        logger.trace { "ffmpeg mp4: $commands" }

        val process = ProcessBuilder(commands).redirectError(ProcessBuilder.Redirect.INHERIT).start()
        if (!process.waitFor(Env.FFMPEG_MP4_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroy()
        }

        logger.debug { "ffmpeg mp4 process done. ($output)" }
    }
}
