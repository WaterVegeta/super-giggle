package com.example.tabsgpttutor.settings.schedule_change.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tabsgpttutor.MyDynamic
import com.example.tabsgpttutor.R
import com.example.tabsgpttutor.SettingsViewModel
import com.example.tabsgpttutor.data_base.LessonChange
import com.example.tabsgpttutor.settings.schedule_change.adapters.LessonChangeAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.realm.kotlin.Realm
import kotlinx.coroutines.launch

class AddLessonFragment: Fragment(R.layout.add_lesson_fragment) {

    lateinit var recyclerView: RecyclerView
    lateinit var addBtn: Button
    lateinit var rvAdapter: LessonChangeAdapter
    lateinit var realm: Realm
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = MyDynamic.realm

        recyclerView = view.findViewById(R.id.rvLesson)
        addBtn = view.findViewById(R.id.addLesson)

        rvAdapter = LessonChangeAdapter(
            onEdit = { item ->
                editItem(item)
            },
            onDelete = { item ->
                deleteItem(item)
            })

        recyclerView.apply {
            adapter = rvAdapter
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator().apply {
                addDuration = 250
                changeDuration = 250
                moveDuration = 250
                removeDuration = 250
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addedLessons.collect { rvAdapter.submitList(it) }
            }
        }

        addBtn.setOnClickListener {
            addLesson(null, null)
        }

    }

    private fun deleteItem(item: LessonChange) {
        val mDiaolog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_this_lesson))
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                lifecycleScope.launch {
                    realm.write {
                        findLatest(item)?.let { delete(it) }
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        mDiaolog.show()
    }

    private fun editItem(item: LessonChange) {
        addLesson(getString(R.string.edit_lesson), item)
    }



    fun addLesson(titleText: String?, item: LessonChange?){
        val dialogView = layoutInflater.inflate(R.layout.add_lesson_diaolog, null)
        val textLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.lessonInput)
        val saveBtn = dialogView.findViewById<Button>(R.id.addButton)
        editText.setText((item?.lesson ?: "").toString())

        val alertDialog = MaterialAlertDialogBuilder(requireContext())
            .setBackgroundInsetStart(32)
            .setBackgroundInsetEnd(32)
            .setTitle(titleText ?: getString(R.string.add_lesson))
            .setView(dialogView)
            .create()

//        editText.requestFocus()
//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.setOnShowListener {
            editText.requestFocus()
            editText.postDelayed({
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }
        editText.doOnTextChanged { text, start, before, count ->
            if (!text.isNullOrEmpty()){
                textLayout.error = null
                textLayout.isErrorEnabled = false
            }
        }
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                saveBtn.performClick()
                true
            } else false
        }

        saveBtn.setOnClickListener {
            val newLesson = editText.text
            if (newLesson.isNullOrEmpty()){
                textLayout.error = getString(R.string.write_lesson)
                textLayout.isErrorEnabled = true
                return@setOnClickListener
            }
            lifecycleScope.launch {
                realm.write {
                    if (item != null){
                        findLatest(item)?.lesson = newLesson.toString()
                    } else{
                        copyToRealm(LessonChange().apply {
                            lesson = newLesson.toString()
                        })

                    }
                }
            }
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}