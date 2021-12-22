package com.pyra.krpytapplication.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener

class DrawerImageView : androidx.appcompat.widget.AppCompatImageView, OnTouchListener {
    var downx = 0f
    var downy = 0f
    var upx = 0f
    var upy = 0f
    lateinit var canvas: Canvas
    lateinit var paint: Paint
    lateinit var editImageMatrix: Matrix

    constructor(context: Context) : super(context) {
        setOnTouchListener(this)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        setOnTouchListener(this)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        setOnTouchListener(this)
    }

    fun setNewImage(alteredBitmap: Bitmap, bmp: Bitmap) {
        canvas = Canvas(alteredBitmap)
        paint = Paint()
        paint.color = Color.RED
        paint.strokeWidth = 18f
        editImageMatrix = Matrix()
        canvas.drawBitmap(bmp, editImageMatrix, paint)
        setImageBitmap(alteredBitmap)
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                downx = getPointerCoords(event)[0] //event.getX();
                downy = getPointerCoords(event)[1] //event.getY();
            }
            MotionEvent.ACTION_MOVE -> {
                upx = getPointerCoords(event)[0] //event.getX();
                upy = getPointerCoords(event)[1] //event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint)
                invalidate()
                downx = upx
                downy = upy
            }
            MotionEvent.ACTION_UP -> {
                upx = getPointerCoords(event)[0] //event.getX();
                upy = getPointerCoords(event)[1] //event.getY();
                canvas.drawLine(downx, downy, upx, upy, paint)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
            }
            else -> {
            }
        }
        return true
    }

    private fun getPointerCoords(e: MotionEvent): FloatArray {
        val index = e.actionIndex
        val coords = floatArrayOf(e.getX(index), e.getY(index))
        val matrix = Matrix()
        imageMatrix.invert(matrix)
        matrix.postTranslate(scrollX.toFloat(), scrollY.toFloat())
        matrix.mapPoints(coords)
        return coords
    }
}