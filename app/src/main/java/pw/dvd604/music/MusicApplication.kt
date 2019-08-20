package pw.dvd604.music

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceManager
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat
import pw.dvd604.music.acra.HttpSenderFactory
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util

class MusicApplication : Application(), Application.ActivityLifecycleCallbacks {

    private val mixpanelToken = TokenStore.mixPanelToken
    var mixpanel: MixpanelAPI? = null
    lateinit var prefs : SharedPreferences


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        Settings.init(this)

        if(Settings.getBoolean(Settings.crashReports, true)) {

            val builder = CoreConfigurationBuilder(this)
            builder.setBuildConfigClass(BuildConfig::class.java).setReportFormat(StringFormat.JSON)
            builder.setReportSenderFactoryClasses(HttpSenderFactory::class.java)
            builder.setEnabled(true)

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