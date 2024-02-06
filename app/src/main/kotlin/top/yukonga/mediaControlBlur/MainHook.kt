package top.yukonga.mediaControlBlur

import android.app.AndroidAppHelper
import android.content.res.Resources.getSystem
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Icon
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
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
                            var mBlurFrameLayout = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mBlurFrameLayout")
                            if (mBlurFrameLayout != null) return@after
                            mBlurFrameLayout = FrameLayout(context)
                            XposedHelpers.setAdditionalInstanceField(it.thisObject, "mBlurFrameLayout", mBlurFrameLayout)
                        }
                    }

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createHook {
                        after {
                            val context = AndroidAppHelper.currentApplication().applicationContext

                            val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@after
                            val mediaBg = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("mediaBg") ?: return@after
                            val player = mediaBg.parent as ViewGroup? ?: return@after
                            val playParent = player.parent as ViewGroup? ?: return@after

                            val mBlurFrameLayout = XposedHelpers.getAdditionalInstanceField(it.thisObject, "mBlurFrameLayout") as FrameLayout

                            artwork = it.args[0].objectHelper().getObjectOrNullAs<Icon>("artwork")

                            val artworkLayer = artwork?.loadDrawable(context) ?: return@after
                            val artworkBitmap = Bitmap.createBitmap(artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(artworkBitmap)
                            artworkLayer.setBounds(0, 0, artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight)
                            artworkLayer.draw(canvas)

                            val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, artworkBitmap)
                            roundedBitmapDrawable.cornerRadius = 60f
                            mBlurFrameLayout.background = roundedBitmapDrawable

                            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, playParent.height)
                            mBlurFrameLayout.layoutParams = params

                            player.addView(mBlurFrameLayout, 0)
                        }
                    }

//                    val mediaCarouselController = loadClassOrNull("com.android.systemui.media.controls.ui.MediaCarouselController")
//                    mediaCarouselController?.methodFinder()?.filterByName("addOrUpdatePlayer")?.first()?.createHook {
//                        before {
//                            val context = AndroidAppHelper.currentApplication().applicationContext
//                            val mediaContent = it.thisObject.objectHelper().getObjectOrNull("mediaContent") as ViewGroup
//                            val player = mediaContent.getChildAt(0) as ViewGroup
//                            val mediaControlPanelFactory = it.thisObject.objectHelper().getObjectOrNull("mediaControlPanelFactory") ?: return@before
//                            val mMiuiMediaControlPanel = XposedHelpers.callMethod(mediaControlPanelFactory, "get") ?: return@before
//                            val mBlurFrameLayout = XposedHelpers.getAdditionalInstanceField(mMiuiMediaControlPanel, "mBlurFrameLayout") as FrameLayout
//
//                            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, player.height)
//                            mBlurFrameLayout.layoutParams = params
//
//                            //player.addView(mBlurFrameLayout, 0)
//                        }
//                    }

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

                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }

}

val Int.dp: Int get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), getSystem().displayMetrics).toInt()