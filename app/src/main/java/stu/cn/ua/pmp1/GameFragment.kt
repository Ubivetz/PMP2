package stu.cn.ua.pmp1

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import model.Question
import stu.cn.ua.pmp1.observer.Observer
import stu.cn.ua.pmp1.service.QuestionLoaderService

class GameFragment : Fragment(), Observer {
    private var currentQuestionIndex = 0
    private var winnings = 0
    private lateinit var questions: List<Question>
    private var hintUsed = false
    private var totalQuestions = 0
    private var hiddenAnswerIndexes: List<Int> = emptyList()

    private var questionLoaderService: QuestionLoaderService? = null
    private var bound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as QuestionLoaderService.QuestionLoaderBinder
            questionLoaderService = binder.getService()
            bound = true

            questionLoaderService?.attachObserver(this@GameFragment)
            questionLoaderService?.loadQuestions()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            questionLoaderService = null
            bound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.game_fragment, container, false)

        Intent(requireContext(), QuestionLoaderService::class.java).also { intent ->
            requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        setupUI(view)
        loadGameSettings(view)
        return view
    }

    private fun setupUI(view: View) {
        view.findViewById<Button>(R.id.btn_take_money).setOnClickListener {
            showResultDialog("Вітаємо, ви виграли $winnings$")
        }

        val hintButton = view.findViewById<Button>(R.id.btn_hint_50_50)
        hintButton.setOnClickListener {
            if (!hintUsed) {
                use50x50Hint(view)
                hintUsed = true
                hintButton.isEnabled = false
            }
        }
    }

    private fun loadGameSettings(view: View) {
        val sharedPreferences = requireActivity().getSharedPreferences("GameSettings", Context.MODE_PRIVATE)
        val isHintEnabled = sharedPreferences.getBoolean("Hint50/50", false)
        val questionCount = sharedPreferences.getInt("QuestionCount", 5)
        totalQuestions = questionCount
        if (!isHintEnabled) {
            view.findViewById<Button>(R.id.btn_hint_50_50).visibility = View.GONE
        }
    }

    override fun update(questions: List<Question>) {
        if (questions.isNotEmpty()) {
            this.questions = questions.take(totalQuestions)
            view?.let { setupGame(it) }
        } else {
            Toast.makeText(requireContext(), "Не вдалося завантажити питання", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupGame(view: View) {
        currentQuestionIndex = 0
        hintUsed = false
        winnings = 0

        Log.d("GameFragment", "Total questions: $totalQuestions")

        if (questions.isNotEmpty()) {
            displayQuestion(view)
            setupAnswerButtons(view)
        } else {
            Toast.makeText(requireContext(), "Не вдалося завантажити питання", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayQuestion(view: View) {
        val currentQuestion = questions[currentQuestionIndex]
        Log.d("GameFragment", "Displaying question: ${currentQuestion.question}, Answers: ${currentQuestion.options}")

        if (currentQuestion.question.isNotEmpty() && currentQuestion.options.isNotEmpty()) {
            view.findViewById<TextView>(R.id.tv_question).text = currentQuestion.question
            view.findViewById<Button>(R.id.btn_answer1).text = currentQuestion.options[0]
            view.findViewById<Button>(R.id.btn_answer2).text = currentQuestion.options[1]
            view.findViewById<Button>(R.id.btn_answer3).text = currentQuestion.options[2]
            view.findViewById<Button>(R.id.btn_answer4).text = currentQuestion.options[3]
        } else {
            Toast.makeText(requireContext(), "Помилка в даних питання", Toast.LENGTH_SHORT).show()
        }

        updateWinningsText(view)
    }

    private fun setupAnswerButtons(view: View) {
        view.findViewById<Button>(R.id.btn_answer1).setOnClickListener { checkAnswer(0, view) }
        view.findViewById<Button>(R.id.btn_answer2).setOnClickListener { checkAnswer(1, view) }
        view.findViewById<Button>(R.id.btn_answer3).setOnClickListener { checkAnswer(2, view) }
        view.findViewById<Button>(R.id.btn_answer4).setOnClickListener { checkAnswer(3, view) }
    }

    private fun checkAnswer(selectedAnswerIndex: Int, view: View) {
        val currentQuestion = questions[currentQuestionIndex]
        val correctAnswer = currentQuestion.correctAnswer
        val selectedAnswer = currentQuestion.options[selectedAnswerIndex]

        Log.d("GameFragment", "Selected answer: $selectedAnswer, Correct answer: $correctAnswer")

        if (selectedAnswer == correctAnswer) {
            winnings += calculateWinnings()
            showCorrectAnswerDialog(view)
        } else {
            showIncorrectAnswerDialog()
        }
    }

    private fun use50x50Hint(view: View) {
        val currentQuestion = questions[currentQuestionIndex]
        val correctAnswerIndex = currentQuestion.options.indexOf(currentQuestion.correctAnswer)
        val wrongAnswerIndexes = (0..3).filter { it != correctAnswerIndex }.shuffled().take(2)
        hideWrongAnswers(wrongAnswerIndexes, view)
        hiddenAnswerIndexes = wrongAnswerIndexes
    }

    private fun hideWrongAnswers(wrongAnswerIndexes: List<Int>, view: View) {
        val buttons = listOf(R.id.btn_answer1, R.id.btn_answer2, R.id.btn_answer3, R.id.btn_answer4)
        buttons.forEachIndexed { index, buttonId ->
            view.findViewById<Button>(buttonId).visibility =
                if (wrongAnswerIndexes.contains(index)) View.GONE else View.VISIBLE
        }
    }

    private fun restoreAnswerButtons(view: View) {
        val buttons = listOf(R.id.btn_answer1, R.id.btn_answer2, R.id.btn_answer3, R.id.btn_answer4)
        buttons.forEach { buttonId ->
            view.findViewById<Button>(buttonId).visibility = View.VISIBLE
        }
    }

    private fun calculateWinnings(): Int {
        val totalPrize = 1000000
        val sumOfProgression = totalQuestions * (totalQuestions + 1) / 2
        val basePrize = totalPrize / sumOfProgression
        var winnings = basePrize * (currentQuestionIndex + 1)
        if (currentQuestionIndex == totalQuestions - 1) {
            val remainder = totalPrize - (basePrize * sumOfProgression)
            winnings += remainder
        }
        return winnings
    }

    private fun showCorrectAnswerDialog(view: View) {
        AlertDialog.Builder(requireContext())
            .setMessage("Правильна відповідь! Ваш виграш: $winnings$")
            .setPositiveButton("Ок") { dialog, _ ->
                restoreAnswerButtons(view)
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    displayQuestion(view)
                } else {
                    showResultDialog("Ви виграли $winnings$")
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showIncorrectAnswerDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("Неправильна відповідь! Ви програли.")
            .setPositiveButton("Ок") { _, _ ->
                requireActivity().supportFragmentManager.popBackStack()
            }
            .show()
    }

    private fun showResultDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                requireActivity().supportFragmentManager.popBackStack()
            }
            .show()
    }

    private fun updateWinningsText(view: View) {
        view.findViewById<TextView>(R.id.tv_winnings).text = "Виграна сума: $winnings$"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (bound) {
            questionLoaderService?.detachObserver(this)
            requireActivity().unbindService(serviceConnection)
            bound = false
        }
    }
}