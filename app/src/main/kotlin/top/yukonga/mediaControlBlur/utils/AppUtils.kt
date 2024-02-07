package top.yukonga.mediaControlBlur.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

object AppUtils {

    val GREY = Color.parseColor("#857772")

    fun colorFilterCompat(colorInt: Int) = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(colorInt, BlendModeCompat.SRC_IN)

    fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

}