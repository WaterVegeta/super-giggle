package com.example.tabsgpttutor

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tabsgpttutor.data_base.AnimationSettings
import com.example.tabsgpttutor.data_base.Homework
import com.example.tabsgpttutor.data_base.ImageItem
import com.example.tabsgpttutor.data_base.shedule.Schedule
import com.example.tabsgpttutor.shcedule.DataClass
import java.time.LocalDate
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.ResultsChange
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.ZoneId
import kotlin.math.ceil

//import io.realm.kotlin.ext.freeze

class HwViewModel: ViewModel() {
    private val realm = MyDynamic.realm

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _uniqueLessons = MutableStateFlow<List<String>>(emptyList())
    val uniqueLessons: StateFlow<List<String>> = _uniqueLessons.asStateFlow()

    init {
        observeLessons()
    }

    fun updateDate(date: LocalDate) {
        _selectedDate.value = date
    }




    val scheduleData: StateFlow<List<DataClass>> = selectedDate
        .flatMapLatest { date ->
            val dayOfWeek = date.dayOfWeek.name
            val isEvenWeek = (ceil(date.dayOfYear / 7.0).toInt() % 2 == 0)

            val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeek).first().find()
                ?: return@flatMapLatest flowOf(emptyList())

            val flows = schedule.lessonAndTime.map { lesson ->
                val subject = if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty())
                    lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                val endMinute = when(lesson.lessonEndMinute){
                    "0"->"00"
                    "1"->"01"
                    "2"->"02"
                    "3"->"03"
                    "4"->"04"
                    "5"->"05"
                    "6"->"06"
                    "7"->"07"
                    "8"->"08"
                    "9"->"09"
                    else -> lesson.lessonEndMinute
                }
                val time = if (lesson.lessonStart.isNotEmpty() && lesson.lessonEndHour.isNotEmpty() &&
                    lesson.lessonEndMinute.isNotEmpty())"${lesson.lessonStart} - ${lesson.lessonEndHour}:${endMinute}"
                else ""

