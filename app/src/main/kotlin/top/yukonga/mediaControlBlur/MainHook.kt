package top.yukonga.mediaControlBlur

import android.app.AndroidAppHelper
import android.content.Context
import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHooks
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.ConstructorFinder.`-Static`.constructorFinder
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import top.yukonga.mediaControlBlur.blur.BlurView

private const val TAG = "MediaControlBlur"
private var artwork: Icon? = null

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag(TAG)
        EzXHelper.setToastTag(TAG)
        when (lpparam.packageName) {

            "com.android.systemui" -> {

                try {
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")

                    miuiMediaControlPanel?.constructorFinder()?.toList()?.createHooks {
                        after {
                            val context = AndroidAppHelper.currentApplication().applicationContext
                            var mFrameLayout = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mFrameLayout")
                            if (mFrameLayout != null) return@after
                            mFrameLayout = FrameLayout(context)
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "mFrameLayout", mFrameLayout)
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "imageViewTopLeft", ImageView(context))
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "imageViewTopRight", ImageView(context))
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "imageViewBottomLeft", ImageView(context))
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "imageViewBottomRight", ImageView(context))
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "mBlurView", BlurView(context, 60))
                        }
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createHook {
                        after {
                            val mIsArtworkUpdate = it.thisObject.objectHelper().getObjectOrNullAs<Boolean>("mIsArtworkUpdate")

                            if (mIsArtworkUpdate == true) {
                                val context = AndroidAppHelper.currentApplication().applicationContext

                                val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@after
                                val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@after
                                val player = mediaBg.parent as ViewGroup? ?: return@after
                                val playParent = player.parent as ViewGroup? ?: return@after

                                val mFrameLayout = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mFrameLayout") as FrameLayout
                                val mBlurView = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mBlurView") as BlurView

                                artwork = it.args[0].objectHelper().getObjectOrNullAs<Icon>("artwork")
                                val artworkLayer = artwork?.loadDrawable(context) ?: return@after
                                mFrameLayout.background = artworkLayer

                                val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, playParent.height)
                                mFrameLayout.layoutParams = params
                                mBlurView.layoutParams = params

                                if (player.indexOfChild(mFrameLayout) == -1) {
                                    player.addView(mFrameLayout, 0)
                                }

                                if (mFrameLayout.indexOfChild(mBlurView) == -1) {
                                    mFrameLayout.addView(mBlurView, 0)
                                }
                            }
                        }

                        val playerTwoCircleView = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.PlayerTwoCircleView")
                        playerTwoCircleView?.methodFinder()?.filterByName("onDraw")?.first()?.createHook {
                            before {
                                (it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint1"))?.alpha = 0
                                (it.thisObject.objectHelper().getObjectOrNullAs<Paint>("mPaint2"))?.alpha = 0
                                it.thisObject.objectHelper().setObject("mRadius", 0.0f)

                                val mediaBg = it.thisObject as ImageView
                                mediaBg.setAlpha(0f)
                            }
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

fun isDarkMode(context: Context): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES