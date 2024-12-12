package top.yukonga.mediaControlOpt.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources.getSystem
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.PowerManager
import android.util.TypedValue

object AppUtils {

    fun colorFilter(colorInt: Int) = BlendModeColorFilter(colorInt, BlendMode.SRC_IN)

    fun isDarkMode(context: Context): Boolean {
        return context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES || !(context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    }

    val Int.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), getSystem().displayMetrics).toInt()
}