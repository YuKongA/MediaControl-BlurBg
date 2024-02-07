package top.yukonga.mediaControlBlur.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color

object AppUtils {

    val GREY = Color.parseColor("#857772")

    fun colorFilterCompat(colorInt: Int) = BlendModeColorFilter(colorInt, BlendMode.SRC_IN)

    fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

}