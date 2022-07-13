package com.zzoin.repository

import android.util.Log
import com.google.firebase.FirebaseException
import com.zzoin.common.AppConfig
import com.zzoin.model.ManagerDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class ManagerRepositoryImpl : ManagerRepository {
    private val firestore = AppConfig.firestore
    override fun findManagerByUidAsync(uid: String): Deferred<ManagerDTO?> =
        CoroutineScope(Dispatchers.IO).async {
            try {
                firestore
                    .collection("manager")
                    .document(uid)
                    .get()
                    .await()
                    .toObject(ManagerDTO::class.java)
            }
            catch (e : FirebaseException){
                Log.e("findManagerByUidAsync", "findManagerByUidAsync: ${e.message}", )
                null
            }
        }
}