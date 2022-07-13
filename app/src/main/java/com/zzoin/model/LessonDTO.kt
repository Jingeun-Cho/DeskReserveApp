package com.zzoin.model

import java.io.Serializable

data class LessonDTO(
    var coachUid : String? = "",           // 레슨 담당 프로코치 Uid
    var uid : String? = "",             // 맴버 UID
    var lessonDateTime : Long? = null,    // 레슨일
    var lessontime : Long? = 0,            //레슨 시간
    var lessonMemo : String? = null,      // 레슨 코치 메모
    var lessonNote : String? = "",       // 레슨 코치 리뷰
    var memberChecked : Boolean = false,   // 레슨 회원 확인
    var checkedTime : Long = 0,
    var type : String? = "" //종류
) : Serializable
