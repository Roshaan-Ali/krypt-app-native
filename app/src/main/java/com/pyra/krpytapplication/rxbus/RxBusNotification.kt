package com.pyra.krpytapplication.rxbus

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


object RxBusNotification {

    private val bus = PublishSubject.create<Any>()

    fun send(event: Any) {
        bus.onNext(event)
    }

    fun <T> listen(eventType: Class<T>): Observable<T> = bus.ofType(eventType)
}