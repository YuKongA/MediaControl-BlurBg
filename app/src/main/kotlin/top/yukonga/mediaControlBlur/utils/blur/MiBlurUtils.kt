package top.yukonga.mediaControlBlur.utils.blur

import android.graphics.Outline
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider

object MiBlurUtils {
    const val BACKGROUND = 1
    const val FOREGROUND = 2

    private val setMiViewBlurMode by lazy {
        View::class.java.getDeclaredMethod("setMiViewBlurMode", Integer.TYPE)
    }

    private val setMiBackgroundBlurMode by lazy {
        View::class.java.getDeclaredMethod("setMiBackgroundBlurMode", Integer.TYPE)
    }

    private val setPassWindowBlurEnabled by lazy {
        View::class.java.getDeclaredMethod("setPassWindowBlurEnabled", java.lang.Boolean.TYPE)
    }

    private val setMiBackgroundBlurRadius by lazy {
        View::class.java.getDeclaredMethod("setMiBackgroundBlurRadius", Integer.TYPE)
    }

    private val addMiBackgroundBlendColor by lazy {
        View::class.java.getDeclaredMethod("addMiBackgroundBlendColor", Integer.TYPE, Integer.TYPE)
    }

    private val setMiBackgroundBlurScaleRatio by lazy {
        View::class.java.getDeclaredMethod("setMiBackgroundBlurScaleRatio", java.lang.Float.TYPE)
    }

    private val clearMiBackgroundBlendColor by lazy {
        View::class.java.getDeclaredMethod("clearMiBackgroundBlendColor")
    }

    private val disableMiBackgroundContainBelow by lazy {
        View::class.java.getDeclaredMethod("disableMiBackgroundContainBelow", java.lang.Boolean.TYPE)
    }

    fun View.setMiBackgroundBlurMode(mode: Int) {
        setMiBackgroundBlurMode.invoke(this, mode)
    }

    fun View.setMiViewBlurMode(mode: Int) {
        setMiViewBlurMode.invoke(this, mode)
    }

    fun View.setMiBackgroundBlurRadius(radius: Int) {
        if (radius < 0 || radius > 500) {
            Log.e("MiBlurUtils", "setMiBackgroundBlurRadius error radius is " + radius + " " + this.javaClass.getName() + " hashcode " + this.hashCode())
            return
        }
        setMiBackgroundBlurRadius.invoke(this, radius)
    }

    fun View.setPassWindowBlurEnabled(z: Boolean) {
        setPassWindowBlurEnabled.invoke(this, z)
    }

    fun View.disableMiBackgroundContainBelow(z: Boolean) {
        disableMiBackgroundContainBelow.invoke(this, z)
    }

    fun View.addMiBackgroundBlendColor(i: Int, i2: Int) {
        addMiBackgroundBlendColor(this, i, i2)
    }

    fun View.clearMiBackgroundBlendColor() {
        clearMiBackgroundBlendColor.invoke(this)
    }

    fun View.setBackgroundBlurScaleRatio(ratio: Float) {
        setMiBackgroundBlurScaleRatio.invoke(this, ratio)
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
