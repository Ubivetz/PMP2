package stu.cn.ua.pmp1.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import model.Question
import stu.cn.ua.pmp1.observer.Observer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QuestionLoaderService : Service() {
    private val TAG = "QuestionLoaderService"
    private val binder = QuestionLoaderBinder()
    private val observers = mutableListOf<Observer>()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    inner class QuestionLoaderBinder : Binder() {
        fun getService(): QuestionLoaderService = this@QuestionLoaderService
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "Service bound")
        return binder
    }

    fun attachObserver(observer: Observer) {
        if (!observers.contains(observer)) {
            observers.add(observer)
            Log.d(TAG, "Observer attached. Total observers: ${observers.size}")
        }
    }

    fun detachObserver(observer: Observer) {
        observers.remove(observer)
    }

    fun loadQuestions() {
        Log.d(TAG, "Starting questions loading")
        executor.execute {
            try {
                val questions = loadQuestionsFromFile()
                Log.d(TAG, "Questions loaded successfully: ${questions.size} questions")
                notifyObservers(questions)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading questions", e)
                notifyObservers(emptyList())
            }
        }
    }

    private fun loadQuestionsFromFile(): List<Question> {
        val questions = mutableListOf<Question>()
        try {
            val inputStream = assets.open("questions.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line = reader.readLine()
            while (line != null) {
                val parts = line.split("|")
                if (parts.size == 6) {
                    val shuffledOptions = parts.subList(1, 5).shuffled()
                    val correctAnswer = parts[5]
                    val correctAnswerIndex = parts.subList(1, 5).indexOf(correctAnswer)
                    val shuffledCorrectAnswer = shuffledOptions[correctAnswerIndex]

                    val question = Question(
                        question = parts[0],
                        options = shuffledOptions,
                        correctAnswer = shuffledCorrectAnswer
                    )
                    questions.add(question)
                }
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading questions file", e)
        }
        return questions.shuffled()
    }

    private fun notifyObservers(questions: List<Question>) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.post {
            observers.forEach { it.update(questions) }
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        super.onDestroy()
        executor.shutdown()
    }
}