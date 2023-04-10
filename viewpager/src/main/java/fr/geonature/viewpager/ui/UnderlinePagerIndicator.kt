package fr.geonature.viewpager.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import fr.geonature.viewpager.R

/**
 * Draws a line for each page.
 * The current page line is colored differently than the unselected page lines.
 *
 * @author S. Grimault
 */
class UnderlinePagerIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.underlinePageIndicatorStyle
) : View(
    context,
    attrs,
    defStyleAttr
), IPagerIndicator {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var viewPager: ViewPager2? = null

    private var currentPage = 0
    private var positionOffset = 0f
    private var scrollState = 0
    private var selectedColor = 0

    init {
        init(
            attrs,
            defStyleAttr
        )
    }

    override fun setViewPager(viewPager: ViewPager2) {
        if (this.viewPager === viewPager) {
            return
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(
                    position,
                    positionOffset,
                    positionOffsetPixels
                )

                currentPage = position
                this@UnderlinePagerIndicator.positionOffset = positionOffset
                invalidate()
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (scrollState == ViewPager2.SCROLL_STATE_IDLE) {
                    currentPage = position
                    positionOffset = 0f
                    invalidate()
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                scrollState = state
            }
        })

        if (viewPager.adapter == null) {
            throw IllegalStateException("ViewPager does not have adapter instance.")
        }

        this.viewPager = viewPager
        invalidate()
    }

    override fun setViewPager(
        viewPager: ViewPager2,
        initialPosition: Int
    ) {
        setViewPager(viewPager)
        setCurrentItem(initialPosition)
    }

    override fun setCurrentItem(item: Int) {
        val viewPager = viewPager
            ?: return

        viewPager.currentItem = item
        currentPage = item
        invalidate()
    }

    override fun notifyDataSetChanged() {
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
            ?: return null

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

        val viewPager = viewPager
            ?: return

        val count = viewPager.adapter?.itemCount
            ?: 0

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

        canvas.drawRect(
            left,
            top,
            right,
            bottom,
            paint
        )
    }

    private fun init(
        attrs: AttributeSet?,
        defStyle: Int
    ) {
        if (isInEditMode) {
            return
        }

        // retrieve styles attributes
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.UnderlinePagerIndicator,
            defStyle,
            0
        )

        selectedColor = typedArray.getColor(
            R.styleable.UnderlinePagerIndicator_selectedColor,
            Color.BLUE
        )

        typedArray.recycle()
    }

    internal class SavedState : BaseSavedState {
        var currentPage: Int = 0

        constructor(superState: Parcelable) : super(superState)

        private constructor(source: Parcel) : super(source) {
            currentPage = source.readInt()
        }

        override fun writeToParcel(
            dest: Parcel,
            flags: Int
        ) {
            super.writeToParcel(
                dest,
                flags
            )

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
