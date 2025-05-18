package com.example.tabsgpttutor.shcedule

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.shcedule.CalendarAdapter
import com.example.tabsgpttutor.shcedule.DataClass
import com.example.tabsgpttutor.Homework
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.shcedule.ScheduleFrag
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

class DayFragment(private val homeworkToEdit: Homework? = null) : Fragment() {

    companion object {
        fun newInstance(date: LocalDate): DayFragment {
            val fragment = DayFragment()
            fragment.arguments = Bundle().apply {
                putString("date", date.toString())
            }
            return fragment
        }

    }
    lateinit var realm: Realm
    lateinit var dataList: ArrayList<DataClass>

    private lateinit var lessonList: Array<String>
    private lateinit var timeList: Array<String>
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: CalendarAdapter
    lateinit var dateTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_day, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        dateTextView = view.findViewById<TextView>(R.id.dateTitle)
        val weekDateText = view.findViewById<TextView>(R.id.weekView)

        val items = listOf(
            weekDateText,
            dateTextView
        )

        val baseDelay = 100L
        val duration = 350L
        val interpolator = OvershootInterpolator(1.1f) // Gentle pop

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
        val formatter = DateTimeFormatter.ofPattern("dd MM yyy")
        val formate = date.format(formatter)
        dateTextView.text = formate.toString()

        val dayOfWeek = date.dayOfWeek

        realm = MyDynamic.Companion.realm

        val weekDate = when(dayOfWeek){
            DayOfWeek.MONDAY -> resources.getString(R.string.monday)
            DayOfWeek.TUESDAY -> resources.getString(R.string.tuesday)
            DayOfWeek.WEDNESDAY -> resources.getString(R.string.wednesday)
            DayOfWeek.THURSDAY -> resources.getString(R.string.thursday)
            DayOfWeek.FRIDAY -> resources.getString(R.string.friday)
            DayOfWeek.SATURDAY -> resources.getString(R.string.saturday)
            DayOfWeek.SUNDAY -> resources.getString(R.string.sunday)
        }
        weekDateText.text = weekDate

        val week = ceil(date.dayOfYear / 7.0)

        timeList = resources.getStringArray(R.array.six)

        lessonList = when (dayOfWeek) {
            DayOfWeek.MONDAY -> if (week % 2 == 0.0) resources.getStringArray(R.array.monday1)
            else resources.getStringArray(R.array.monday2)
            DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
            DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
            DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
            DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
            else -> arrayOf("")
        }

        dataList = arrayListOf<DataClass>()
        getData(date, lessonList)

        setupRecyclerViewScroll()

        return view
    }

    fun getData(date: LocalDate, lessons: Array<String>){
        for (i in lessons.indices) {
            val subject = lessons[i]
            val time = timeList[i]
            val allHomework = realm.query<Homework>("lesson == $0 AND date == $1", subject, date.toString()
            ).first().find()
            val homeworks = allHomework?.note
            val homeworksId = allHomework?.id
            val hwDone = allHomework?.done
            Log.d("Homework", "subject: $subject hE: $homeworks heId: $homeworksId")
            dataList.add(DataClass(subject, time, homeworks, homeworksId.toString(), hwDone))
        }
        adapter = CalendarAdapter(
            context = requireContext(),
            dataList,
            onItemLongClick = { clickedLesson, position ->
                (parentFragment as ScheduleFrag).nextSubjectDate(clickedLesson.subject, date, position)
            },
            onDone = { clickedLesson, position ->
                (parentFragment as ScheduleFrag).doneHw(date,
                    clickedLesson.homework.toString(), clickedLesson.subject, position)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        Log.d("DayFragment", "Data List Size: ${dataList.size} and list $dataList")
    }

    fun refreshData(date: LocalDate, subject: String, thatTime: String, position: Int) {
        val allNewHomework = realm.query<Homework>("lesson == $0 AND date == $1", subject, date.toString()
        ).first().find()
        val newHomework = allNewHomework?.note
        val newId = allNewHomework?.id
        val newDone = allNewHomework?.done

        val updatedList = DataClass(
            subject,
            thatTime,
            newHomework,
            newId.toString(),
            newDone
        )
//        val week = ceil(date.dayOfYear / 7.0)
//        lessonList = when (date.dayOfWeek) {
//            DayOfWeek.MONDAY -> if (week % 2 == 0.0) resources.getStringArray(R.array.monday1)
//            else resources.getStringArray(R.array.monday2)
//            DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
//            DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
//            DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
//            DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
//            else -> arrayOf("")
//        }
//        dataList.clear()
//        getData(date, lessonList)
        adapter.updateItem(updatedList, position)
        Log.d("fragRecieved", "date: $date, subject: $subject")
//        Log.d("HOmeworkInfoFrag", "Data refreshing?  $updatedList")

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

}