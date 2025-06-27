package com.example.tabsgpttutor.shcedule

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.CalendarView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.tabsgpttutor.HwViewModel
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.data_base.shedule.Schedule
import com.example.tabsgpttutor.shcedule.adapters.DayPagerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.ceil

class ScheduleFrag: Fragment(R.layout.schedule_frag_layout) {

    private val viewModel: HwViewModel by viewModels()

    lateinit var viewPager: ViewPager2
    val OFFSET = 1000
    lateinit var calendarFAB: FloatingActionButton
    lateinit var deleteFAB: FloatingActionButton
    lateinit var homeFAB: ExtendedFloatingActionButton
    var localDate = LocalDate.now()
    var localTime = LocalTime.now()
    var k = 0
    lateinit var realm: Realm
    var curPosition = 0
    lateinit var currentDay: LocalDate
    lateinit var animShow: Animation
    lateinit var animHide: Animation
    lateinit var firstNoteText: String
//    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//        val manager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
//        manager.defaultVibrator
//    } else {
//        @Suppress("DEPRECATION")
//        requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
//    }
    override fun onAttach(context: Context) {
        super.onAttach(context)

        Log.d("k value", "k = $k local time ${localTime.hour} ${localTime.minute} local date ${localDate.dayOfWeek}")
    }

    @SuppressLint("ClickableViewAccessibility", "ServiceCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = MyDynamic.Companion.realm

        val items = realm.query<Schedule>().find()
        k = 0
        for (i in items){
            if (i.dayOfWeek == localDate.dayOfWeek.toString()){
                i.lessonAndTime.findLast { it.lessonEndHour.isNotEmpty() && it.lessonEndMinute.isNotEmpty() }?.let {
                    val endMin = it.lessonEndMinute
                    val endHour = it.lessonEndHour
                    k = if (localTime.hour.toInt() >= endHour.toInt() && localTime.minute.toInt() >= endMin.toInt()){
                            1
                        } else 0
                }
                break

            }
        }



        viewPager = view.findViewById(R.id.viewPager)
        viewPager.adapter = DayPagerAdapter(this)
        viewPager.setCurrentItem(OFFSET + k, false)
//        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin)
//            val pageOffsetPx = resources.getDimensionPixelOffset(R.dimen.pageOffset)
//            Log.i("Offsetpawd", "$pageOffsetPx")
//
//            viewPager.setPageTransformer { page, position ->
//                when {
//                    position < -1 -> { // [-Infinity,-1)
//                        page.alpha = 0f
//                    }
//                    position <= 1 -> { // [-1,1]
//                        page.alpha = 1f
//
//                        // Counteract the default slide transition
//                        page.translationX = -position * page.width
//
//                        // Set scale to 1 to maintain original size
//                        page.scaleX = 1f
//                        page.scaleY = 1f
//                    }
//                    else -> { // (1,+Infinity]
//                        page.alpha = 0f
//                    }
//                }
//            }
//
//            // Add padding so pages aren't clipped
//            viewPager.setPadding(pageOffsetPx, 0, pageOffsetPx, 0)
//            viewPager.clipToPadding = false
//            viewPager.clipChildren = false
//            viewPager.addItemDecoration(object : RecyclerView.ItemDecoration(){
//                override fun getItemOffsets(
//                    outRect: Rect,
//                    view: View,
//                    parent: RecyclerView,
//                    state: RecyclerView.State
//                ) {
//                    val position = parent.getChildPosition(view)
//                    when(position){
//                        0 -> outRect.left = pageMarginPx / 2
//                        parent.adapter?.itemCount?.minus(1) -> outRect.right = pageMarginPx / 2
//                        else -> {
//                            outRect.left = pageMarginPx / 2
//                            outRect.right = pageMarginPx / 2
//                        }
//                    }
//                }
//            })
//        } else {
//            // For portrait - normal single page behavior
//            viewPager.setPageTransformer(null)
//            viewPager.setPadding(0, 0, 0, 0)
//            viewPager.clipToPadding = true
//            viewPager.clipChildren = true
//        }


        Log.d("FragmentCreated", "ScheduleFragment, k: $k offset: $OFFSET currentItem: ${viewPager.currentItem}")
//        viewPager.offscreenPageLimit = 2

        calendarFAB = view.findViewById(R.id.calendarFAB)
        calendarFAB.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .setInterpolator(DecelerateInterpolator())
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(100)
                        .start()
                }
                MotionEvent.ACTION_UP -> {

                    val rect = Rect()
                    v.getGlobalVisibleRect(rect)
                    if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                        // Finger lifted inside the FAB
                        Log.d("FAB", "Touch Up Inside")
                        v.animate()
                            .setInterpolator(OvershootInterpolator())
                            .setDuration(140)
                            .scaleX(1f)
                            .scaleY(1f)
                            .withEndAction { quickDateChange() }
                            .start()
                    } else {
                        // Finger lifted outside
                        Log.d("FAB", "Touch Up Outside")
                        v.animate()
                            .setInterpolator(OvershootInterpolator())
                            .setDuration(140)
                            .scaleX(1f)
                            .scaleY(1f)
                            .start()
                    }

                }

            }
            true
        }
