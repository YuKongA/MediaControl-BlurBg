package top.yukonga.mediaControlBlur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.HardwareRenderer
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.widget.ImageView
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.EzXHelper
import com.github.kyuubiran.ezxhelper.HookFactory.`-Static`.createHook
import com.github.kyuubiran.ezxhelper.Log
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.github.kyuubiran.ezxhelper.finders.MethodFinder.`-Static`.methodFinder
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage
import kotlin.random.Random


const val TAG = "MediaControlBlur"
private var artwork: Icon? = null

class MainHook : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        EzXHelper.initHandleLoadPackage(lpparam)
        EzXHelper.setLogTag(TAG)
        EzXHelper.setToastTag(TAG)
        when (lpparam.packageName) {

            "com.android.systemui" -> {

                // 大部分代码来自 Hyper Helper (https://github.com/HowieHChen/XiaomiHelper/blob/master/app/src/main/kotlin/dev/lackluster/mihelper/hook/rules/systemui/CustomMusicControl.kt)
                try {
                    val miuiMediaControlPanel = loadClass("com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel")
                    val playerTwoCircleView = loadClass("com.android.systemui.statusbar.notification.mediacontrol.PlayerTwoCircleView")

                    //  获取 Icon
                    miuiMediaControlPanel.methodFinder().filterByName("bindPlayer").first().createHook {
                        before {
                            artwork = (it.args[0].objectHelper().getObjectOrNull("artwork") ?: return@before) as Icon
                        }
                    }

                    // 重写 onDraw
                    playerTwoCircleView.methodFinder().filterByName("onDraw").first().createHook {
                        before {
                            (it.thisObject.objectHelper().getObjectOrNull("mPaint1") as Paint).alpha = 0
                            (it.thisObject.objectHelper().getObjectOrNull("mPaint2") as Paint).alpha = 0
                            it.thisObject.objectHelper().setObject("mRadius", 0.0f)
                        }
                    }

                    // 重写 setBackground
                    playerTwoCircleView.methodFinder().filterByName("setBackground").first().createHook {
                        replace {
                            if (artwork == null) return@replace it

                            // 获取 ImageView
                            val imageView = it.thisObject as ImageView

                            // 获取 Bitmap
                            var artworkLayer = artwork?.loadDrawable(imageView.context) ?: return@replace it
                            val artworkBitmap = Bitmap.createBitmap(artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(artworkBitmap)
                            artworkLayer.setBounds(0, 0, artworkLayer.intrinsicWidth, artworkLayer.intrinsicHeight)
                            artworkLayer.draw(canvas)

                            // 混色处理
                            val backgroundColors = it.args[0] as IntArray
                            canvas.drawColor(0x7F000000 or (backgroundColors[0] and 0x4FFFFFFF))

                            // 缩小图片
                            val tmpBitmap = Bitmap.createBitmap(132, 132, Bitmap.Config.ARGB_8888)
                            val tmpCanvas = Canvas(tmpBitmap)
                            val scale = 132f / artworkBitmap.width
                            val scaleMatrix = Matrix()
                            scaleMatrix.setScale(scale, scale)
                            val paint = Paint()
                            tmpCanvas.drawBitmap(artworkBitmap, scaleMatrix, paint)

                            // 创建混合图
                            val bigBitmap = Bitmap.createBitmap(tmpBitmap.width * 2, tmpBitmap.height * 2, Bitmap.Config.ARGB_8888)
                            val canvas2 = Canvas(bigBitmap)

                            // 随机旋转 90° 的整数倍
                            val matrix = Matrix()
                            val pivotX = tmpBitmap.width / 2f
                            val pivotY = tmpBitmap.height / 2f
                            val rotationAngle = Random.nextInt(4) * 90f

                            // 生成 5 个旋转后的图片
                            matrix.postRotate(rotationAngle, pivotX, pivotY)
                            val rot1 = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.width, tmpBitmap.height, matrix, true)
                            matrix.postRotate(rotationAngle, pivotX, pivotY)
                            val rot2 = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.width, tmpBitmap.height, matrix, true)
                            matrix.postRotate(rotationAngle, pivotX, pivotY)
                            val rot3 = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.width, tmpBitmap.height, matrix, true)
                            matrix.postRotate(rotationAngle, pivotX, pivotY)
                            val rot4 = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.width, tmpBitmap.height, matrix, true)
                            matrix.postRotate(rotationAngle, pivotX, pivotY)
                            val rot5 = Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.width / 2, tmpBitmap.height / 2, matrix, true)

                            // 绘制到混合图上
                            canvas2.drawBitmap(rot1, 0f, 0f, null) // 左上角
                            canvas2.drawBitmap(rot2, tmpBitmap.width.toFloat(), 0f, null) // 右上角
                            canvas2.drawBitmap(rot3, 0f, tmpBitmap.height.toFloat(), null) // 左下角
                            canvas2.drawBitmap(rot4, tmpBitmap.width.toFloat(), tmpBitmap.height.toFloat(), null) // 右下角
                            canvas2.drawBitmap(rot5, tmpBitmap.width / 4f * 3f, tmpBitmap.height / 4f * 3f, null) // 中心

                            // 模糊处理
                            artworkLayer = BitmapDrawable(imageView.resources, bigBitmap.blur(35f))

                            // 绘制到 ImageView 上
                            imageView.setImageDrawable(artworkLayer)
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

private fun Bitmap.blur(radius: Float): Bitmap {

    // 该部分来自 Google (https://developer.android.google.cn/guide/topics/renderscript/migrate)

    val imageReader =
        ImageReader.newInstance(this.width, this.height, PixelFormat.RGBA_8888, 1, HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT)
    val renderNode = RenderNode("BlurEffect")
    val hardwareRenderer = HardwareRenderer()

    hardwareRenderer.setSurface(imageReader.surface)
    hardwareRenderer.setContentRoot(renderNode)
    renderNode.setPosition(0, 0, imageReader.width, imageReader.height)
    val blurRenderEffect = RenderEffect.createBlurEffect(
        radius, radius, Shader.TileMode.MIRROR
    )
    renderNode.setRenderEffect(blurRenderEffect)

    val renderCanvas = renderNode.beginRecording()
    renderCanvas.drawBitmap(this, 0f, 0f, null)
    renderNode.endRecording()
    hardwareRenderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()

    val image = imageReader.acquireNextImage() ?: throw RuntimeException("No Image")
    val hardwareBuffer = image.hardwareBuffer ?: throw RuntimeException("No HardwareBuffer")
    val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null) ?: throw RuntimeException("Create Bitmap Failed")

    hardwareBuffer.close()
    image.close()
    imageReader.close()
    renderNode.discardDisplayList()
    hardwareRenderer.destroy()

    return bitmap
}