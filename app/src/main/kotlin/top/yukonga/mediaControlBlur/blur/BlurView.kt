package top.yukonga.mediaControlBlur.blur

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import top.yukonga.mediaControlBlur.blur.MiBlurUtilities.mSupportedMiBlur

@SuppressLint("ViewConstructor")
class BlurView(context: Context, private val radius: Int) : View(context) {

    private fun setBlur() {
        MiBlurUtilities.clearMiBackgroundBlendColor(this.parent as View)
        MiBlurUtilities.setPassWindowBlurEnabled(this.parent as View, true)
        MiBlurUtilities.setMiViewBlurMode(this.parent as View, 3)
        MiBlurUtilities.setBlurRoundRect(this.parent as View, radius)

        MiBlurUtilities.addMiBackgroundBlendColor(this.parent as View, 0x2f000000, 3)
        //MiBlurUtilities.addMiBackgroundBlendColor(this.parent as View, if (isDarkMode(this.context)) 0x3f000000 else 0x3fffffff, 3)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (mSupportedMiBlur) {
            setBlur()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        MiBlurUtilities.clearAllBlur(this.parent as View)
    }
}