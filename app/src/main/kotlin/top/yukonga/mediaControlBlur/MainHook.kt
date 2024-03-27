package top.yukonga.mediaControlBlur

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.graphics.drawable.ShapeDrawable
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createAfterHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createBeforeHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.yukonga.mediaControlBlur.utils.AppUtils.colorFilter
import top.yukonga.mediaControlBlur.utils.AppUtils.dp
import top.yukonga.mediaControlBlur.utils.AppUtils.isDarkMode
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setBlurRoundRect
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiBackgroundBlendColors
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiViewBlurMode

class MainHook : IXposedHookLoadPackage {

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag("MediaControlBlur")
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                try {
                    val mediaControlPanel = loadClassOrNull("com.android.systemui.media.controls.ui.MediaControlPanel")
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
                    val notificationUtil = loadClassOrNull("com.android.systemui.statusbar.notification.NotificationUtil")
                    val playerTwoCircleView = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.PlayerTwoCircleView")
                    val seekBarObserver = loadClassOrNull("com.android.systemui.media.controls.models.player.SeekBarObserver")

                    seekBarObserver?.constructors?.first()?.createAfterHook {
                        it.thisObject.objectHelper().setObject("seekBarEnabledMaxHeight", 8.dp)
                        val seekBar = it.args[0].objectHelper().getObjectOrNullAs<SeekBar>("seekBar")
                        seekBar?.apply {
                            thumb = ShapeDrawable().apply {
                                intrinsicWidth = 8.dp
                                intrinsicHeight = 8.dp
                            }
                        }
                    }

                    mediaControlPanel?.methodFinder()?.filterByName("attachPlayer")?.first()?.createAfterHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@createAfterHook

                        val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@createAfterHook
                        val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@createAfterHook

                        mediaBg.apply {
                            setMiViewBlurMode(1)
                            setBlurRoundRect(getNotificationElementRoundRect(context))
                            setMiBackgroundBlendColors(getNotificationElementBlendColors(context), 1f)
                        }
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createAfterHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean

                        val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@createAfterHook

                        val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@createAfterHook
                        val titleText = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("titleText")
                        val artistText = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("artistText")
                        val seamlessIcon = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("seamlessIcon")
                        val action0 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action0")
                        val action1 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action1")
                        val action2 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action2")
                        val action3 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action3")
                        val action4 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action4")
                        val seekBar = mMediaViewHolder.objectHelper().getObjectOrNullAs<SeekBar>("seekBar")
                        val elapsedTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("elapsedTimeView")
                        val totalTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("totalTimeView")
                        val albumView = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("albumView")
                        val appIcon = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("appIcon")

                        val grey = if (isDarkMode(context)) Color.parseColor("#80ffffff") else Color.parseColor("#99000000")

                        if (!isBackgroundBlurOpened) {
                            titleText?.setTextColor(Color.WHITE)
                            seamlessIcon?.setColorFilter(Color.WHITE)
                            action0?.setColorFilter(Color.WHITE)
                            action1?.setColorFilter(Color.WHITE)
                            action2?.setColorFilter(Color.WHITE)
                            action3?.setColorFilter(Color.WHITE)
                            action4?.setColorFilter(Color.WHITE)
                            seekBar?.progressDrawable?.colorFilter = colorFilter(Color.WHITE)
                            seekBar?.thumb?.colorFilter = colorFilter(Color.WHITE)
                        } else {
                            if (!isDarkMode(context)) {
                                titleText?.setTextColor(Color.BLACK)
                                artistText?.setTextColor(grey)
                                seamlessIcon?.setColorFilter(Color.BLACK)
                                action0?.setColorFilter(Color.BLACK)
                                action1?.setColorFilter(Color.BLACK)
                                action2?.setColorFilter(Color.BLACK)
                                action3?.setColorFilter(Color.BLACK)
                                action4?.setColorFilter(Color.BLACK)
                                seekBar?.progressDrawable?.colorFilter = colorFilter(Color.argb(165, 0, 0, 0))
                                seekBar?.thumb?.colorFilter = colorFilter(Color.TRANSPARENT)
                                elapsedTimeView?.setTextColor(grey)
                                totalTimeView?.setTextColor(grey)
                            } else {
                                titleText?.setTextColor(Color.WHITE)
                                artistText?.setTextColor(grey)
                                seamlessIcon?.setColorFilter(Color.WHITE)
                                action0?.setColorFilter(Color.WHITE)
                                action1?.setColorFilter(Color.WHITE)
                                action2?.setColorFilter(Color.WHITE)
                                action3?.setColorFilter(Color.WHITE)
                                action4?.setColorFilter(Color.WHITE)
                                seekBar?.progressDrawable?.colorFilter = colorFilter(Color.argb(165, 255, 255, 255))
                                seekBar?.thumb?.colorFilter = colorFilter(Color.TRANSPARENT)
                                elapsedTimeView?.setTextColor(grey)
                                totalTimeView?.setTextColor(grey)
                            }

                            mediaBg.setMiBackgroundBlendColors(getNotificationElementBlendColors(context), 1f)

                            val artwork = it.args[0].objectHelper().getObjectOrNullAs<Icon>("artwork") ?: return@createAfterHook
                            val artworkLayer = artwork.loadDrawable(context) ?: return@createAfterHook
                            val artworkBitmap = Bitmap.createBitmap(artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(artworkBitmap)
                            artworkLayer.setBounds(0, 0, artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight)
                            artworkLayer.draw(canvas)
                            val resizedBitmap = Bitmap.createScaledBitmap(artworkBitmap, 300, 300, true)

                            val radius = 45f
                            val newBitmap = Bitmap.createBitmap(resizedBitmap.width, resizedBitmap.height, Bitmap.Config.ARGB_8888)
                            val canvas1 = Canvas(newBitmap)

                            val paint = Paint()
                            val rect = Rect(0, 0, resizedBitmap.width, resizedBitmap.height)
                            val rectF = RectF(rect)

                            paint.isAntiAlias = true
                            canvas1.drawARGB(0, 0, 0, 0)
                            paint.color = Color.BLACK
                            canvas1.drawRoundRect(rectF, radius, radius, paint)

                            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                            canvas1.drawBitmap(resizedBitmap, rect, rect, paint)

                            albumView?.setImageDrawable(BitmapDrawable(context.resources, newBitmap))

                            (appIcon?.parent as ViewGroup?)?.removeView(appIcon)

                            elapsedTimeView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
                            totalTimeView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
                        }
                    }

                    playerTwoCircleView?.methodFinder()?.filterByName("onDraw")?.first()?.createBeforeHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@createBeforeHook

                        it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint1")?.alpha = 0
                        it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint2")?.alpha = 0
                        it.thisObject.objectHelper().setObject("mRadius", 0f)
                    }

                    playerTwoCircleView?.methodFinder()?.filterByName("setBackground")?.first()?.createBeforeHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@createBeforeHook

                        it.result = null
                    }
                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }


    @SuppressLint("DiscouragedApi")
    fun getNotificationElementBlendColors(context: Context): IntArray {
        val resources = context.resources
        return try {
            val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", "com.android.systemui")
            resources.getIntArray(arrayId)
        } catch (_: Exception) {
            val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", "com.android.systemui")
            resources.getIntArray(arrayId)
        }
    }

    @SuppressLint("DiscouragedApi")
    fun getNotificationElementRoundRect(context: Context): Int {
        val resources = context.resources
        val dimenId = resources.getIdentifier("notification_item_bg_radius", "dimen", "com.android.systemui")
        return resources.getDimensionPixelSize(dimenId)
    }
}