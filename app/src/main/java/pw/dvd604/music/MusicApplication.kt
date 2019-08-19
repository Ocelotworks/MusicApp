package pw.dvd604.music

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.acra.annotation.AcraCore
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.MailSenderConfigurationBuilder
import org.acra.data.StringFormat
import pw.dvd604.music.util.Settings
import java.util.*

@AcraCore(buildConfigClass = BuildConfig::class)

class MusicApplication : Application(), Application.ActivityLifecycleCallbacks {

    private val mixpanelToken = "55d111267e0216c6895dbac9a5bdfbad"
    var mixpanel: MixpanelAPI? = null
    lateinit var prefs : SharedPreferences


    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val builder: CoreConfigurationBuilder =
            CoreConfigurationBuilder(this).setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder::class.java)
            .setMailTo("reports@printrworks.co.uk").setEnabled(true)
        //ACRA.init(this, builder)
    }

    override fun onCreate() {
        super.onCreate()
        Settings.init(this)
        this.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        mixpanel = MixpanelAPI.getInstance(this, mixpanelToken)
        val uuid = prefs.getString("trackingID", UUID.randomUUID().toString())
        prefs.edit().putString("trackingID", uuid).apply()


        mixpanel?.identify(uuid)
        mixpanel?.people?.identify(uuid)

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