/*
 * Copyright 2015, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.austindroids.austinfeedsme.data

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.subjects.PublishSubject
import javax.inject.Singleton

@Singleton
class EventsRepository(private val eventsRemoteDataSource: EventsDataSource) : EventsDataSource, RxEventsDataSource {

    val eventsSubject : PublishSubject<List<Event>> = PublishSubject.create()

    override fun getEvents(callback: EventsDataSource.LoadEventsCallback, onlyFood: Boolean) {
        eventsRemoteDataSource.getEvents(object : EventsDataSource.LoadEventsCallback {
            override fun onEventsLoaded(events: List<Event>) {
                callback.onEventsLoaded(events)
            }

            override fun onError(error: String) {
                callback.onError(error)
            }
        }, onlyFood)
    }

    override fun saveEvent(eventToSave: Event, callback: EventsDataSource.SaveEventCallback?) {
        eventsRemoteDataSource.saveEvent(eventToSave, object : EventsDataSource.SaveEventCallback {
            override fun onEventSaved(success: Boolean) {
                callback?.onEventSaved(success)
            }

            override fun onError(error: String) {
                callback?.onError(error)
            }
        })
    }

    override fun getEventsRX(onlyFood: Boolean): Observable<List<Event>> {
        eventsRemoteDataSource.getEvents(object : EventsDataSource.LoadEventsCallback {
            override fun onEventsLoaded(events: List<Event>) {
                eventsSubject.onNext(events)
            }

            override fun onError(error: String) {
                eventsSubject.onError(Throwable(error))
            }
        }, onlyFood)
        return eventsSubject
    }

    override fun saveEventRX(eventToSave: Event?): Single<Boolean> {
        return Single.create(SingleOnSubscribe {
            eventsRemoteDataSource.saveEvent(eventToSave, object : EventsDataSource.SaveEventCallback {
                override fun onEventSaved(success: Boolean) {
                    it.onSuccess(true)
                }

                override fun onError(error: String) {
                    it.onError(Throwable(error))
                }
            })
        })

    }
}