//        calendarFAB.setOnClickListener {
//            quickDateChange()
//        }
        homeFAB = view.findViewById(R.id.homeFAB)
        homeFAB.setOnClickListener {
            viewPager.setCurrentItem(OFFSET + k, true)
            homeFAB.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
//            val timings = longArrayOf(0, 100, 80, 200, 60, 300) // pause, vibrate, ...
//            val amplitudes = intArrayOf(0, 100, 0, 180, 0, 255) // 0 = pause, 255 = max
//
//            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1) // -1 = no repeat
//            vibrator.vibrate(effect)
            homeFAB.hide()

        }
        homeFAB.hide()
//        if (viewPager.currentItem != OFFSET+k){
//            homeFAB.apply {
//                startAnimation(animShow)
//                show()
//            }
//        }
        curPosition = OFFSET + k

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateHomeButton(position)
                Log.d("ViewPagerChange", "position: $position current: ${viewPager.currentItem}")
                if (calendarFAB.isShown == false){
                    calendarFAB.apply {
                        startAnimation(animShow)
                        show()
                    }

                }



                curPosition = position
            }
        })
        animShow = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_animation)
//        calendarFAB.startAnimation(animShow)

//        calendarFAB.apply {
//            translationY = 300f
//            translationX = -760f
//            scaleX = 0.6f
//            scaleY = 0.6f
//            alpha = 0f
//            animate().apply {
//                scaleX(1f)
//                scaleY(1f)
//                alpha(1f)
//                translationY(0f)
//                translationX(0f)
//                setDuration(300)
//                setInterpolator(FastOutSlowInInterpolator())
//                calendarFAB.startAnimation(animShow)
//            }
//        }


        val animations = viewModel.getAnimations("Schedule")!!
        val layout: CoordinatorLayout = view.findViewById(R.id.scheduleFragLayout)
        if (animations.firstAnim){
            val interp = when(animations.firstInterpolator){
                "AccelerateInterpolator" -> AccelerateInterpolator()
                "AccelerateDecelerateInterpolator" -> AccelerateDecelerateInterpolator()
                "AnticipateInterpolator" -> AnticipateInterpolator()
                "AnticipateOvershootInterpolator" -> AnticipateOvershootInterpolator()
                "BounceInterpolator" -> BounceInterpolator()
                "DecelerateInterpolator" -> DecelerateInterpolator()
                "FastOutSlowInInterpolator" -> FastOutSlowInInterpolator()
                "FastOutLinearInInterpolator" -> FastOutLinearInInterpolator()
                "LinearOutSlowInInterpolator" -> LinearOutSlowInInterpolator()
                "LinearInterpolator" -> LinearInterpolator()
                "OvershootInterpolator" -> OvershootInterpolator()
                else -> LinearInterpolator()
            }

            layout.post {
//            layout.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//            layout.translationY = 300f
                layout.scaleX = animations.firstScaleX
                layout.scaleY = animations.firstScaleY
                layout.alpha = animations.firstAlpha
                layout.pivotY = layout.height.toFloat() * animations.pivotY
                layout.pivotX = layout.width * animations.pivotX
                layout.translationX = animations.firstTranslationX
                layout.translationY = animations.firstTranslationY
                layout.animate().apply {
                    if (animations.secondAnim){
                        alpha(animations.secondAlpha)
                        scaleX(animations.secondScaleX)
                        scaleY(animations.secondScaleY)
                        translationX(animations.secondTranslationX)
                        translationY(animations.secondTranslationY)
                        setDuration(animations.firstDuration)
                        setInterpolator(interp)
                        withEndAction {
                            val interp2 = when(animations.secondInterpolator){
                                "AccelerateInterpolator" -> AccelerateInterpolator()
                                "AccelerateDecelerateInterpolator" -> AccelerateDecelerateInterpolator()
                                "AnticipateInterpolator" -> AnticipateInterpolator()
                                "AnticipateOvershootInterpolator" -> AnticipateOvershootInterpolator()
                                "BounceInterpolator" -> BounceInterpolator()
                                "DecelerateInterpolator" -> DecelerateInterpolator()
                                "FastOutSlowInInterpolator" -> FastOutSlowInInterpolator()
                                "FastOutLinearInInterpolator" -> FastOutLinearInInterpolator()
                                "LinearOutSlowInInterpolator" -> LinearOutSlowInInterpolator()
                                "LinearInterpolator" -> LinearInterpolator()
                                "OvershootInterpolator" -> OvershootInterpolator()
                                else -> LinearInterpolator()
                            }
                            layout.animate().apply {
                                alpha(1f)
                                scaleX(1f)
                                scaleY(1f)
                                translationX(0f)
                                translationY(0f)
                                setDuration(animations.secondDuration)
                                setInterpolator(interp2)
                            }
                        }

                    } else{
                        alpha(1f)
                        scaleX(1f)
                        scaleY(1f)
                        translationX(0f)
                        translationY(0f)
                        setDuration(animations.firstDuration)
                        setInterpolator(interp)

                    }
                }

            }

        }

        animHide = AnimationUtils.loadAnimation(requireContext(), R.anim.fab_anim_hide)





    }

    fun hideFABs() {
        calendarFAB.hide()
        homeFAB.hide()
    }

    fun showFABs() {
        if (calendarFAB.isShown == false ){

            calendarFAB.apply {
                startAnimation(animShow)
                show()
            }
            if (curPosition == OFFSET + k) {
                homeFAB.apply {
                    hide()
                }
            }
            else {
                homeFAB.apply {
                    startAnimation(animShow)
                    show()
                }
            }
        }

    }

    fun quickDateChange() {
        val dialogView = layoutInflater.inflate(R.layout.bsheet_calendar_change, null)
        var dateInput = dialogView.findViewById<CalendarView>(R.id.calendarView)
        val changeButton = dialogView.findViewById<Button>(R.id.changeButton)

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)

        var selectedDate = LocalDate.now().plusDays(k.toLong())
        dateInput.date = selectedDate.plusDays((viewPager.currentItem - OFFSET - k).toLong())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()

        dateInput.setOnDateChangeListener { _, year, month, day ->
            selectedDate = LocalDate.of(year, month + 1, day)
        }

        changeButton.setOnClickListener {
            val today = LocalDate.now().plusDays(k.toLong())
            val targetPosition = OFFSET + k + (selectedDate.toEpochDay() - today.toEpochDay()).toInt()
            viewPager.setCurrentItem(targetPosition, true)
            bottomSheetDialog.dismiss()
            updateHomeButton(targetPosition)

        }
        bottomSheetDialog.show()
        dialogView.post {
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)

                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    it.requestLayout()
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    fun updateHomeButton(position: Int) {
        if (position == OFFSET + k) {
            homeFAB.hide()
        } else if (homeFAB.isShown == false) {
            homeFAB.apply {
                startAnimation(animShow)
                show()
            }
        }
    }

    fun doneHw(fGDate: LocalDate, fGnote: String, fGsubject: String, position: Int){
        val fragPosition = viewPager.currentItem
        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
        val homework = realm.query<Homework>("date == $0 AND note == $1 AND lesson == $2",
            fGDate.toString(), fGnote, fGsubject).first().find()

        var timeList = resources.getStringArray(R.array.six)
        var thatTime = timeList[position]

        lifecycleScope.launch {
            realm.write {
                if (homework != null) {
                    findLatest(homework)?.done = true
                }
            }
//            if (refFrag != null) {
//                refFrag.refreshData(fGDate, fGsubject, thatTime, position)
//                Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
//            }
        }
    }

    fun nextSubjectDate(subject: String, FGdate: LocalDate, pressedPosition: Int) {
        Log.d("NextSubf", "subject: $subject date: $FGdate position: $pressedPosition")
        val today = FGdate
        for (i in 1..60){
            val date = today.plusDays(i.toLong())
            val dayOfWeekName = date.dayOfWeek.name

            val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeekName).first().find()

            if (schedule != null) {
                val isEvenWeek = ceil(date.dayOfYear / 7.0).toInt() % 2 == 0

                val found = schedule.lessonAndTime.any { lesson ->
                    val subjectFromDb = if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                    subjectFromDb == subject
                }

                if (found) {
                    showAddHomeworkDialog(
                        subject = subject,
                        date,
                        pressedPosition,
                        i
                    )
                    break
                }
            }

        }
