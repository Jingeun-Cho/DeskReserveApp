package com.zzoin.controller

import kotlinx.coroutines.Deferred

interface LessonController {
    fun countTodayLesson(uid : String) : Deferred<Int>
}