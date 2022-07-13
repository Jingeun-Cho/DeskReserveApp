package com.zzoin.repository

import android.util.Log
import com.google.firebase.FirebaseException
import com.zzoin.common.AppConfig
import com.zzoin.model.UserDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class MemberRepositoryImpl :MemberRepository {
    private val firestore = AppConfig.firestore



    override fun findMemberByPhoneAsync(phone: String): Deferred<Pair<UserDTO?, String>> =
        CoroutineScope(Dispatchers.IO).async {
            try{
                val result = firestore
                    .collection("user")
                    .whereEqualTo("phone", phone)
                    .get()
                    .await()
                if(result.isEmpty) Pair(null, "")
                else{
                    val documentId = result.documents[0].id
                    val item = result.documents[0].toObject(UserDTO::class.java)
                    Pair(item, documentId)
                }
            }
            catch (e : FirebaseException){
                Log.e("findMemberByPhoneAsync", "findMemberByPhoneAsync: ${e.message}", )
                Pair(null, "")
            }
        }

    override fun findMemberByProUidAsync(proUid: String): Deferred<ArrayList<UserDTO>> =
        CoroutineScope(Dispatchers.IO).async {
            try {
                firestore
                    .collection("user")
                    .whereEqualTo("proUid", proUid)
                    .get()
                    .await()
                    .toObjects(UserDTO::class.java) as ArrayList<UserDTO>
            }
            catch (e : FirebaseException){
                Log.e("findMemberByProUidAsync", "findMemberByProUidAsync: ${e.message}", )
                arrayListOf()
            }
        }
}