//        for (i in 1..60) {
//            val date = today.plusDays(i.toLong())
//            val dayOfWeek = date.dayOfWeek
//            val weekNumber = ceil(date.dayOfYear / 7.0).toInt()
//            val isEvenWeek = weekNumber % 2 == 0
//
//            val lessons = when (dayOfWeek) {
//                DayOfWeek.MONDAY -> if (isEvenWeek) resources.getStringArray(R.array.monday1) else resources.getStringArray(
//                    R.array.monday2
//                )
//
//                DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
//                DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
//                DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
//                DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
//                else -> continue
//            }
//
//            if (lessons.contains(subject)) {
//                showAddHomeworkDialog(subject, date, pressedPosition, i, lessons)
//                break
//            }
//        }
    }

    fun showAddHomeworkDialog(
        subject: String,
        sDate: LocalDate,
        position: Int,
        toDay: Int
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_homework, null)
        val input = dialogView.findViewById<TextInputEditText>(R.id.noteInput)
        val txtLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val button = dialogView.findViewById<Button>(R.id.addButton)
        val toggleGroup = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        input.requestFocus()

        input.doOnTextChanged {text, start, before, count ->
            txtLayout.error = null
            txtLayout.isErrorEnabled = false
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(dialogView)
        val isHomewrkExists =
            realm.query<Homework>("date == $0 AND lesson == $1", sDate.toString(), subject).first()
                .find()

        var timeList = resources.getStringArray(R.array.six)
        var thatTime = timeList[position]


        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.addNewButton -> {
                        firstNoteText = input.text.toString()
                        val fragPosition = viewPager.currentItem + toDay
                        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
                        Log.d("MainActivity", "currentItem: $fragPosition and tag $fragTag")

                        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
                        lifecycleScope.launch {
                            val noteTextValue: String? = realm.write {
                                if (isHomewrkExists != null) {
                                    findLatest(isHomewrkExists)?.note
                                } else firstNoteText
                            }
                            input.setText(noteTextValue ?: "")
                        }

                        button.setOnClickListener {
                            var noteText = input.text.toString()
                            if (noteText.isEmpty()){
                                txtLayout.error = "Write homework or leave"
                                txtLayout.isErrorEnabled = true
                                return@setOnClickListener
                            }
                            lifecycleScope.launch {
                                realm.write {
                                    if (isHomewrkExists != null) {
                                        findLatest(isHomewrkExists)?.note = noteText
                                    } else {
                                        val newItem = Homework().apply {
                                            lesson = subject
                                            date = sDate.toString()
                                            note = noteText
                                        }
                                        copyToRealm(newItem)
                                    }
                                }

                                bottomSheetDialog.dismiss()
                                Snackbar.make(calendarFAB, "Homework added for $sDate", Snackbar.LENGTH_LONG)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                    .setActionTextColor(MaterialColors.getColor(calendarFAB, R.attr.colorTertiary))
                                    .setBackgroundTint(MaterialColors.getColor(calendarFAB, com.google.android.material.R.attr.colorSurfaceContainerHigh))
                                    .setTextColor(MaterialColors.getColor(calendarFAB, R.attr.colorOnSurface))
                                    .setAction("Undo") {
                                        lifecycleScope.launch {
                                            realm.write {
                                                if (isHomewrkExists != null){
                                                    findLatest(isHomewrkExists)?.note = isHomewrkExists.note
                                                }
                                                else{
                                                    realm.query<Homework>("lesson == $0 AND date == $1 AND note == $2",
                                                        subject, sDate.toString(), noteText).first().find()
                                                        ?.let { findLatest(it)?.let { delete(it) } }
                                                }
                                            }
                                        }
                                    }
                                    .show()
//                                Toast.makeText(
//                                    requireContext(),
//                                    "Homework added for $sDate",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                val allHomework = realm.query<Homework>(
                                    "date == $0 AND lesson == $1",
                                    sDate.toString(), subject
                                ).first().find()?.note
                                Log.d("HomeworkDatasds", "data: $allHomework")

//                                if (refFrag != null) {
//                                    refFrag.refreshData(sDate, subject, thatTime, position)
//                                    Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
//                                }
                                viewPager.setCurrentItem(viewPager.currentItem, true)
                            }
                        }

                    }

                    R.id.editDayButton -> {
                        firstNoteText = input.text.toString()
                        val fragPosition = viewPager.currentItem
                        val fragTag = (viewPager.adapter as DayPagerAdapter).getFragmentTag(fragPosition)
                        Log.d("MainActivity", "currentItem: $fragPosition and tag $fragTag")

                        val refFrag = childFragmentManager.findFragmentByTag(fragTag) as? DayFragment
                        currentDay =
                            LocalDate.now().plusDays((viewPager.currentItem - OFFSET).toLong())
                        Log.d("CurrentDate", "date: $currentDay")
                        var week = ceil(currentDay.dayOfYear / 7.0)
                        var currentLessons = when(currentDay.dayOfWeek) {
                            DayOfWeek.MONDAY -> if (week % 2 == 0.0) resources.getStringArray(R.array.monday1)
                            else resources.getStringArray(R.array.monday2)
                            DayOfWeek.TUESDAY -> resources.getStringArray(R.array.tues)
                            DayOfWeek.WEDNESDAY -> resources.getStringArray(R.array.wend)
                            DayOfWeek.THURSDAY -> resources.getStringArray(R.array.thurs)
                            DayOfWeek.FRIDAY -> resources.getStringArray(R.array.frid)
                            else -> arrayOf("")
                        }
                        val isHomewrkExists = realm.query<Homework>("date == $0 AND lesson == $1", currentDay.toString(), subject).first()
                            .find()
                        lifecycleScope.launch {
                            val noteTextValue: String? = realm.write {
                                if (isHomewrkExists != null) {
                                    findLatest(isHomewrkExists)?.note
                                } else firstNoteText
                            }
                            input.setText(noteTextValue ?: "")
                        }

                        button.setOnClickListener {
                            var noteText = input.text.toString()
                            if (noteText.isEmpty()){
                                txtLayout.error = "Write homework or leave"
                                return@setOnClickListener
                            }
                            lifecycleScope.launch {
                                realm.write {
                                    if (isHomewrkExists != null) {
                                        findLatest(isHomewrkExists)?.note = noteText
                                    } else {
                                        val newItem = Homework().apply {
                                            lesson = subject
                                            date = currentDay.toString()
                                            note = noteText
                                        }
                                        copyToRealm(newItem)
                                    }
                                }

                                bottomSheetDialog.dismiss()
                                Snackbar.make(calendarFAB, "Homework added for $currentDay", Snackbar.LENGTH_INDEFINITE)
                                    .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                                    .setActionTextColor(MaterialColors.getColor(calendarFAB, R.attr.colorTertiary))
                                    .setBackgroundTint(MaterialColors.getColor(calendarFAB, com.google.android.material.R.attr.colorSurfaceContainerHigh))
                                    .setTextColor(MaterialColors.getColor(calendarFAB, R.attr.colorOnSurface))
                                    .setAction("Undo") {
                                        lifecycleScope.launch {
                                            realm.write {
                                                if (isHomewrkExists != null){
                                                    findLatest(isHomewrkExists)?.note = isHomewrkExists.note
                                                }
                                                else{
                                                    realm.query<Homework>("lesson == $0 AND date == $1 AND note == $2",
                                                        subject, currentDay.toString(), noteText).first().find()
                                                        ?.let { findLatest(it)?.let { delete(it) } }
                                                }
                                            }
                                        }
                                    }
                                    .show()
//                                Toast.makeText(
//                                    requireContext(),
//                                    "Homework added for $currentDay",
//                                    Toast.LENGTH_SHORT
//                                ).show()
                                val allHomework = realm.query<Homework>(
                                    "date == $0 AND lesson == $1",
                                    currentDay.toString(), subject
                                ).first().find()?.note
                                Log.d("HomeworkDatasds", "data: $allHomework")

//                                if (refFrag != null) {
//                                    refFrag.refreshData(currentDay, subject, thatTime, position)
//                                    Log.d("FragmentFound", "tag of frag: $refFrag, $fragTag")
//                                }
                            }
                        }
                    }
                }
            }
        }
        bottomSheetDialog.show()
        toggleGroup.check(R.id.addNewButton)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FragmentDestroyed", "ScheduleFragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("FragmentDestroyedView", "ScheduleFragment")
    }

}