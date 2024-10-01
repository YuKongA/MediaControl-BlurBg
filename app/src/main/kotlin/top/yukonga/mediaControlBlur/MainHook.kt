package top.yukonga.mediaControlBlur

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
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
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.BACKGROUND
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
                    var lockScreenStatus: Boolean? = null
                    var darkModeStatus: Boolean? = null

                    val mediaViewHolder = if (Build.VERSION.SDK_INT > 34) {
                        loadClassOrNull("com.android.systemui.media.controls.ui.view.MediaViewHolder")
                    } else {
                        loadClassOrNull("com.android.systemui.media.controls.models.player.MediaViewHolder")
                    }
                    val seekBarObserver = if (Build.VERSION.SDK_INT > 34) {
                        loadClassOrNull("com.android.systemui.media.controls.ui.binder.SeekBarObserver")
                    } else {
                        loadClassOrNull("com.android.systemui.media.controls.models.player.SeekBarObserver")
                    }
                    val notifUtil = if (Build.VERSION.SDK_INT > 34) {
                        loadClassOrNull("com.miui.systemui.notification.MiuiBaseNotifUtil")
                    } else {
                        loadClassOrNull("com.android.systemui.statusbar.notification.NotificationUtil")
                    }
                    val playerTwoCircleView = if (Build.VERSION.SDK_INT > 34) {
                        loadClassOrNull("com.miui.systemui.notification.media.PlayerTwoCircleView")
                    } else {
                        loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.PlayerTwoCircleView")
                    }
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
                    val statusBarStateControllerImpl = loadClassOrNull("com.android.systemui.statusbar.StatusBarStateControllerImpl")
                    val miuiStubClass = loadClassOrNull("miui.stub.MiuiStub")
                    val miuiStubInstance = XposedHelpers.getStaticObjectField(miuiStubClass, "INSTANCE")

                    mediaViewHolder?.constructors?.first()?.createAfterHook {
                        val seekBar = it.thisObject.objectHelper().getObjectOrNullAs<SeekBar>("seekBar")

                        val backgroundDrawable = GradientDrawable().apply {
                            color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#20ffffff")))
                            cornerRadius = 9.dp.toFloat()
                        }

                        val onProgressDrawable = GradientDrawable().apply {
                            color = ColorStateList(arrayOf(intArrayOf()), intArrayOf(Color.parseColor("#ffffffff")))
                            cornerRadius = 9.dp.toFloat()
                        }

                        val thumbDrawable = seekBar?.thumb as LayerDrawable
                        val layerDrawable = LayerDrawable(arrayOf(backgroundDrawable, ClipDrawable(onProgressDrawable, Gravity.START, ClipDrawable.HORIZONTAL)))

                        seekBar.apply {
                            thumb = thumbDrawable
                            progressDrawable = layerDrawable
                        }
                    }

                    seekBarObserver?.constructors?.first()?.createAfterHook {
                        it.thisObject.objectHelper().setObject("seekBarEnabledMaxHeight", 9.dp)
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createAfterHook {
                        val context = it.thisObject.objectHelper().getObjectOrNullUntilSuperclassAs<Context>("mContext") ?: return@createAfterHook

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notifUtil, "isBackgroundBlurOpened", context) as Boolean

                        val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@createAfterHook

                        val action0 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action0")
                        val action1 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action1")
                        val action2 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action2")
                        val action3 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action3")
                        val action4 = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageButton>("action4")
                        val titleText = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("titleText")
                        val artistText = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("artistText")
                        val seamlessIcon = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("seamlessIcon")
                        val seekBar = mMediaViewHolder.objectHelper().getObjectOrNullAs<SeekBar>("seekBar")
                        val elapsedTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("elapsedTimeView")
                        val totalTimeView = mMediaViewHolder.objectHelper().getObjectOrNullAs<TextView>("totalTimeView")
                        val albumView = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("albumView")
                        val appIcon = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("appIcon")

                        val artwork = it.args[0].objectHelper().getObjectOrNullAs<Icon>("artwork") ?: return@createAfterHook
                        val artworkLayer = artwork.loadDrawable(context) ?: return@createAfterHook

                        val artworkBitmap = Bitmap.createBitmap(artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(artworkBitmap)
                        artworkLayer.setBounds(0, 0, artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight)
                        artworkLayer.draw(canvas)
                        val minDimen = artworkBitmap.width.coerceAtMost(artworkBitmap.height)
                        val left = (artworkBitmap.width - minDimen) / 2
                        val top = (artworkBitmap.height - minDimen) / 2
                        val rect = Rect(left, top, left + minDimen, top + minDimen)
                        val croppedBitmap = Bitmap.createBitmap(minDimen, minDimen, Bitmap.Config.ARGB_8888)
                        val canvasCropped = Canvas(croppedBitmap)
                        canvasCropped.drawBitmap(artworkBitmap, rect, Rect(0, 0, minDimen, minDimen), null)
                        // 300px & 45f rounded corners are necessary，otherwise the rounded corners are not drawn correctly.
                        val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 300, 300, true)
                        val bitmapNew = Bitmap.createBitmap(resizedBitmap.width, resizedBitmap.height, Bitmap.Config.ARGB_8888)
                        val canvasNew = Canvas(bitmapNew)
                        val paint = Paint()
                        val rectF = RectF(0f, 0f, resizedBitmap.width.toFloat(), resizedBitmap.height.toFloat())
                        paint.isAntiAlias = true
                        canvasNew.drawARGB(0, 0, 0, 0)
                        paint.color = Color.BLACK
                        canvasNew.drawRoundRect(rectF, 45f, 45f, paint)
                        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                        canvasNew.drawBitmap(resizedBitmap, 0f, 0f, paint)
                        albumView?.setImageDrawable(BitmapDrawable(context.resources, bitmapNew))
                        (appIcon?.parent as ViewGroup?)?.removeView(appIcon)

                        val grey = if (isDarkMode(context)) Color.LTGRAY else Color.DKGRAY
                        val color = if (isDarkMode(context)) Color.WHITE else Color.BLACK
                        seekBar?.thumb?.colorFilter = colorFilter(Color.TRANSPARENT)
                        elapsedTimeView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
                        totalTimeView?.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f)
                        if (!isBackgroundBlurOpened) {
                            action0?.setColorFilter(color)
                            action1?.setColorFilter(color)
                            action2?.setColorFilter(color)
                            action3?.setColorFilter(color)
                            action4?.setColorFilter(color)
                            titleText?.setTextColor(Color.WHITE)
                            seamlessIcon?.setColorFilter(Color.WHITE)
                            seekBar?.progressDrawable?.colorFilter = colorFilter(Color.WHITE)
                        } else {
                            artistText?.setTextColor(grey)
                            elapsedTimeView?.setTextColor(grey)
                            totalTimeView?.setTextColor(grey)
                            titleText?.setTextColor(grey)
                            action0?.setColorFilter(color)
                            action1?.setColorFilter(color)
                            action2?.setColorFilter(color)
                            action3?.setColorFilter(color)
                            action4?.setColorFilter(color)
                            titleText?.setTextColor(color)
                            seamlessIcon?.setColorFilter(color)
                            seekBar?.progressDrawable?.colorFilter = colorFilter(color)
                        }
                    }

                    playerTwoCircleView?.constructors?.forEach { constructor ->
                        constructor.createAfterHook {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val mSysUIProvider = XposedHelpers.getObjectField(miuiStubInstance, "mSysUIProvider")
                            val mStatusBarStateController = XposedHelpers.getObjectField(mSysUIProvider, "mStatusBarStateController")
                            val getLazyClass = XposedHelpers.callMethod(mStatusBarStateController, "get")
                            val getState = XposedHelpers.callMethod(getLazyClass, "getState")

                            (it.thisObject as ImageView).setMiViewBlurMode(BACKGROUND)
                            (it.thisObject as ImageView).setBlurRoundRect(getNotificationElementRoundRect(context))
                            (it.thisObject as ImageView).apply {
                                getNotificationElementBlendColors(context, getState == 1)?.let { iArr -> setMiBackgroundBlendColors(iArr, 1f) }
                            }

                            statusBarStateControllerImpl?.methodFinder()?.filterByName("getState")?.first()?.createAfterHook { hookParam1 ->
                                val getStatusBarState = hookParam1.result as Int
                                val isInLockScreen = getStatusBarState == 1
                                val isDarkMode = isDarkMode(context)
                                if (lockScreenStatus == null || darkModeStatus == null || lockScreenStatus != isInLockScreen || darkModeStatus != isDarkMode) {
                                    if (BuildConfig.DEBUG) Log.dx("getStatusBarState: $getStatusBarState")
                                    if (BuildConfig.DEBUG) Log.dx("darkModeStatus: $isDarkMode")
                                    lockScreenStatus = isInLockScreen
                                    darkModeStatus = isDarkMode
                                    (it.thisObject as ImageView).apply {
                                        getNotificationElementBlendColors(context, isInLockScreen)?.let { iArr -> setMiBackgroundBlendColors(iArr, 1f) }
                                    }
                                }
                            }
                        }
                    }

                    playerTwoCircleView?.methodFinder()?.filterByName("onDraw")?.first()?.createBeforeHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notifUtil, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@createBeforeHook

                        val mPaint1 = it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint1")
                        val mPaint2 = it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint2")
                        if (mPaint1?.alpha == 0) return@createBeforeHook

                        if (BuildConfig.DEBUG) Log.dx("PlayerTwoCircleView onDraw called!")

                        mPaint1?.alpha = 0
                        mPaint2?.alpha = 0
                        it.thisObject.objectHelper().setObject("mRadius", 0f)
                    }

                    playerTwoCircleView?.methodFinder()?.filterByName("setBackground")?.first()?.createBeforeHook {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notifUtil, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@createBeforeHook

                        (it.thisObject as ImageView).background = null
                        it.result = null
                    }
                } catch (t: Throwable) {
                    Log.ex(t)
                }

                if (Build.VERSION.SDK_INT == 35) {
                    val graphicsA15 = loadClassOrNull("androidx.palette.graphics.Palette\$Builder\$1")
                    graphicsA15?.methodFinder()?.filterByName("onPostExecute")?.first()?.createBeforeHook {
                        it.result = null
                    }
                }
            }

            else -> return
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getResourceValue(resources: Resources, name: String, type: String, theme: Resources.Theme? = null): Int {
        val id = resources.getIdentifier(name, type, "com.android.systemui")
        return when (type) {
            "color" -> resources.getColor(id, theme)
            "integer" -> resources.getInteger(id)
            else -> throw IllegalArgumentException("Unsupported resource type: $type")
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getNotificationElementBlendColors(context: Context, isInLockScreen: Boolean): IntArray? {
        val resources = context.resources
        val theme = context.theme
        var arrayInt: IntArray? = null
        try {
            if (isInLockScreen) {
                val arrayId = resources.getIdentifier("notification_element_blend_keyguard_colors", "array", "com.android.systemui")
                arrayInt = resources.getIntArray(arrayId)
                if (BuildConfig.DEBUG) Log.dx("Notification element blend keyguard colors found successful [1/3]!")
            } else {
                val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", "com.android.systemui")
                arrayInt = resources.getIntArray(arrayId)
                if (BuildConfig.DEBUG) Log.dx("Notification element blend shade colors found successful [1/3]!")
            }
            return arrayInt
        } catch (_: Exception) {
            if (BuildConfig.DEBUG) Log.dx("Notification element blend colors not found [1/3]!")
        }

        try {
            if (isInLockScreen) {
                val color1 = getResourceValue(resources, "notification_element_blend_keyguard_color_1", "color", theme)
                val color2 = getResourceValue(resources, "notification_element_blend_keyguard_color_2", "color", theme)
                val integer1 = getResourceValue(resources, "notification_element_blend_keyguard_mode_1", "integer")
                val integer2 = getResourceValue(resources, "notification_element_blend_keyguard_mode_2", "integer")
                arrayInt = intArrayOf(color1, integer1, color2, integer2)
                if (BuildConfig.DEBUG) Log.dx("Notification element blend keyguard colors found successful [2/3]!")
            } else {
                val color1 = getResourceValue(resources, "notification_element_blend_shade_color_1", "color", theme)
                val color2 = getResourceValue(resources, "notification_element_blend_shade_color_2", "color", theme)
                val integer1 = getResourceValue(resources, "notification_element_blend_shade_mode_1", "integer")
                val integer2 = getResourceValue(resources, "notification_element_blend_shade_mode_2", "integer")
                arrayInt = intArrayOf(color1, integer1, color2, integer2)
                if (BuildConfig.DEBUG) Log.dx("Notification element blend shade colors found successful [2/3]!")
            }
            return arrayInt
        } catch (_: Exception) {
            if (BuildConfig.DEBUG) Log.dx("Notification element blend colors not found [2/3]!")
        }

        try {
            if (isInLockScreen) {
                val arrayId = resources.getIdentifier("notification_element_keyguard_colors", "array", "com.android.systemui")
                arrayInt = resources.getIntArray(arrayId)
                if (BuildConfig.DEBUG) Log.dx("Notification element keyguard colors found successful [3/3]!")
            } else {
                val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", "com.android.systemui")
                arrayInt = resources.getIntArray(arrayId)
                if (BuildConfig.DEBUG) Log.dx("Notification element blend colors found successful [3/3]!")
            }
            return arrayInt
        } catch (_: Exception) {
            if (BuildConfig.DEBUG) Log.dx("Notification element colors not found [3/3]!")
        }

        Log.ex("Notification element blend colors not found!")
        return arrayInt
    }

    @SuppressLint("DiscouragedApi")
    fun getNotificationElementRoundRect(context: Context): Int {
        val resources = context.resources
        val dimenId = resources.getIdentifier("notification_item_bg_radius", "dimen", "com.android.systemui")
        return resources.getDimensionPixelSize(dimenId)
    }
}
