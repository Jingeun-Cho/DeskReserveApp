package com.zzoin.repository

import android.util.Log
import com.google.firebase.FirebaseException
import com.zzoin.common.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneOffset

class LessonRepositoryImpl : LessonRepository{
    private val firestore = AppConfig.firestore
    override fun countLessonByUidAsync(uid: String): Deferred<Int> =
        CoroutineScope(Dispatchers.IO).async {
            try{
                val today = LocalDate.now().atStartOfDay(ZoneOffset.systemDefault()).toInstant().toEpochMilli()
                val offsetOfDay : Long = 24 * 60 * 60 * 1000 - 1
                firestore
                    .collection("lesson")
                    .whereEqualTo("uid", uid)
                    .whereGreaterThanOrEqualTo("lessonDateTime", today)
                    .whereLessThanOrEqualTo("lessonDateTime", today + offsetOfDay)
                    .get()
                    .await()
                    .documents.size
            }
            catch (e : FirebaseException){
                Log.e("countLessonByUidAsync", "countLessonByUidAsync: ${e.message}", )
                -1
            }
        }
}