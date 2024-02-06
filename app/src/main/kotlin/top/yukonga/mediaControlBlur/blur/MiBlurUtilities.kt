package top.yukonga.mediaControlBlur.blur

import android.graphics.Outline
import android.util.Log
import android.view.View
import android.view.ViewOutlineProvider
import org.lsposed.hiddenapibypass.HiddenApiBypass

object MiBlurUtilities {
    var mSupportedMiBlur: Boolean = true

    fun setMiBackgroundBlurMode(view: View?, i: Int) {
        HiddenApiBypass.invoke(View::class.java, view, "setMiBackgroundBlurMode", i)
    }

    fun setMiViewBlurMode(view: View, i: Int) {
        HiddenApiBypass.invoke(View::class.java, view, "setMiViewBlurMode", i)
    }

    fun setMiBackgroundBlurRadius(view: View, i: Int) {
        if (i < 0 || i > 200) {
            Log.e("MiBlurUtilities", "setMiBackgroundBlurRadius error radius is " + i + " " + view.javaClass.getName() + " hashcode " + view.hashCode())
            return
        }
        HiddenApiBypass.invoke(View::class.java, view, "setMiBackgroundBlurRadius", i)
    }

    fun setPassWindowBlurEnabled(view: View, z: Boolean) {
        Log.d("MiBlurUtilities", "setViewBlur:  view $view")
        HiddenApiBypass.invoke(View::class.java, view, "setPassWindowBlurEnabled", z)
    }

    fun disableMiBackgroundContainBelow(view: View, z: Boolean) {
        HiddenApiBypass.invoke(View::class.java, view, "disableMiBackgroundContainBelow", z)
    }

    fun addMiBackgroundBlendColor(view: View, i: Int, i2: Int) {
        HiddenApiBypass.invoke(View::class.java, view, "addMiBackgroundBlendColor", i, i2)
    }

    fun clearMiBackgroundBlendColor(view: View) {
        HiddenApiBypass.invoke(View::class.java, view, "clearMiBackgroundBlendColor")
    }

    fun setBlurRoundRect(view: View, i: Int, i2: Int, i3: Int, i4: Int, i5: Int) {
        view.setClipToOutline(false)
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    i2, i3, i4, i5, i.toFloat()
                )
            }
        }
        view.outlineProvider = outlineProvider
    }

    fun setBlurRoundRect(view: View, i: Int) {
        view.setClipToOutline(true)
        val outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(
                    0, 0, view.width, view.height, i.toFloat()
                )
            }
        }
        view.outlineProvider = outlineProvider
    }


    fun clearAllBlur(view: View) {
        clearMiBackgroundBlendColor(view)
        setMiBackgroundBlurMode(view, 0)
        setMiViewBlurMode(view, 0)
        setMiBackgroundBlurRadius(view, 0)
        setPassWindowBlurEnabled(view, false)
    }

}
