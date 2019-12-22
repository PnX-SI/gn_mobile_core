package fr.geonature.viewpager.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * This is a custom [ViewPager] implementation allowing disabling paging / swiping controls.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class EnablePagingViewPager(
    context: Context,
    attrs: AttributeSet
) : ViewPager(
    context,
    attrs
) {

    private var pagingEnabled = false
    private var pagingPreviousEnabled = false
    private var pagingNextEnabled = false
    private var lastX = 0f

    init {
        this.pagingEnabled = true
        this.pagingPreviousEnabled = true
        this.pagingNextEnabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return this.pagingEnabled && super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> lastX = ev.x
            MotionEvent.ACTION_MOVE -> {
                if (lastX > ev.x && !this.pagingPreviousEnabled) {
                    return false
                }

                if (lastX < ev.x && !this.pagingNextEnabled) {
                    return false
                }

                lastX = ev.x
            }
        }

        return this.pagingEnabled && super.onTouchEvent(ev)
    }

    fun setPagingEnabled(pPagingEnabled: Boolean) {
        this.pagingEnabled = pPagingEnabled

        if (this.pagingEnabled) {
            this.pagingPreviousEnabled = true
            this.pagingNextEnabled = true
        }
    }

    fun setPagingPreviousEnabled(pPagingPreviousEnabled: Boolean) {
        this.pagingPreviousEnabled = pPagingPreviousEnabled
    }

    fun setPagingNextEnabled(pPagingNextEnabled: Boolean) {
        this.pagingPreviousEnabled = pPagingNextEnabled
    }
}
