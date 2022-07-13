package com.zzoin.repository

import com.zzoin.model.UserDTO
import kotlinx.coroutines.Deferred

interface MemberRepository {
    fun findMemberByPhoneAsync(phone : String) : Deferred<Pair<UserDTO?, String>>
    fun findMemberByProUidAsync(proUid : String) : Deferred<ArrayList<UserDTO>>
}