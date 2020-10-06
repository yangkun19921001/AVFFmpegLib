package com.devyk.av.ffmpegcmd.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * <pre>
 *     author  : devyk on 2020-09-29 15:24
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is SpacingDecoration
 * </pre>
 */

class SpacingDecoration
/**
 * 间隔单位为dp
 * @param hSpacing 横向间隔（单位为dp）
 * @param vSpacing 纵向间隔（单位为dp）
 * @param includeHEdge 是否保留水平外边间距
 * @param includeVEdge 是否保留垂直外边间距
 */
    (
    context: Context,
    hSpacing: Float,
    vSpacing: Float,
    includeHEdge: Boolean,
    includeVEdge: Boolean,
    hasHeader: Boolean
) :
    RecyclerView.ItemDecoration() {
    private var mHorizontalSpacing = 0
    private var mVerticalSpacing = 0
    private var mIncludeHEdge = false//是否保留水平外边间距
    private var mIncludeVEdge = false//是否保留垂直外边间距
    private var mHasHeader = false//是否含有header

    /**
     * 间隔单位为dp
     * @param hSpacing 横向间隔（单位为dp）
     * @param vSpacing 纵向间隔（单位为dp）
     * @param includeEdge 是否包括边缘
     */
    constructor(context: Context, hSpacing: Float, vSpacing: Float, includeEdge: Boolean) : this(
        context,
        hSpacing,
        vSpacing,
        includeEdge,
        includeEdge,
        false
    ) {
    }

    init {
        mHorizontalSpacing = SizeUtil.dp2px(context, hSpacing)
        mVerticalSpacing = SizeUtil.dp2px(context, vSpacing)
        mIncludeHEdge = includeHEdge
        mIncludeVEdge = includeVEdge
        mHasHeader = hasHeader
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        // Only handle the vertical situation
        var position = parent.getChildAdapterPosition(view)
        //处理有header的情况
        if (mHasHeader) {
            if (position == 0) {
                return //header不处理间距
            } else {
                position = position - 1//减去header占用
            }
        }
        if (parent.layoutManager is GridLayoutManager) {
            val layoutManager = parent.layoutManager as GridLayoutManager?
            val spanCount = layoutManager!!.spanCount
            val column = position % spanCount
            getGridItemOffsets(outRect, position, column, spanCount)
        } else if (parent.layoutManager is StaggeredGridLayoutManager) {
            val layoutManager = parent.layoutManager as StaggeredGridLayoutManager?
            val spanCount = layoutManager!!.spanCount
            val lp = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
            val column = lp.spanIndex
            getGridItemOffsets(outRect, position, column, spanCount)
        } else if (parent.layoutManager is LinearLayoutManager) {
            //水平方向只有一项
            outRect.left = mHorizontalSpacing
            outRect.right = mHorizontalSpacing
            //处理垂直方向间距
            if (mIncludeVEdge) {
                if (position == 0) {
                    outRect.top = mVerticalSpacing
                }
                outRect.bottom = mVerticalSpacing
            } else {
                if (position > 0) {
                    outRect.top = mVerticalSpacing
                }
            }
        }
    }

    private fun getGridItemOffsets(outRect: Rect, position: Int, column: Int, spanCount: Int) {
        //处理水平方向间距
        if (mIncludeHEdge) {
            outRect.left = mHorizontalSpacing * (spanCount - column) / spanCount
            outRect.right = mHorizontalSpacing * (column + 1) / spanCount
        } else {
            outRect.left = mHorizontalSpacing * column / spanCount
            outRect.right = mHorizontalSpacing * (spanCount - 1 - column) / spanCount
        }
        //处理垂直方向间距
        if (mIncludeVEdge) {
            if (position < spanCount) {
                outRect.top = mVerticalSpacing
            }
            outRect.bottom = mVerticalSpacing
        } else {
            if (position >= spanCount) {
                outRect.top = mVerticalSpacing
            }
        }
    }
}
