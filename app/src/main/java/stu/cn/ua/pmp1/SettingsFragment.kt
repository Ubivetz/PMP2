package stu.cn.ua.pmp1.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import stu.cn.ua.pmp1.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.settings_fragment, container, false)

        val sharedPreferences = requireContext().getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val questionCountSpinner = view.findViewById<Spinner>(R.id.spinner_question_count)
        val spinnerItems = listOf("5", "10", "15", "20")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionCountSpinner.adapter = adapter

        val savedQuestionCount = sharedPreferences.getInt("QuestionCount", 5)
        val defaultPosition = spinnerItems.indexOf(savedQuestionCount.toString())

        if (defaultPosition != -1) {
            questionCountSpinner.setSelection(defaultPosition)
        }

        val hintCheckBox = view.findViewById<CheckBox>(R.id.checkbox_hint_50_50)
        hintCheckBox.isChecked = sharedPreferences.getBoolean("Hint50/50", false)

        view.findViewById<Button>(R.id.btn_save_settings).setOnClickListener {
            val questionCount = questionCountSpinner.selectedItem?.toString()?.toIntOrNull()
            val isHintEnabled = hintCheckBox.isChecked

            if (questionCount != null) {
                editor.putInt("QuestionCount", questionCount)
                editor.putBoolean("Hint50/50", isHintEnabled)
                editor.apply()

                Toast.makeText(requireContext(), "Налаштування збережено: Запитань - $questionCount, Підказка - $isHintEnabled", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Будь ласка, виберіть кількість запитань", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
