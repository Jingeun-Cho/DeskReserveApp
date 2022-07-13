package com.zzoin.controller

import com.zzoin.repository.LessonRepository
import kotlinx.coroutines.Deferred

class LessonControllerImpl(private val lessonRepository: LessonRepository) : LessonController{
    override fun countTodayLesson(uid: String): Deferred<Int> = lessonRepository.countLessonByUidAsync(uid)
}