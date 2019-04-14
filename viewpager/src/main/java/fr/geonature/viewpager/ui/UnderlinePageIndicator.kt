package fr.geonature.viewpager.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import fr.geonature.viewpager.R

/**
 * Draws a line for each page.
 * The current page line is colored differently than the unselected page lines.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class UnderlinePageIndicator @JvmOverloads constructor(context: Context,
                                                       attrs: AttributeSet? = null,
                                                       defStyleAttr: Int = R.attr.underlinePageIndicatorStyle) : View(context,
                                                                                                                      attrs,
                                                                                                                      defStyleAttr),
                                                                                                                 IPagerIndicator {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var viewPager: ViewPager? = null

    private var currentPage = 0
    private var positionOffset = 0f
    private var scrollState = 0
    private var selectedColor = 0

    init {
        init(attrs,
             defStyleAttr)
    }

    override fun setViewPager(viewPager: ViewPager) {
        if (this.viewPager === viewPager) {
            return
        }

        this.viewPager?.removeOnPageChangeListener(this)

        if (viewPager.adapter == null) {
            throw IllegalStateException("ViewPager does not have adapter instance.")
        }

        this.viewPager = viewPager
        this.viewPager?.addOnPageChangeListener(this)

        invalidate()
    }

    override fun setViewPager(viewPager: ViewPager,
                              initialPosition: Int) {
        setViewPager(viewPager)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(item: Int) {
        val viewPager = viewPager ?: return

        viewPager.currentItem = item
        currentPage = item
        invalidate()
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    override fun onPageScrolled(position: Int,
                                positionOffset: Float,
                                positionOffsetPixels: Int) {
        currentPage = position
        this.positionOffset = positionOffset
        invalidate()
    }

    override fun onPageSelected(position: Int) {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            currentPage = position
            positionOffset = 0f
            invalidate()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        scrollState = state
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        if (superState == null) return superState

        val savedState = SavedState(superState)
        savedState.currentPage = currentPage

        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        currentPage = savedState.currentPage

        requestLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val viewPager = viewPager ?: return

        val count = viewPager.adapter?.count ?: 0

        if (count == 0) {
            return
        }

        if (currentPage >= count) {
            setCurrentItem(count - 1)
            return
        }

        paint.color = selectedColor

        val paddingLeft = paddingLeft
        val pageWidth = (width - paddingLeft - paddingRight) / (1f * count)
        val left = paddingLeft + pageWidth * (currentPage + positionOffset)
        val right = left + pageWidth
        val top = paddingTop.toFloat()
        val bottom = (height - paddingBottom).toFloat()

        canvas.drawRect(left,
                        top,
                        right,
                        bottom,
                        paint)
    }

    private fun init(attrs: AttributeSet?,
                     defStyle: Int) {
        if (isInEditMode) {
            return
        }

        // retrieve styles attributes
        val typedArray = context.obtainStyledAttributes(attrs,
                                                        R.styleable.UnderlinePageIndicator,
                                                        defStyle,
                                                        0)

        selectedColor = typedArray.getColor(R.styleable.UnderlinePageIndicator_selectedColor,
                                            Color.BLUE)

        typedArray.recycle()
    }

    internal class SavedState : BaseSavedState {
        var currentPage: Int = 0

        constructor(superState: Parcelable) : super(superState)

        private constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
        }

        override fun writeToParcel(dest: Parcel,
                                   flags: Int) {
            super.writeToParcel(dest,
                                flags)

            dest.writeInt(currentPage)
        }

        companion object {

            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
