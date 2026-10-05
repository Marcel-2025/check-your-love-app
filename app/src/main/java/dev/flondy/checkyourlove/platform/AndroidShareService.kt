package dev.flondy.checkyourlove.platform

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import dev.flondy.checkyourlove.CurrentActivity
import java.io.File

/** Speichert das Bild im freigegebenen Cache-Ordner und öffnet den Android-Teilen-Dialog. */
class AndroidShareService(context: Context) : ShareService {
    private val appContext = context.applicationContext

    override fun shareImage(png: ByteArray, text: String, chooserTitle: String) {
        val dir = File(appContext.cacheDir, "shared").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, "check-your-love-${System.currentTimeMillis()}.png")
        file.writeBytes(png)
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.share", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, chooserTitle)
        val activity = CurrentActivity.get()
        if (activity != null) activity.startActivity(chooser)
        else appContext.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
