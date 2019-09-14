package pw.dvd604.music.service

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import java.util.*
import kotlin.math.abs

class StepsListener(private val mediaService: MediaService) : SensorEventListener {
    companion object {
        var lastSteps = 0f
        var lastTime: Long = 0
        var playbackSpeed = 1f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { data ->
            val newSteps = data.values[0]
            val newTime = Date().time
            val stepsDifference = newSteps - lastSteps
            val timeDifference = newTime - lastTime
            val timeInSeconds = timeDifference / 1000

            if (true || timeInSeconds >= 10) {
                val stepsPerMinute: Float = (stepsDifference / timeInSeconds) * 60

                if (!(stepsPerMinute < 0 || stepsPerMinute == Float.NEGATIVE_INFINITY || stepsPerMinute == Float.POSITIVE_INFINITY)) {

                    Util.log(this, "$stepsPerMinute")

                    if (stepsPerMinute >= minMOE() && stepsPerMinute <= maxMOE()) {
                        playbackSpeed = 1f
                    } else {
                        val target = Settings.getInt(Settings.stepTarget)
                        val minSpeed = Settings.getInt(Settings.minSongSpeed)
                        val maxSpeed = Settings.getInt(Settings.maxSongSpeed)
                        if (stepsPerMinute < minMOE()) {
                            playbackSpeed = abs((stepsPerMinute - minMOE()) / 100)
                        }
                        if (stepsPerMinute > maxMOE()) {
                            playbackSpeed = abs((maxMOE() - stepsPerMinute) / 100)
                        }

                    }

                    Util.log(this, "$playbackSpeed")

                    if (mediaService.player.isPlaying) {
                        mediaService.player.playbackParams =
                            mediaService.player.playbackParams.setSpeed(
                                playbackSpeed
                            )
                    }
                }
            }

            lastTime = newTime
            lastSteps = newSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun minMOE(): Int {
        val target = Settings.getInt(Settings.stepTarget)
        val moe = Settings.getInt(Settings.runningMargin)
        return target - (target * (moe / 100))
    }

    fun maxMOE(): Int {
        val target = Settings.getInt(Settings.stepTarget)
        val moe = Settings.getInt(Settings.runningMargin)
        return target + (target * (moe / 100))
    }
}