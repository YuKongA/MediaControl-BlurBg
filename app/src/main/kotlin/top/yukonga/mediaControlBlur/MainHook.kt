package top.yukonga.mediaControlBlur

import android.annotation.SuppressLint
import android.app.AndroidAppHelper
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
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.EzXHelper.moduleRes
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.yukonga.mediaControlBlur.utils.AppUtils.colorFilter
import top.yukonga.mediaControlBlur.utils.AppUtils.isDarkMode
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setBlurRoundRect
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiBackgroundBlendColors
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiViewBlurMode
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.supportBackgroundBlur

private const val TAG = "MediaControlBlur"
private const val ALPHA = 1f

class MainHook : IXposedHookLoadPackage, IXposedHookZygoteInit {

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelper.initZygote(startupParam)
    }

    @SuppressLint("DiscouragedApi")
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag(TAG)
        EzXHelper.setToastTag(TAG)
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                try {
                    if (!supportBackgroundBlur()) return

                    val mediaControlPanel = loadClassOrNull("com.android.systemui.media.controls.ui.MediaControlPanel")
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
                    val notificationUtil = loadClassOrNull("com.android.systemui.statusbar.notification.NotificationUtil")
                    val playerTwoCircleView = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.PlayerTwoCircleView")

                    mediaControlPanel?.methodFinder()?.filterByName("attachPlayer")?.first()?.createHook {
                        after {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                            if (!isBackgroundBlurOpened) return@after

                            val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@after
                            val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@after

                            val resources = context.resources
                            val intArray = try {
                                val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", lpparam.packageName)
                                resources.getIntArray(arrayId)
                            } catch (_: Exception) {
                                val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", lpparam.packageName)
                                resources.getIntArray(arrayId)
                            } catch (e: Exception) {
                                Log.ex("notification element blend colors not found!")
                                return@after
                            }

                            val dimenId = resources.getIdentifier("notification_item_bg_radius", "dimen", lpparam.packageName)
                            val radius = resources.getDimensionPixelSize(dimenId)

                            mediaBg.apply {
                                setMiViewBlurMode(1)
                                setBlurRoundRect(radius)
                                setMiBackgroundBlendColors(intArray, ALPHA)
                            }
                        }
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createHook {
                        after {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean

                            val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@after

                            val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@after
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

                            val grey = if (isDarkMode(context)) {
                                moduleRes.getColor(R.color.mediacontrol_artist_text_color_dark, null)
                            } else {
                                moduleRes.getColor(R.color.mediacontrol_artist_text_color_light, null)
                            }

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
                                    seekBar?.progressDrawable?.colorFilter = colorFilter(Color.BLACK)
                                    seekBar?.thumb?.colorFilter = colorFilter(Color.BLACK)
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
                                    seekBar?.progressDrawable?.colorFilter = colorFilter(Color.WHITE)
                                    seekBar?.thumb?.colorFilter = colorFilter(Color.WHITE)
                                    elapsedTimeView?.setTextColor(grey)
                                    totalTimeView?.setTextColor(grey)
                                }

                                val resources = context.resources
                                val intArray = try {
                                    val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", lpparam.packageName)
                                    resources.getIntArray(arrayId)
                                } catch (_: Exception) {
                                    val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", lpparam.packageName)
                                    resources.getIntArray(arrayId)
                                } catch (e: Exception) {
                                    Log.ex("notification element blend colors not found!")
                                    return@after
                                }
                                mediaBg.setMiBackgroundBlendColors(intArray, ALPHA)

                                val artwork = it.args[0].objectHelper().getObjectOrNullAs<Icon>("artwork") ?: return@after
                                val artworkLayer = artwork.loadDrawable(context) ?: return@after
                                val artworkBitmap = Bitmap.createBitmap(artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(artworkBitmap)
                                artworkLayer.setBounds(0, 0, artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight)
                                artworkLayer.draw(canvas)

                                val radius = 45f
                                val output = Bitmap.createBitmap(artworkBitmap.width, artworkBitmap.height, Bitmap.Config.ARGB_8888)
                                val canvas1 = Canvas(output)

                                val paint = Paint()
                                val rect = Rect(0, 0, artworkBitmap.width, artworkBitmap.height)
                                val rectF = RectF(rect)

                                paint.isAntiAlias = true
                                canvas1.drawARGB(0, 0, 0, 0)
                                paint.color = Color.BLACK
                                canvas1.drawRoundRect(rectF, radius, radius, paint)

                                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                                canvas1.drawBitmap(artworkBitmap, rect, rect, paint)

                                albumView?.setImageDrawable(BitmapDrawable(context.resources, output))

                                appIcon?.parent?.let { viewParent ->
                                    (viewParent as ViewGroup).removeView(appIcon)
                                }
                            }
                        }
                    }

                    // 移除 MediaControlPanel 绘制
                    playerTwoCircleView?.methodFinder()?.filterByName("onDraw")?.first()?.createHook {
                        before {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                            if (!isBackgroundBlurOpened) return@before

                            it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint1")?.alpha = 0
                            it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint2")?.alpha = 0
                            it.thisObject.objectHelper().setObject("mRadius", 0f)

                            it.result = null
                        }
                    }

                    // 移除 MediaControlPanel 背景
                    playerTwoCircleView?.methodFinder()?.filterByName("setBackground")?.first()?.createHook {
                        before {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                            if (!isBackgroundBlurOpened) return@before

                            val mediaBg = it.thisObject as ImageView
                            mediaBg.background = null

                            it.result = null
                        }
                    }

                    // 移除 MediaControlPanel 混色
                    playerTwoCircleView?.methodFinder()?.filterByName("setPaintColor")?.first()?.createHook {
                        before {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtil, "isBackgroundBlurOpened", context) as Boolean
                            if (!isBackgroundBlurOpened) return@before

                            it.result = null
                        }
                    }


                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }

}