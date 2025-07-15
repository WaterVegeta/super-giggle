package com.example.tabsgpttutor.shcedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.HwViewModel
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.shcedule.adapters.CalendarAdapter
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayFragment() : Fragment() {

    companion object {
        fun newInstance(date: LocalDate): DayFragment {
            val fragment = DayFragment()
            fragment.arguments = Bundle().apply {
                putString("date", date.toString())
            }
            return fragment
        }

    }

    private val viewModel: HwViewModel by viewModels()
    lateinit var recyclerView: RecyclerView
    lateinit var rvAdapter: CalendarAdapter
    lateinit var dateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FragmentCreated", "DayFragment")
        val view = inflater.inflate(R.layout.fragment_day, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        dateTextView = view.findViewById<TextView>(R.id.dateTitle)
        val weekDateText = view.findViewById<TextView>(R.id.weekView)
        val constraints: ConstraintLayout = view.findViewById(R.id.constFragmentDay)

        val items = listOf(
            weekDateText,
            dateTextView,
            recyclerView
        )

        val baseDelay = 100L
        val duration = 350L
        val interpolator = OvershootInterpolator()
//        constraints.post {
//            constraints.translationY = 1100f
////            translationX = -360f
//            constraints.scaleX = 0.6f
//            constraints.scaleY = 0.6f
//            constraints.alpha = 0f
//            constraints.pivotY = constraints.height.toFloat()
//            constraints.pivotX = constraints.width / 2f
//            constraints.animate().apply {
//                alpha(1f)
//                scaleX(1f)
//                scaleY(1f)
//                translationY(0f)
////                translationX(0f)
//                setDuration(500)
//                setInterpolator(FastOutSlowInInterpolator())
//            }
//
//        }

         // Gentle pop

        for ((index, item) in items.withIndex()) {
            item.alpha = 0f
            item.scaleX = 0.9f
            item.scaleY = 0.9f

            item.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(baseDelay * index)
                .setDuration(duration)
                .setInterpolator(interpolator)
                .start()
        }




        val date = LocalDate.parse(requireArguments().getString("date"))
        val formatter = DateTimeFormatter.ofPattern("dd MMMM ")
        dateTextView.text = date.format(formatter).toString()

        weekDateText.text = when(date.dayOfWeek){
            DayOfWeek.MONDAY -> getString(R.string.monday)
            DayOfWeek.TUESDAY -> getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY -> getString(R.string.wednesday)
            DayOfWeek.THURSDAY -> getString(R.string.thursday)
            DayOfWeek.FRIDAY -> getString(R.string.friday)
            DayOfWeek.SATURDAY -> getString(R.string.saturday)
            DayOfWeek.SUNDAY -> getString(R.string.sunday)
        }


        getData(date)

        setupRecyclerViewScroll()

        return view
    }

    fun getData(date: LocalDate){
        rvAdapter = CalendarAdapter(
            onItemLongClick = { clickedLesson, position ->
                (parentFragment as ScheduleFrag).nextSubjectDate(
                    clickedLesson.subject,
                    date
                )
            },
            onDone = { clickedLesson, position ->
                (parentFragment as ScheduleFrag).doneHw(
                    clickedLesson.hwId
                )
            }
        )
        viewModel.updateDate(date)
        recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 300
                changeDuration = 500
            }
            setHasFixedSize(true)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scheduleData.collect { data ->
                    rvAdapter.submitList(data)
                }
            }
        }
    }
    private fun setupRecyclerViewScroll() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val fragment = parentFragment as? ScheduleFrag
                if (dy > 0) {
                    // scrolling down
                    fragment?.hideFABs()
                } else if (dy < 0) {
                    // scrolling up
                    fragment?.showFABs()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FragmentDestroyed", "DayFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentDestroyedView", "DayFragment")
    }

    override fun onStart() {
        super.onStart()
        Log.v("start", "Onstart")
    }

    override fun onPause() {
        super.onPause()
        Log.v("pause", "On pause")
    }

    override fun onResume() {
        super.onResume()
        Log.wtf("resume", " on resume")

    }

}