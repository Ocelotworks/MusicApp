package pw.dvd604.music.acra

import android.content.Context
import android.util.Log
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import org.json.JSONObject

class HttpSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        Log.e(this::class.java.name, "Yes")
        return HttpSender()
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}

class HttpSender : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {

        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("content", parse(errorContent))
    }

    private fun parse(rd: CrashReportData): String {
        Log.e(this::class.java.name, rd.toJSON())
        return ""
    }
}