package stu.cn.ua.pmp1.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import model.Question
import stu.cn.ua.pmp1.observer.Observer
import stu.cn.ua.pmp1.observer.QuestionSubject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class QuestionLoaderService : Service() {
    private val binder = QuestionLoaderBinder()
    private val questionSubject = QuestionSubject()

    inner class QuestionLoaderBinder : Binder() {
        fun getService(): QuestionLoaderService = this@QuestionLoaderService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun attachObserver(observer: Observer) {
        questionSubject.attach(observer)
    }

    fun detachObserver(observer: Observer) {
        questionSubject.detach(observer)
    }

    fun loadQuestions() {
        try {
            val inputStream = assets.open("questions.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val questions = mutableListOf<Question>()

            var line = reader.readLine()
            while (line != null) {
                try {
                    val parts = line.split("|")
                    if (parts.size == 6) {
                        val shuffledOptions = listOf(parts[1], parts[2], parts[3], parts[4]).shuffled() // Перемешиваем варианты
                        val question = Question(
                            question = parts[0],
                            options = shuffledOptions,
                            correctAnswer = parts[5]
                        )
                        questions.add(question)
                    } else {
                        Log.e("QuestionLoaderService", "Invalid question format: $line")
                    }
                } catch (e: Exception) {
                    Log.e("QuestionLoaderService", "Error parsing line: $line", e)
                }
                line = reader.readLine()
            }
            val shuffledQuestions = questions.shuffled()
            questionSubject.notifyObservers(shuffledQuestions)

        } catch (e: IOException) {
            Log.e("QuestionLoaderService", "Error loading questions: ${e.message}")
            questionSubject.notifyObservers(emptyList())
        }
    }

}