package top.yukonga.mediaControlBlur.utils.blur

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

object MiBlurUtils {
    const val BACKGROUND = 1
    const val FOREGROUND = 2

    private val setBackgroundBlur by lazy {
        View::class.java.getDeclaredMethod("setBackgroundBlur", Integer.TYPE, FloatArray::class.java, Array<IntArray>::class.java)
    }

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

    fun View.setBackgroundBlur(blurRadius: Int, cornerRadius: FloatArray, blendModes: Array<IntArray>) {
        setBackgroundBlur.invoke(this, blurRadius, cornerRadius, blendModes)
    }

    fun View.setMiBackgroundBlurMode(mode: Int) {
        setMiBackgroundBlurMode.invoke(this, mode)
    }

    fun View.setMiViewBlurMode(mode: Int) {
        setMiViewBlurMode.invoke(this, mode)
    }

    fun View.setMiBackgroundBlurRadius(blurRadios: Int) {
        val radius = when {
            blurRadios < 0 -> 0
            blurRadios > 500 -> 500
            else -> blurRadios
        }
        setMiBackgroundBlurRadius.invoke(this, radius)
    }

    fun View.setPassWindowBlurEnabled(enabled: Boolean) {
        setPassWindowBlurEnabled.invoke(this, enabled)
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

    fun View.setBackgroundBlurScaleRatio(blurScaleRadio: Float) {
        setMiBackgroundBlurScaleRatio.invoke(this, blurScaleRadio)
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
