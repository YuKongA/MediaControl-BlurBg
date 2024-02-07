package top.yukonga.mediaControlBlur.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Properties

object AppUtils {

    val GREY = Color.parseColor("#857772")

    fun colorFilterCompat(colorInt: Int) = BlendModeColorFilter(colorInt, BlendMode.SRC_IN)

    fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    fun getProp(name: String): String {
        var prop = getPropByStream(name)
        if (prop.isEmpty()) prop = getPropByShell(name)
        return prop
    }

    private fun getPropByStream(key: String): String {
        return try {
            val prop = Properties()
            FileInputStream(File(Environment.getRootDirectory(), "build.prop")).use { prop.load(it) }
            prop.getProperty(key, "")
        } catch (_: Exception) {
            ""
        }
    }

    private fun getPropByShell(propName: String): String {
        return try {
            val p = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(p.inputStream), 1024).use { it.readLine() ?: "" }
        } catch (ignore: IOException) {
            ""
        }
    }
}