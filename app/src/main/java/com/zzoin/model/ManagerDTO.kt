package com.zzoin.model

import java.io.Serializable

data class ManagerDTO(
    var name : String? = null,
    var branch : String? = null,
    var phone : String? = null,
    var uid : String? = null,
    var email : String = "",
    var position : String = "",
    var restStart : Long = 0,
    var restFinish : Long = 0,
    var profileImg : String = "",          // 프로필 이미지
    var proUrl : String? = "",
    var closeToday : Long = 0
) : Serializable
