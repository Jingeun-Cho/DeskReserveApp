package com.zzoin.model

import java.io.Serializable

data class UserDTO(
    var uid : String? = null,
    var name : String? = null,             // 회원명 매니저
    var gender : String? = null,           // 성별 매니저
    var birth : Long = 0,                  // 생년월일 매니저
    var phone : String? = null,            // 회원 전화번호 매니저
    var email : String? = null,            // 회원 이메일 유저
    var profileImg : String? = "",          // 프로필 이미지

    var lessonAvailable : Boolean = false, // 레슨 유무

    var lessonMembershipType : String = "",
    var lessonMembershipStart : Long = 0,   //레슨권 시작일
    var lessonMembershipEnd : Long = 0,     //레슨권 종료일
    var lessonMembership : Long = 0,        //레슨권 총 갯수
    var lessonMembershipUsed: Long = 0,     //사용한 레슨권 갯수
    var lessonCancelCount : Long = 0,       //레슨 취소 횟수

    var branch : String? = null,           // 가입 지점
    var pro : String = "",         // 담당 프로코치
    var proUid : String = "",
    var point : Long = 0,                  // 포인트
    var memo : String? = null,             // 회원 메모
    var available : Boolean = false,        // 앱 가입 여부

    var ableReservation : String? = "false" //회원 예약 가능여부

): Serializable
