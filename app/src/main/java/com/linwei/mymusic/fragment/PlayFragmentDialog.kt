package com.linwei.mymusic.fragment

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.linwei.mymusic.R
import com.linwei.mymusic.constant.Constant
import kotlinx.android.synthetic.main.base_play_activity.view.*


class PlayFragmentDialog : BottomSheetDialogFragment() {

    private var bottomSheet: FrameLayout? = null
    lateinit var behavior: BottomSheetBehavior<FrameLayout>
    lateinit var v: View
    private val fragmentList = ArrayList<Fragment>()
    private val tabtitle = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.base_play_activity, null)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.Dialog_FullScreen)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
    }

    private fun initView() {
        v.baseimageBack.setOnClickListener {
            dismiss()
        }
        fragmentList.add(SongPlayFragment(object : SongPlayFragment.setBackgroundListener {
            override fun setbackground(bitmap: Bitmap) {
                super.setbackground(bitmap)
                if (context!=null) v.background = BitmapDrawable(resources, Constant.blurBitmap(context, bitmap, 25f))
            }
        }))
        fragmentList.add(SonglyricFragment())
        tabtitle.add("歌曲")
        tabtitle.add("歌词")
        val linearLayout = v.basetablayout.getChildAt(0) as LinearLayout
        linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        linearLayout.dividerDrawable = resources.getDrawable(R.drawable.ic_divider_vertical)
        linearLayout.dividerPadding = 40
        v.baseViewpager.adapter = object :
            FragmentStatePagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getCount(): Int = fragmentList.size
            override fun getItem(position: Int): Fragment = fragmentList[position]
            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}
            override fun getPageTitle(position: Int): CharSequence? {
                return tabtitle[position]
            }
        }
        v.basetablayout.setupWithViewPager(v.baseViewpager, false)
    }

//

    override fun onStart() {
        super.onStart()
        val dialog = dialog as BottomSheetDialog
        bottomSheet = dialog.delegate.findViewById(R.id.design_bottom_sheet)
        if (bottomSheet != null) {
            val layoutParams = bottomSheet!!.layoutParams
            layoutParams.height = getHeight()
            bottomSheet!!.layoutParams = layoutParams
            behavior = BottomSheetBehavior.from(bottomSheet!!)
            behavior.peekHeight = getHeight()
            // 初始为展开状态
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    protected fun getHeight(): Int {
        val height=resources.displayMetrics.heightPixels
        Log.e("dialog","$height")
        return height
    }

}