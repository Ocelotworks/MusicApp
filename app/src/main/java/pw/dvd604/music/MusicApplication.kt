package pw.dvd604.music

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceManager
import androidx.room.Room
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.HttpSenderConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.sender.HttpSender
import pw.dvd604.music.util.AppDatabase
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import java.util.*

@AcraCore(buildConfigClass = BuildConfig::class)

class MusicApplication : Application(), Application.ActivityLifecycleCallbacks {

    private val mixpanelToken = TokenStore.mixPanelToken
    var mixpanel: MixpanelAPI? = null
    lateinit var prefs : SharedPreferences


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        Settings.init(this)

        if(Settings.getBoolean(Settings.crashReports, true)) {
            val builder: CoreConfigurationBuilder =
                CoreConfigurationBuilder(this).setBuildConfigClass(BuildConfig::class.java)
                    .setReportFormat(StringFormat.JSON)
            builder.getPluginConfigurationBuilder(HttpSenderConfigurationBuilder::class.java)
                .setUri(TokenStore.reportURL)
                .setHttpMethod(HttpSender.Method.POST)
                .setBasicAuthLogin(TokenStore.reportsName)
                .setBasicAuthPassword(TokenStore.reportsPassword)
                .setEnabled(true)
            ACRA.init(this, builder)
        }
    }

    override fun onCreate() {
        super.onCreate()
        this.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        mixpanel = MixpanelAPI.getInstance(this, mixpanelToken)

        mixpanel?.identify(Util.getTrackingID())
        mixpanel?.people?.identify(Util.getTrackingID())

        mixpanel?.track("App start")

        (activity as MainActivity).startTracking()
    }

    override fun onActivityDestroyed(activity: Activity?) {
        mixpanel?.track("App close")
        mixpanel?.flush()
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
    }

}