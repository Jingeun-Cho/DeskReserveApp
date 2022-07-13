package com.zzoin.repository

import kotlinx.coroutines.Deferred

interface LessonRepository {
    fun countLessonByUidAsync(uid : String) : Deferred<Int>
}