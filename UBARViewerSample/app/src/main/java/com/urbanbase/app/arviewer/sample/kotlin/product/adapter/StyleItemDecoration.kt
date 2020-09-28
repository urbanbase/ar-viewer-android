package com.urbanbase.app.arviewer.sample.kotlin.product.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class StyleItemDecoration(val horizontalOffset: Int, val padding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (position == 0) {
            // 처음 아이템 인 경우 위쪽 패딩 값 적용
            outRect.top = padding
        } else {
            outRect.top = padding
            outRect.bottom = padding
        }

        outRect.left = horizontalOffset
        outRect.right = horizontalOffset
    }
}