package pw.dvd604.music

import android.app.Application
import android.content.Context
import org.acra.ACRA
import org.acra.annotation.*
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.MailSenderConfigurationBuilder
import org.acra.data.StringFormat

@AcraCore(buildConfigClass = BuildConfig::class)

class MusicApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        val builder: CoreConfigurationBuilder =
            CoreConfigurationBuilder(this).setBuildConfigClass(BuildConfig::class.java)
                .setReportFormat(StringFormat.JSON)
        builder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder::class.java)
            .setMailTo("reports@printrworks.co.uk").setEnabled(true)
        //ACRA.init(this, builder)
    }

}