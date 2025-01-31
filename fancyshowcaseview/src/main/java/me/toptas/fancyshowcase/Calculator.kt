/*
 * Copyright (c) 2018. Faruk Toptaş
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.toptas.fancyshowcase

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout

/**
 * Geometric calculations for position, size and radius
 */

class Calculator(activity: Activity,
                 focusShape: FocusShape,
                 views: List<View>,
                 radiusFactor: Double,
                 fitSystemWindows: Boolean) {

    /**
     * @return Width of background bitmap
     */
    val bitmapWidth: Int
    /**
     * @return Height of background bitmap
     */
    val bitmapHeight: Int
    /**
     * @return Shape of focus
     */
    var focusShape: FocusShape? = null
        private set
    /**
     * @return Focus width
     */
    var focusWidth: Int = 0
        private set
    /**
     * @return Focus height
     */
    var focusHeight: Int = 0
        private set
    /**
     * @return X coordinate of focus circle
     */
    var circleCenterX: Int = 0
        private set
    /**
     * @return Y coordinate of focus circle
     */
    var circleCenterY: Int = 0
        private set
    /**
     * @return Radius of focus circle
     */
    var viewRadius: Int = 0
        private set
    private var mHasFocus: Boolean = false
    private var windowFlags: Int = activity.window.attributes.flags


    init {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        val deviceHeight = displayMetrics.heightPixels
        bitmapWidth = deviceWidth
        bitmapHeight = deviceHeight - if (fitSystemWindows) 0 else getStatusBarHeight(activity)
        val shouldAdjustYPosition = (fitSystemWindows && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                || (isFullScreen() && !fitSystemWindows))
        if (views.isNotEmpty()) {
            val adjustHeight = if (shouldAdjustYPosition)
                0
            else
                getStatusBarHeight(activity)
            val viewPoint = getPoint(views)
            val size = getSize(views)

            focusWidth = size?.width ?: 0
            focusHeight = size?.height ?: 0
            this.focusShape = focusShape
            circleCenterX = (viewPoint?.x ?: 0) + focusWidth / 2
            circleCenterY = (viewPoint?.y ?: 0) + focusHeight / 2 - adjustHeight
            viewRadius = ((Math.hypot(focusWidth.toDouble(), focusHeight.toDouble()) / 2).toInt() * radiusFactor).toInt()
            mHasFocus = true
        } else {
            mHasFocus = false
        }
    }

    /**
     * Setting round rectangle focus at specific position
     *
     * @param positionX       focus at specific position Y coordinate
     * @param positionY       focus at specific position circle radius
     * @param rectWidth   focus at specific position rectangle width
     * @param rectHeight  focus at specific position rectangle height
     */

    fun setRectPosition(positionX: Int, positionY: Int, rectWidth: Int, rectHeight: Int) {
        circleCenterX = positionX
        circleCenterY = positionY
        focusWidth = rectWidth
        focusHeight = rectHeight
        focusShape = FocusShape.ROUNDED_RECTANGLE
        mHasFocus = true
    }

    /**
     * Setting circle focus at specific position
     *
     * @param positionX       focus at specific position Y coordinate
     * @param positionY       focus at specific position circle radius
     * @param radius          focus at specific position circle radius
     */

    fun setCirclePosition(positionX: Int, positionY: Int, radius: Int) {
        circleCenterX = positionX
        viewRadius = radius
        circleCenterY = positionY
        focusShape = FocusShape.CIRCLE
        mHasFocus = true
    }

    fun calcAutoTextPosition(view: View) {
        val top = roundRectTop(0, 0.0)
        val bottom = roundRectBottom(0, 0.0)

        val spaceAbove = top.toInt()
        val spaceBelow = bitmapHeight - bottom.toInt()
        val params = view.layoutParams as RelativeLayout.LayoutParams

        if (spaceAbove > spaceBelow) {
            params.bottomMargin = bitmapHeight - (circleCenterY + viewRadius)
            params.topMargin = 0
            params.height = top.toInt()
        } else {
            params.topMargin = circleCenterY + viewRadius
            params.bottomMargin = 0
            params.height = (bitmapHeight - top).toInt()
        }
        view.layoutParams = params
    }

    /**
     * @return True if there is a view to focus
     */
    fun hasFocus(): Boolean {
        return mHasFocus
    }

    /**
     * @param animCounter    Counter for circle animation
     * @param animMoveFactor Move factor for circle animation (Bigger value makes bigger animation)
     * @return Radius of animating circle
     */
    fun circleRadius(animCounter: Int, animMoveFactor: Double): Float {
        return (viewRadius + animCounter * animMoveFactor).toFloat()
    }


    /**
     * @param animCounter    Counter for round rect animation
     * @param animMoveFactor Move factor for round rect animation (Bigger value makes bigger animation)
     * @return Bottom position of round rect
     */
    fun roundRectLeft(animCounter: Int, animMoveFactor: Double): Float {
        return (circleCenterX.toDouble() - (focusWidth / 2).toDouble() - animCounter * animMoveFactor).toFloat()
    }

    /**
     * @param animCounter    Counter for round rect animation
     * @param animMoveFactor Move factor for round rect animation (Bigger value makes bigger animation)
     * @return Top position of focus round rect
     */
    fun roundRectTop(animCounter: Int, animMoveFactor: Double): Float {
        return (circleCenterY.toDouble() - (focusHeight / 2).toDouble() - animCounter * animMoveFactor).toFloat()
    }

    /**
     * @param animCounter    Counter for round rect animation
     * @param animMoveFactor Move factor for round rect animation (Bigger value makes bigger animation)
     * @return Bottom position of round rect
     */
    fun roundRectRight(animCounter: Int, animMoveFactor: Double): Float {
        return (circleCenterX.toDouble() + (focusWidth / 2).toDouble() + animCounter * animMoveFactor).toFloat()
    }

    /**
     * @param animCounter    Counter for round rect animation
     * @param animMoveFactor Move factor for round rect animation (Bigger value makes bigger animation)
     * @return Bottom position of round rect
     */
    fun roundRectBottom(animCounter: Int, animMoveFactor: Double): Float {
        return (circleCenterY.toDouble() + (focusHeight / 2).toDouble() + animCounter * animMoveFactor).toFloat()
    }

    /**
     * @param animCounter    Counter for round rect animation
     * @param animMoveFactor Move factor for round rect animation (Bigger value makes bigger animation)
     * @return Radius of focus round rect
     */
    fun roundRectLeftCircleRadius(animCounter: Int, animMoveFactor: Double): Float {
        return (focusHeight / 2 + animCounter * animMoveFactor).toFloat()
    }

    private fun isFullScreen(): Boolean =
            (windowFlags and WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0

    private fun getPoint(views: List<View>): Point? {
        val viewPoint = IntArray(2)

        var minX: Int? = null
        var minY: Int? = null

        views.forEach { view ->
            view.getLocationInWindow(viewPoint)

            if (minX == null || minX!! > viewPoint[0]) {
                minX = viewPoint[0]
            }

            if (minY == null || minY!! > viewPoint[1]) {
                minY = viewPoint[1]
            }
        }

        return if (minX != null && minY != null) Point(minX!!, minY!!) else null
    }

    private fun getSize(views: List<View>): Size? {
        val viewPoint = IntArray(2)

        var minLeft: Int? = null
        var maxRight: Int? = null

        var minTop: Int? = null
        var maxBottom: Int? = null

        views.forEach { view ->
            view.getLocationInWindow(viewPoint)

            if (minLeft == null || minLeft!! > viewPoint[0]) {
                minLeft = viewPoint[0]
            }

            val right = viewPoint[0] + view.width
            if (maxRight == null || maxRight!! < right) {
                maxRight = right
            }

            val top = viewPoint[1]
            if (minTop  == null || minTop!! > top) {
                minTop = top
            }

            val bottom = viewPoint[1] + view.height
            if (maxBottom == null || maxBottom!! < bottom) {
                maxBottom = bottom
            }
        }

        return if (minLeft == null || maxRight == null || minTop == null || maxBottom == null) null
         else Size(maxRight!! - minLeft!!, maxBottom!! - minTop!!)
    }

    private data class Size(val width: Int, val height: Int)

    companion object {
        fun getStatusBarHeight(context: Context): Int {
            var result = 0
            val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = context.resources.getDimensionPixelSize(resourceId)
            }
            return result
        }
    }
}
