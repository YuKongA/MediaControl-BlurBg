package top.yukonga.mediaControlBlur.utils.blur

import android.graphics.Outline
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import org.lsposed.hiddenapibypass.HiddenApiBypass

object MiBlurUtils {

    fun View.setMiBackgroundBlurMode(i: Int) {
        HiddenApiBypass.invoke(View::class.java, this, "setMiBackgroundBlurMode", i)
    }

    fun View.setMiViewBlurMode(i: Int) {
        HiddenApiBypass.invoke(View::class.java, this, "setMiViewBlurMode", i)
    }

    fun View.setMiBackgroundBlurRadius(i: Int) {
        if (i < 0 || i > 200) {
            Log.e("MiBlurUtils", "setMiBackgroundBlurRadius error radius is " + i + " " + this.javaClass.getName() + " hashcode " + this.hashCode())
            return
        }
        HiddenApiBypass.invoke(View::class.java, this, "setMiBackgroundBlurRadius", i)
    }

    fun View.setPassWindowBlurEnabled(z: Boolean) {
        HiddenApiBypass.invoke(View::class.java, this, "setPassWindowBlurEnabled", z)
    }

    fun View.disableMiBackgroundContainBelow(z: Boolean) {
        HiddenApiBypass.invoke(View::class.java, this, "disableMiBackgroundContainBelow", z)
    }

    fun View.addMiBackgroundBlendColor(i: Int, i2: Int) {
        HiddenApiBypass.invoke(View::class.java, this, "addMiBackgroundBlendColor", i, i2)
    }

    fun View.setMiBackgroundBlendColors(iArr: IntArray, f: Float) {
        var z: Boolean
        this.clearMiBackgroundBlendColor()
        val length = iArr.size / 2
        for (i in 0 until length) {
            val i2 = i * 2
            var i3 = iArr[i2]
            z = f == 1.0f
            if (!z) {
                val i4 = (i3 shr 24) and 255
                i3 = (i3 and ((i4 shl 24).inv())) or (((i4 * f).toInt()) shl 24)
            }
            val i5 = iArr[i2 + 1]
            this.addMiBackgroundBlendColor(i3, i5)
        }
    }

    fun View.clearMiBackgroundBlendColor() {
        HiddenApiBypass.invoke(View::class.java, this, "clearMiBackgroundBlendColor")
    }

    fun View.setBlurRoundRect(i: Int, i2: Int, i3: Int, i4: Int, i5: Int) {
        this.setClipToOutline(false)
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    i2, i3, i4, i5, i.toFloat()
                )
            }
        }
        this.outlineProvider = outlineProvider
    }

    fun View.setBlurRoundRect(i: Int) {
        this.setClipToOutline(true)
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0, 0, view.width, view.height, i.toFloat()
                )
            }
        }
        this.outlineProvider = outlineProvider
    }

    fun View.clearAllBlur() {
        clearMiBackgroundBlendColor()
        setMiBackgroundBlurMode(0)
        setMiViewBlurMode(0)
        setMiBackgroundBlurRadius(0)
        setPassWindowBlurEnabled(false)
    }

}
