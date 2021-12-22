package com.pyra.krpytapplication.view.activity

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.MediaController
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.utils.show
import kotlinx.android.synthetic.main.activity_view.*
import kotlin.math.sqrt

class ImageAndVideoViewer : BaseActivity() {

    var matrix: Matrix = Matrix()
    var savedMatrix: Matrix = Matrix()

    // We can be in one of these 3 states
    val NONE = 0
    val DRAG = 1
    val ZOOM = 2
    var mode = NONE

    // Remember some things for zooming
    var start = PointF()
    var mid = PointF()
    var oldDist = 1f
    var savedItemClicked: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view)

        getIntentValues()

    }

    private fun getIntentValues() {

        intent.extras?.let { bundle ->
            if (bundle.getBoolean(Constants.IntentKeys.ISVIDEO)) {
                loadVideo(bundle.getString(Constants.IntentKeys.CONTENT))
            } else {
                loadImage(bundle.getString(Constants.IntentKeys.CONTENT))
            }
        }
    }

    private fun loadVideo(url: String?) {
        videoContainer.show()
        imageContainer.hide()

        videoContainer.setVideoPath(url)

        videoContainer.start()

        videoContainer.setMediaController(MediaController(this))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun loadImage(url: String?) {

        videoContainer.hide()
        imageContainer.show()

        imageContainer.loadImage(url)

        imageContainer.setOnTouchListener { p0, p1 ->
            onTouch(p0, p1)
            true
        }
    }

    fun onBackButtonPressed(view: View) {
        onBackPressed()
    }

    fun onTouch(v: View, event: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        val view: ImageView = v as ImageView
        dumpEvent(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start.set(event.x, event.y)
                mode = DRAG;
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                matrix.set(savedMatrix)
                matrix.postTranslate(
                    event.x - start.x, event.y
                            - start.y
                )
            } else if (mode == ZOOM) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale: Float = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
            }
        }
        view.imageMatrix = matrix
        return true
    }

    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf(
            "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
            "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?"
        )
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN
            || actionCode == MotionEvent.ACTION_POINTER_UP
        ) {
            sb.append("(pid ").append(
                action shr MotionEvent.ACTION_POINTER_ID_SHIFT
            )
            sb.append(")")
        }
        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }
        sb.append("]")
    }

    /** Determine the space between the first two fingers  */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    /** Calculate the mid point of the first two fingers  */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }
}