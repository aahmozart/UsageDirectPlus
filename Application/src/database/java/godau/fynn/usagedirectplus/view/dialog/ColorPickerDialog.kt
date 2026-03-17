package godau.fynn.usagedirectplus.view.dialog

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.palette.graphics.Palette
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.DialogColorPickerBinding
import godau.fynn.usagedirectplus.persistence.AppColor
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor
import godau.fynn.usagedirectplus.thread.icon.IconThread

abstract class ColorPickerDialog(context: Context, timeAppColor: TimeAppColor) : MaterialAlertDialogBuilder(context) {

    init {
        val appColor: AppColor
        if (timeAppColor.appColor == null) {
            appColor = AppColor(
                timeAppColor.applicationId,
                extractDefaultColor(drawableToBitmap(IconThread.iconMap[timeAppColor.applicationId]), context),
                0
            )
        } else {
            // Don't edit existing object which is also used for rendering to support canceling
            appColor = AppColor(timeAppColor.appColor!!)
        }

        val binding = DialogColorPickerBinding.inflate(LayoutInflater.from(getContext()))

        binding.appIcon.setImageDrawable(IconThread.iconMap[appColor.applicationId])

        binding.colorPicker.setColor(appColor.color)

        binding.colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                appColor.color = color
            }
        })

        setView(binding.root)

        setPositiveButton(R.string.confirm) { _, _ ->
            timeAppColor.appColor = appColor
            onColorSet(timeAppColor)
        }

        setNeutralButton(R.string.cancel, null)

        setNegativeButton(R.string.uncolor) { _, _ ->
            timeAppColor.appColor = null
            onColorRemoved()
        }
    }

    protected abstract fun onColorSet(color: TimeAppColor)

    protected abstract fun onColorRemoved()

    companion object {
        @ColorInt
        private fun extractDefaultColor(bitmap: Bitmap, context: Context): Int {
            @ColorInt val color = Palette.from(bitmap)
                .generate()
                .getDominantColor(0xffffffff.toInt())

            if (color == 0xffffffff.toInt()) {
                // Choose different default color at random
                val random = (Math.random() * 4).toInt()
                @ColorRes val res = when (random) {
                    1 -> R.color.notice_gray
                    2 -> R.color.notice_green
                    3 -> R.color.notice_red
                    else -> R.color.notice_blue
                }

                return context.getColor(res)
            } else {
                return color
            }
        }

        /**
         * @see [Androidx implementation](https://android.googlesource.com/platform/frameworks/support/+/android-room-release/core/ktx/src/main/java/androidx/core/graphics/drawable/Drawable.kt#42)
         */
        private fun drawableToBitmap(drawable: Drawable?): Bitmap {
            if (drawable == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(Canvas(bitmap))

            return bitmap
        }
    }
}
