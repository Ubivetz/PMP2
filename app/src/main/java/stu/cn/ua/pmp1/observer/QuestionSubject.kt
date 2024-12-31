package stu.cn.ua.pmp1.observer

import model.Question

interface Observer {
    fun update(questions: List<Question>)
}

class QuestionSubject {
    private val observers = mutableListOf<Observer>()

    fun attach(observer: Observer) {
        observers.add(observer)
    }

    fun detach(observer: Observer) {
        observers.remove(observer)
    }

    fun notifyObservers(questions: List<Question>) {
        observers.forEach { it.update(questions) }
    }
}