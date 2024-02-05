package com.example.android_media_lib.block

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView

// 参考文章：
// https://www.jianshu.com/p/bbc77334be95
// https://blog.csdn.net/allen315410/article/details/45059989
// https://blog.csdn.net/qq_39734865/article/details/132204375

private const val TAG = "BitmapLearning"

fun testPaintFilter(view: ImageView, originalDrawable: Drawable, filterColor: Int = Color.RED) {
    // 将Drawable转换成Bitmap
    val originalBitmap: Bitmap = (originalDrawable as BitmapDrawable).bitmap

    // 创建一个新的Bitmap，用于修改颜色
    val modifiedBitmap: Bitmap =
        Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, originalBitmap.config)

    // 创建一个Canvas对象，用于在新的Bitmap上绘制图像
    val canvas = Canvas(modifiedBitmap)

    // 创建一个Paint对象，用于指定绘制颜色的样式
    val paint = Paint()

//    val colorMatrix = ColorMatrix()
//    colorMatrix.setSaturation(0f) // 设置饱和度为0，将彩色转为黑白
//    val filter = ColorMatrixColorFilter(colorMatrix)
//    paint.colorFilter = filter

    // 修改颜色
    paint.colorFilter = LightingColorFilter(filterColor, 0x000000) // 将白色替换成红色

    // 再次在新的Bitmap上绘制图像，这时白色已经变为红色
    canvas.drawBitmap(originalBitmap, 0f, 0f, paint)

    // 将修改后的Bitmap设置给ImageView显示
    view.setImageBitmap(modifiedBitmap)
}

fun testBitmapDrawText(imageView: ImageView, resources: Resources, src: Int, filterColor: Int = Color.RED) {
    val bitmap = BitmapFactory.decodeResource(resources, src)
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)
    val paint = Paint()

    // paint.colorFilter = LightingColorFilter(filterColor, 0x000000) // 将白色替换成红色
    // canvas.drawBitmap(bitmap, 0f, 0f, paint)

    paint.textSize = 200f
    paint.color = filterColor
    canvas.drawText("Hello, World!", 100f, 100f, paint)

    // 将修改后的Bitmap设置给ImageView显示
    imageView.setImageBitmap(mutableBitmap)
}