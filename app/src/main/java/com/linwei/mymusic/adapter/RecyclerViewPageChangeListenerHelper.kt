package com.linwei.mymusic.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper

class RecyclerViewPageChangeListenerHelper(
    snapHelper: SnapHelper,
    onPageChangeListener: OnPageChangeListener
) : RecyclerView.OnScrollListener() {
    private var snapHelper: SnapHelper = snapHelper
    private var onPageChangeListener: OnPageChangeListener = onPageChangeListener
    private var oldPosition = -1

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        onPageChangeListener.onScrolled(recyclerView, dx, dy)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        var position=0
        val layoutManager=recyclerView.layoutManager
        val view=snapHelper.findSnapView(layoutManager)
        if (view!=null){
            position = layoutManager!!.getPosition(view);
        }
        if (onPageChangeListener!=null){
            onPageChangeListener.onScrollStateChanged(recyclerView, newState)
            if (newState==RecyclerView.SCROLL_STATE_IDLE&&oldPosition!=position){
                oldPosition=position
                onPageChangeListener.onPageSelected(position)
            }
        }
    }

    interface OnPageChangeListener {
        fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int){}
        fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int){}
        fun onPageSelected(position: Int){}
    }
}