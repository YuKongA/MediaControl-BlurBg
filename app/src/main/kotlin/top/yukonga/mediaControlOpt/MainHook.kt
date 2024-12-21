package top.yukonga.mediaControlOpt

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createAfterHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.yukonga.mediaControlOpt.utils.AppUtils.colorFilter
import top.yukonga.mediaControlOpt.utils.AppUtils.dp
import top.yukonga.mediaControlOpt.utils.AppUtils.isDarkMode

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag("MediaControlBlur")
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                try {
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
                    val mediaViewHolder = loadClassOrNull("com.android.systemui.media.controls.ui.view.MediaViewHolder")

                    mediaViewHolder?.constructors?.first()?.createAfterHook {
                        val seekBar = it.thisObject.objectHelper().getObjectOrNullAs<SeekBar>("seekBar")
                        val backgroundDrawable = GradientDrawable().apply {
                            color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#20ffffff")))
                            cornerRadius = 7.dp.toFloat()
                        }
                        val onProgressDrawable = GradientDrawable().apply {
                            color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#ccffffff")))
                            cornerRadius = 7.dp.toFloat()
                        }
                        val layerDrawable = LayerDrawable(
                            arrayOf(backgroundDrawable, ClipDrawable(onProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL))
                        ).apply {
                            val layerHeight = 7.dp

                            val totalHeight = seekBar?.height ?: 0
                            val topOffset = (totalHeight - layerHeight) / 2

                            setLayerInset(0, 0, topOffset, 0, topOffset)
                            setLayerInset(1, 0, topOffset, 0, topOffset)
                        }
                        seekBar?.progressDrawable = layerDrawable
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createAfterHook {
                        val context = it.thisObject.objectHelper().getObjectOrNullUntilSuperclassAs<Context>("mContext") ?: return@createAfterHook
                        val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@createAfterHook

                        val appIcon = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("appIcon")
                        (appIcon?.parent as ViewGroup?)?.removeView(appIcon)

                        val seekBar = mMediaViewHolder.objectHelper().getObjectOrNullAs<SeekBar>("seekBar")
                        seekBar?.thumb?.colorFilter = colorFilter(Color.TRANSPARENT)

                        val elapsedTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("elapsedTimeView")
                        val totalTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("totalTimeView")
                        val grey = if (isDarkMode(context)) Color.LTGRAY else Color.DKGRAY

                        elapsedTimeView?.setTextColor(grey)
                        totalTimeView?.setTextColor(grey)
                        elapsedTimeView?.textSize = 12f
                        totalTimeView?.textSize = 12f
                    }
                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }
}