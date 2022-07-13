package com.zzoin.controller

import com.zzoin.model.UserDTO
import kotlinx.coroutines.Deferred

interface MemberController {
    fun getMemberByPhone(phone : String) : Deferred<Pair<UserDTO?, String>>
    fun getMemberByProUid(proUid : String) : Deferred<ArrayList<UserDTO>>
}