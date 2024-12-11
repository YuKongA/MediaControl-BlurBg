package top.yukonga.mediaControlOpt

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
import android.widget.ImageView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createAfterHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag("MediaControlBlur")
        when (lpparam.packageName) {
            "com.android.systemui" -> {
                try {
                    val miuiMediaControlPanel = loadClassOrNull("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")

                    miuiMediaControlPanel?.methodFinder()?.filterByName("bindPlayer")?.first()?.createAfterHook {
                        val context = it.thisObject.objectHelper().getObjectOrNullUntilSuperclassAs<Context>("mContext") ?: return@createAfterHook
                        val mMediaViewHolder = it.thisObject.objectHelper().getObjectOrNullUntilSuperclass("mMediaViewHolder") ?: return@createAfterHook
                        val albumView = mMediaViewHolder.objectHelper().getObjectOrNullAs<ImageView>("albumView")
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
                    }
                } catch (t: Throwable) {
                    Log.ex(t)
                }
            }

            else -> return
        }
    }
}
