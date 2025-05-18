package com.example.tabsgpttutor.shcedule

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.tabsgpttutor.shcedule.ScheduleFrag
import java.time.LocalDate

class DayPagerAdapter(activity: ScheduleFrag) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2000

    override fun createFragment(position: Int): Fragment {
        val offset = position - 1000
        val date = LocalDate.now().plusDays(offset.toLong())
        return DayFragment.newInstance(date)
    }

    fun getFragmentTag(position: Int): String {
        return "f$position"
    }
}