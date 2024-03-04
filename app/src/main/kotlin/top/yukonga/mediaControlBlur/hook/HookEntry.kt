package top.yukonga.mediaControlBlur.hook

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
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import de.robv.android.xposed.XposedHelpers
import top.yukonga.mediaControlBlur.utils.AppUtils
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setBlurRoundRect
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiBackgroundBlendColors
import top.yukonga.mediaControlBlur.utils.blur.MiBlurUtils.setMiViewBlurMode

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
class HookEntry : IYukiHookXposedInit {

    override fun onInit() = configs {
        debugLog {
            tag = "MediaControlBlur"
            //isEnable = false
        }
        isDebug = false
        //isEnableHookSharedPreferences = true
    }

    @SuppressLint("DiscouragedApi")
    override fun onHook() = encase {
        loadApp(name = "com.android.systemui") {
            val mediaControlPanelClass by lazyClass("$packageName.media.controls.ui.MediaControlPanel")
            val miuiMediaControlPanelClass by lazyClass("$packageName.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
            val notificationUtilClass by lazyClass("$packageName.statusbar.notification.NotificationUtil")
            val playerTwoCircleViewClass by lazyClass("$packageName.statusbar.notification.mediacontrol.PlayerTwoCircleView")

            mediaControlPanelClass.apply {
                method {
                    name = "attachPlayer"
                }.hook {
                    after {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtilClass, "isBackgroundBlurOpened", context) as Boolean
                        if (!isBackgroundBlurOpened) return@after

                        val mMediaViewHolder = this.instance.current().field {
                            name = "mMediaViewHolder"
                            superClass()
                        }.any() ?: return@after
                        val mediaBg = mMediaViewHolder.current(true).field { name = "mediaBg" }.any() as ImageView

                        val resources = context.resources
                        val intArray = try {
                            val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", "com.android.systemui")
                            resources.getIntArray(arrayId)
                        } catch (_: Exception) {
                            val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", "com.android.systemui")
                            resources.getIntArray(arrayId)
                        } catch (_: Exception) {
                            YLog.error("notification element blend colors not found!")
                            return@after
                        }

                        val dimenId = resources.getIdentifier("notification_item_bg_radius", "dimen", "com.android.systemui")
                        val radius = resources.getDimensionPixelSize(dimenId)

                        mediaBg.apply {
                            setMiViewBlurMode(1)
                            setBlurRoundRect(radius)
                            setMiBackgroundBlendColors(intArray, 1f)
                        }
                    }
                }
            }
            miuiMediaControlPanelClass.apply {
                method {
                    name = "bindPlayer"
                }.hook {
                    after {
                        val context = AndroidAppHelper.currentApplication().applicationContext

                        val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtilClass, "isBackgroundBlurOpened", context) as Boolean

                        val mMediaViewHolder = this.instance.current().field {
                            name = "mMediaViewHolder"
                            superClass()
                        }.any() ?: return@after
                        val mediaBg = mMediaViewHolder.current(true).field { name = "mediaBg" }.any() as ImageView
                        val titleText = mMediaViewHolder.current(true).field { name = "titleText" }.any() as TextView
                        val artistText = mMediaViewHolder.current(true).field { name = "artistText" }.any() as TextView
                        val seamlessIcon = mMediaViewHolder.current(true).field { name = "seamlessIcon" }.any() as ImageView
                        val action0 = mMediaViewHolder.current(true).field { name = "action0" }.any() as ImageButton
                        val action1 = mMediaViewHolder.current(true).field { name = "action1" }.any() as ImageButton
                        val action2 = mMediaViewHolder.current(true).field { name = "action2" }.any() as ImageButton
                        val action3 = mMediaViewHolder.current(true).field { name = "action3" }.any() as ImageButton
                        val action4 = mMediaViewHolder.current(true).field { name = "action4" }.any() as ImageButton
                        val seekBar = mMediaViewHolder.current(true).field { name = "seekBar" }.any() as SeekBar
                        val elapsedTimeView = mMediaViewHolder.current(true).field { name = "elapsedTimeView" }.any() as TextView
                        val totalTimeView = mMediaViewHolder.current(true).field { name = "totalTimeView" }.any() as TextView
                        val albumView = mMediaViewHolder.current(true).field { name = "albumView" }.any() as ImageView
                        val appIcon = mMediaViewHolder.current(true).field { name = "appIcon" }.any() as ImageView

                        val grey = if (AppUtils.isDarkMode(context)) Color.parseColor("#80ffffff") else Color.parseColor("#99000000")


                        if (!isBackgroundBlurOpened) {
                            titleText.setTextColor(Color.WHITE)
                            seamlessIcon.setColorFilter(Color.WHITE)
                            action0.setColorFilter(Color.WHITE)
                            action1.setColorFilter(Color.WHITE)
                            action2.setColorFilter(Color.WHITE)
                            action3.setColorFilter(Color.WHITE)
                            action4.setColorFilter(Color.WHITE)
                            seekBar.progressDrawable?.colorFilter = AppUtils.colorFilter(Color.WHITE)
                            seekBar.thumb?.colorFilter = AppUtils.colorFilter(Color.WHITE)
                        } else {
                            if (!AppUtils.isDarkMode(context)) {
                                titleText.setTextColor(Color.BLACK)
                                artistText.setTextColor(grey)
                                seamlessIcon.setColorFilter(Color.BLACK)
                                action0.setColorFilter(Color.BLACK)
                                action1.setColorFilter(Color.BLACK)
                                action2.setColorFilter(Color.BLACK)
                                action3.setColorFilter(Color.BLACK)
                                action4.setColorFilter(Color.BLACK)
                                seekBar.progressDrawable?.colorFilter = AppUtils.colorFilter(Color.BLACK)
                                seekBar.thumb?.colorFilter = AppUtils.colorFilter(Color.BLACK)
                                elapsedTimeView.setTextColor(grey)
                                totalTimeView.setTextColor(grey)
                            } else {
                                titleText.setTextColor(Color.WHITE)
                                artistText.setTextColor(grey)
                                seamlessIcon.setColorFilter(Color.WHITE)
                                action0.setColorFilter(Color.WHITE)
                                action1.setColorFilter(Color.WHITE)
                                action2.setColorFilter(Color.WHITE)
                                action3.setColorFilter(Color.WHITE)
                                action4.setColorFilter(Color.WHITE)
                                seekBar.progressDrawable?.colorFilter = AppUtils.colorFilter(Color.WHITE)
                                seekBar.thumb?.colorFilter = AppUtils.colorFilter(Color.WHITE)
                                elapsedTimeView.setTextColor(grey)
                                totalTimeView.setTextColor(grey)
                            }
                        }

                        val resources = context.resources
                        val intArray = try {
                            val arrayId = resources.getIdentifier("notification_element_blend_shade_colors", "array", "com.android.systemui")
                            resources.getIntArray(arrayId)
                        } catch (_: Exception) {
                            val arrayId = resources.getIdentifier("notification_element_blend_colors", "array", "com.android.systemui")
                            resources.getIntArray(arrayId)
                        } catch (_: Exception) {
                            YLog.error("notification element blend colors not found!")
                            return@after
                        }
                        mediaBg.setMiBackgroundBlendColors(intArray, 1f)

                        val artwork = this.args(0).any()?.current()?.field { name = "artwork" }?.any() ?: return@after
                        val artworkLayer = (artwork as Icon).loadDrawable(context) ?: return@after
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

                        albumView.setImageDrawable(BitmapDrawable(context.resources, newBitmap))

                        appIcon.parent?.let { viewParent ->
                            (viewParent as ViewGroup).removeView(appIcon)
                        }
                    }
                }

                // 移除 MediaControlPanel 绘制
                playerTwoCircleViewClass.apply {
                    method {
                        name = "onDraw"
                    }.hook {
                        before {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtilClass, "isBackgroundBlurOpened", context) as Boolean
                            if (!isBackgroundBlurOpened) return@before

                            (this.instance.current().field { name = "mPaint1" }.any() as Paint).alpha = 0
                            (this.instance.current().field { name = "mPaint2" }.any() as Paint).alpha = 0
                            this.instance.current().field { name = "mRadius" }.set(0f)

                            result = null
                        }
                        method {
                            name = "setBackground"
                        }.hook {
                            before {
                                val context = AndroidAppHelper.currentApplication().applicationContext

                                val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtilClass, "isBackgroundBlurOpened", context) as Boolean
                                if (!isBackgroundBlurOpened) return@before

                                result = null
                            }
                        }
                        method {
                            name = "setPaintColor"
                        }.hook {
                            before {
                                val context = AndroidAppHelper.currentApplication().applicationContext

                                val isBackgroundBlurOpened = XposedHelpers.callStaticMethod(notificationUtilClass, "isBackgroundBlurOpened", context) as Boolean
                                if (!isBackgroundBlurOpened) return@before

                                result = null
                            }
                        }
                    }
                }
            }
        }
    }
}