                realm.query<Homework>("lesson == $0 AND date == $1", subject, date.toString())
                    .first()
                    .asFlow()
                    .map { change ->
                        change.obj?.let { hw ->
                            DataClass(
                                subject = subject,
                                time = time,
                                homework = hw.note,
                                hwId = hw.id ?: "${subject}_${date}",
                                done = hw.done
                            )
                        } ?: DataClass(
                            subject = subject,
                            time = time,
                            homework = null,
                            hwId = "${subject}_${date}",
                            done = null
                        )
                    }
                    .distinctUntilChanged { old, new ->
                        old.homework == new.homework && old.done == new.done
                    }
            }

            // Use combine with distinctUntilChanged
            combine(flows) { it.toList() }
                .distinctUntilChanged { old, new ->
                    // Compare lists efficiently
                    old.size == new.size && old.zip(new).all { (a, b) -> a.contentEquals(b) }
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(10000), emptyList())
//    val scheduleData: StateFlow<List<DataClass>> = selectedDate
//        .flatMapLatest { date ->
//            val dayOfWeek = date.dayOfWeek.name.uppercase()
//            val weekNumber = ceil(date.dayOfYear / 7.0).toInt()
//            val isEvenWeek = weekNumber % 2 == 0
//
//            val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeek).first().find()
//                ?: return@flatMapLatest flowOf(emptyList())
//
//            val lessons = schedule.lessonAndTime.mapIndexed { i, lesson ->
//                val subject = if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) {
//                    lesson.lessonSchedeleOnEven
//                } else {
//                    lesson.lessonScheduleOnOdd
//                }
//
//                val time = "${lesson.lessonStart} - ${lesson.lessonEndHour}:${lesson.lessonEndMinute}"
//
//                realm.query<Homework>(
//                    "lesson == $0 AND date == $1", subject, date.toString()
//                ).first().asFlow().map { result ->
//                    val hw = result.obj
//                    DataClass(
//                        subject = subject,
//                        time = time,
//                        homework = hw?.note,
//                        id = hw?.id.toString(),
//                        done = hw?.done
//                    )
//                }
//            }
//
//            combine(lessons) { it.toList() }
//        }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ALL = 0
    val TODAY_BEYOND = 1
    val PAST_TODAY = 2

    val DONE = 3
    val NOT_DONE = 4

    val WITH_IMAGE = 5
    val NO_IMAGE = 6

    private val _query = MutableStateFlow("")
    private val query = _query.asStateFlow()

    val done_sort = MutableStateFlow(NOT_DONE)
    val date = MutableStateFlow(TODAY_BEYOND)
    val lesson = MutableStateFlow("All")
    val image = MutableStateFlow(ALL)
    val sortField = MutableStateFlow("date")

    val sortOrder = MutableStateFlow(Sort.ASCENDING)

    val homeworkDataFlow = combine(
        date,
        sortField,
        sortOrder
    ) { dateQuery, sortFieldValue, sortOrderValue ->

        Triple(dateQuery, sortFieldValue, sortOrderValue)

    }.flatMapLatest { (dateQuery, sortFieldValue, sortOrderValue) ->
        val query = when(dateQuery){
            ALL -> realm.query<Homework>()
            TODAY_BEYOND -> realm.query<Homework>("date >= $0", LocalDate.now().toString())
            PAST_TODAY -> realm.query<Homework>("date <= $0", LocalDate.now().toString())
            else -> {realm.query<Homework>("date >= $0", LocalDate.now().toString())}
        }

        query.sort(sortFieldValue, sortOrderValue)
            .asFlow()
            .map { change ->
                when (change) {
                    is InitialResults -> change.list
                    is UpdatedResults -> change.list
                }
            }
    }

    val homeworkList: StateFlow<List<Homework>> = combine(
        homeworkDataFlow,
        _query.debounce(300),
        done_sort,
        lesson,
        image
    ) { allData, searchText, doneFilter, lessonFilter, imageFilter ->
        allData.filter { hw ->
            val matchesText = hw.note.contains(searchText, true)

            val matchesDone = when (doneFilter) {
                DONE -> hw.done == true
                NOT_DONE -> hw.done == false
                else -> true
            }

            val matchesLesson = if (lessonFilter == "All") true
            else hw.lesson == lessonFilter

            val matchesImage = when (imageFilter) {
                WITH_IMAGE -> hw.images.isNotEmpty()
                NO_IMAGE -> hw.images.isEmpty()
                else -> true
            }

            matchesText && matchesDone && matchesLesson && matchesImage
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
//
//    val homeworkList = query
//        .debounce(300)
//        .flatMapLatest { searchText ->
//            realm.query<Homework>("date >= $0 AND note CONTAINS[c] $1", LocalDate.now().toString(), searchText)
//                .sort("date", Sort.ASCENDING)
//                .asFlow()
//                .map { it.list.toList() }
//        }.stateIn(viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            emptyList())
    fun onSearch(text: String){
        _query.value = text
    }
    fun changeDone(text: Int){
        done_sort.value = text
    }
    fun changeLesson(text: String){
        lesson.value = text

    }
    fun changeImage(text: Int){
        image.value = text
    }
    fun changeDate(text: Int){
        date.value = text
    }
    fun changeSortField(text: String){
        sortField.value = text
    }
    fun changeSortOrder(sort: Sort){
        sortOrder.value = sort
    }

    private fun observeLessons() {
        viewModelScope.launch {
            realm.query(Schedule::class)
                .asFlow()
                .collect { change: ResultsChange<Schedule> ->
                    val schedules = when (change) {
                        is InitialResults -> change.list
                        is UpdatedResults -> change.list
                        else -> emptyList()
                    }

                    val allLessons = schedules.flatMap { schedule ->
                        schedule.lessonAndTime.flatMap { lesson ->
                            listOf(
                                lesson.lessonScheduleOnOdd,
                                lesson.lessonSchedeleOnEven
                            )
                        }
                    }.filter { it.isNotBlank() }
                        .toSet()
                        .sorted()

                    _uniqueLessons.value = allLessons
                }
        }
    }

    fun addHw(date: String, note: String, lesson: String){
        viewModelScope.launch {
            realm.write {
                copyToRealm(Homework().apply {
                    this.date = date
                    this.note = note
                    this.lesson = lesson
                })
            }
        }
    }
    fun doneHw(hw: Homework?, bool: Boolean?){
        viewModelScope.launch {
            realm.write {
                if (bool != null){
                    findLatest(hw as Homework)?.done = hw.done
                }else{
                    findLatest(hw as Homework)?.done = !hw.done
                }
            }
        }
    }

    fun deleteHw(ids: List<String>, context: Context) {
        viewModelScope.launch {
            realm.write {
                ids.forEach { id ->
                    Log.d("ViewModel", "deleting id: $id")
                    query<Homework>("id == $0", id)
                        .first()
                        .find()
                        ?.let {
                            it.images.forEach {
                                Log.d("FileName", "name ${it.filePath}")
                                if (it.filePath != null){
                                    deleteImage(context, it.filePath)
                                }
                            }
                            delete(it)
                            Log.d("ViewModel", "deleted: $it")
                        }
                }
            }
        }
    }

    fun deleteImage(context: Context, path: String?) {
        try {
            val file = File(path)
//            val file = File(context.filesDir, fileName)

            if (file.exists()) {
                Log.d("file", "file deleted, $file")
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun editHw(
        date: String,
        note: String,
        lesson: String,
        newDate: String,
        newNote: String,
        newLesson: String){
        viewModelScope.launch {
            realm.write {
                // More efficient direct query
                query<Homework>(
                    "date == $0 AND note == $1 AND lesson == $2",
                    date,
                    note,
                    lesson
                )
                    .first()
                    .find()
                    ?.let { hw ->
                        findLatest(hw)?.apply {
                            this.date = newDate
                            this.note = newNote
                            this.lesson = newLesson
                        }
                    }
            }

        }


    }

    suspend fun findHwById(itemId: String): Homework?{
        return realm.write {
            query<Homework>("id == $0", itemId).first().find()
        }
    }

    fun saveAnimation(whichView: String,
                      enabled: Boolean,
                      alpha: Float,
                      scaleX: Float,
                      scaleY: Float,
                      duration: Long,
                      translationX: Float,
                      translationY: Float,
                      pivotX: Float,
                      pivotY: Float,
                      interpolatorValue: String,
                      secondEnabled: Boolean,
                      alpha2: Float,
                      scaleX2: Float,
                      scaleY2: Float,
                      duration2: Long,
                      translationX2: Float,
                      translationY2: Float,
                      interpolatorValue2: String
                      ){
        viewModelScope.launch {
            realm.write {
                if (enabled){
                    query<AnimationSettings>("whatView == $0", whichView).first().find()?.let {anim ->
                        findLatest(anim)?.apply {
                            firstAnim = true
                            firstAlpha = alpha
                            firstScaleX = scaleX
                            firstScaleY = scaleY
                            firstDuration = duration
                            firstTranslationX = translationX
                            firstTranslationY = translationY
                            this.pivotX = pivotX
                            this.pivotY = pivotY
                            firstInterpolator = interpolatorValue
                            if (secondEnabled){
                                secondAnim = true
                                secondAlpha = alpha2
                                secondScaleX = scaleX2
                                secondScaleY = scaleY2
                                secondDuration = duration2
                                secondTranslationX = translationX2
                                secondTranslationY = translationY2
                                secondInterpolator = interpolatorValue2
                            }
                            else{
                                secondAnim = false
                            }
                        }
                    }
                }
                else{
                    query<AnimationSettings>("whatView == $0", whichView).first().find()?.let {anim ->
                        findLatest(anim)?.apply {
                        this.firstAnim = false
                        }
                    }
                }
            }
        }
    }

    fun getAnimations(whatView: String): AnimationSettings?{
        return realm.query<AnimationSettings>("whatView == $0", whatView).first().find()
    }
    fun copyImageToInternalStorage(context: Context, uri: Uri): List<String>? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            return listOf(FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ).toString(), file.path)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    fun saveUri(uri: String, selectedHwList: Homework?, path: String?){
        viewModelScope.launch {
            realm.write {
                selectedHwList?.let { hw ->
                    findLatest(hw)?.images?.add(
                        ImageItem().apply {
                            imageUri = uri
                            if (path != null) filePath = path
                        }
                    )

                }
            }
        }
    }
    fun findNextNice(today: LocalDate, selected: String) : MutableSet<Long>{
        val schedule = realm.query<Schedule>().find()
        val dateList : MutableSet<Long> = mutableSetOf()
        for (i in -30..60) {
            val date = today.plusDays(i.toLong())
            val dayOfWeekName = date.dayOfWeek.name
            val scheduleDay = schedule.find { it.dayOfWeek == dayOfWeekName }

            if (scheduleDay != null) {
                val isEvenWeek = ceil(date.dayOfYear / 7.0).toInt() % 2 == 0

                val found = scheduleDay.lessonAndTime.any { lesson ->
                    val subjectFromDb =
                        if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                    subjectFromDb == selected
                }
                if (found){

                    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    dateList.add(millis)
                }
            }
        }
        return dateList
    }

    fun findValidDate(selectedLesson: String): LocalDate?{
        var foundDate: LocalDate? = null
        for (i in 1..14) {
            val date = LocalDate.now().plusDays(i.toLong())
            val dayOfWeekName = date.dayOfWeek.name

            val schedule = realm.query<Schedule>("dayOfWeek == $0", dayOfWeekName).first().find()

            if (schedule != null) {
                val isEvenWeek = ceil(date.dayOfYear / 7.0).toInt() % 2 == 0

                val found = schedule.lessonAndTime.any { lesson ->
                    val subjectFromDb =
                        if (isEvenWeek && !lesson.lessonSchedeleOnEven.isNullOrEmpty()) lesson.lessonSchedeleOnEven else lesson.lessonScheduleOnOdd
                    subjectFromDb == selectedLesson
                }
                if (found){
                    foundDate = date
                    break
                }
            }
        }
        return foundDate
    }

    fun deleteCurImage(imageUri: Uri, id: String?){
        viewModelScope.launch {
            realm.write {
                query<ImageItem>("imageUri == $0 AND id == $1", imageUri.toString(), id).first().find()?.let {
                    delete(it)
                }
            }
        }
    }

    fun findHwByDateLesson(date: String, lesson: String): Homework?{
        return realm.query<Homework>("date == $0 AND lesson == $1", date, lesson).first().find()
    }
}