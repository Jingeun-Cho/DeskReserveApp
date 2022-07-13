package com.zzoin.common

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.zzoin.controller.*
import com.zzoin.model.ManagerDTO
import com.zzoin.model.UserDTO
import com.zzoin.repository.*
import kotlinx.coroutines.tasks.await
import java.sql.Struct
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object AppConfig {

    val firestore : FirebaseFirestore by lazy { Firebase.firestore }
    private val memberRepository : MemberRepository = MemberRepositoryImpl()
    private val managerRepository : ManagerRepository = ManagerRepositoryImpl()
    private val lessonRepository : LessonRepository = LessonRepositoryImpl()

    val memberController : MemberController = MemberControllerImpl(memberRepository)
    val managerController : ManagerController = ManagerControllerImpl(managerRepository)
    val lessonController : LessonController = LessonControllerImpl(lessonRepository)

    fun Long.convertTimestampToSimpleDate(): String = SimpleDateFormat("yy년 MM월 dd일", Locale("ko", "KR")).format(this)
    fun Long.convertTimestampToDate(): String = SimpleDateFormat("yy년 MM월 dd일 HH시 mm분", Locale("ko", "KR")).format(this)

}